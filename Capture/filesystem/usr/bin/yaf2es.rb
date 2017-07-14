#!/usr/bin/ruby

# Copyright (C) 2017 e-ito Technology Services GmbH
# e-mail: info@e-ito.de

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.



require 'elasticsearch'
require 'json'
require 'ipaddr'
require 'getoptlong'

# convert string of flags to netflow notation "...R.."
def netflow_flags(flags)
   flags_netflow = {'U' => '.', 'A' => '.', 'P' => '.', 'R' => '.', 'S' => '.', 'F' => '.' }
   flags.split("").uniq.each { |x| if "UAPRSF".include? x then flags_netflow[x] = x end }
   netflow_flags = ""
   flags_netflow.each { |k,v| netflow_flags += v }
   return netflow_flags
end


# replace yaf fields names with netflow names
mappings = {
        "dataByteCount"=>"InputBytes",
        "destinationIPv4Address"=>"DestinationAddress",
        "destinationMacAddress"=>"OutputDestinationMACAddress",
        "destinationTransportPort"=>"DestinationPort",
        "egressInterface"=>"InterfaceOUT",
        "flowDurationMilliseconds"=>"Duration",
        "flowEndMilliseconds"=>"EndTime",
        "flowStartMilliseconds"=>"StartTime",
        "ingressInterface"=>"InterfaceIN",
        "packetTotalCount"=>"InputPackets",
        "protocolIdentifier"=>"Protocol",
        "reverseDataByteCount"=>"OutputBytes",
        "reversePacketTotalCount"=>"OutputPackets",
        "sourceIPv4Address"=>"SourceAddress",
        "sourceMacAddress"=>"InputSourceMACAddress",
        "sourceTransportPort"=>"SourcePort"
} 

eshost = "http://elasticsearch:9200"
esdebug = false
ipv6 = false

opts = GetoptLong.new(
  ["--eshost", "-e", GetoptLong::REQUIRED_ARGUMENT],
  ["--esdebug", "-d", GetoptLong::NO_ARGUMENT],
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--ipv6", "-6", GetoptLong::NO_ARGUMENT]
)

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF

YAF/super_mediator import, works on STDIN. | Syntax:

  --eshost|-e     name of Elasticsearch host. Default: #{eshost}
  --esdebug|-d    show Elasticsearch queries
  --help|-?       display this page
  --ipv6|-6       IPv6 flows are written to kyn-ipv6-netflow-* index, instead of being ignored completely

  EOF
    exit 0
  when "--eshost"
    eshost = arg
  when "--esdebug"
    esdebug = true
  when "--ipv6"
    ipv6 = true
  end
end

client = Elasticsearch::Client.new url: eshost, log: esdebug

elastic_bulk = { body: [] }
counter = 0

ARGF.each do |line|
    begin
      line_json = JSON.parse(line)
      rescue Exception
      $stderr.print "Line\n#{line}\nskipped, Error: " + $!.to_s
      next
    end
    case line_json.keys[0]
    when "flows"
      # strip 'flows' from JSON string
      line_json = line_json['flows']

      elastic_type = 'yaf'
      
      # rename hash keys
      mappings.each do |k,v|
        if line_json[k] != nil
          line_json[v] = line_json[k]
          line_json.delete(k)
        end
      end
      
      ## some fields need to be calculated/manipulated
      # *Address (yaf does not use its *IPv6Address fields)
      source_ip = IPAddr.new line_json['SourceAddress']
      destination_ip = IPAddr.new line_json['DestinationAddress']
      if source_ip.ipv6? and destination_ip.ipv6?
        if ipv6
          elastic_type = 'yaf_IPv6'
        else
          next
        end
      elsif line_json['SourceAddress'] == "::" and destination_ip.ipv4?
        line_json['SourceAddress'] = "0.0.0.0"
      end

      # Bytes
      line_json['InputBytes'] != nil ? line_json['Bytes'] = line_json['InputBytes'] : line_json['Bytes'] = 0
      line_json['Bytes'] += line_json['OutputBytes'] unless line_json['OutputBytes'] == nil

      # Duration
      line_json['Duration'] = (line_json['Duration']).round

      # Packets
      line_json['Packets'] = line_json['InputPackets']
      line_json['Packets'] += line_json['OutputPackets'] unless line_json['OutputPackets'] == nil
      # TCPFlags
      if line_json['Protocol'] == 6
        flags_yaf_string = line_json['initialTCPFlags'] + line_json['unionTCPFlags']
        reverse_flags_yaf_string = ""
        reverse_flags_yaf_string += line_json['reverseInitialTCPFlags'] unless line_json['reverseInitialTCPFlags'] == nil
        reverse_flags_yaf_string += line_json['reverseUnionTCPFlags'] unless line_json['reverseUnionTCPFlags'] == nil 
        line_json['TCPFlags'] = netflow_flags(flags_yaf_string + reverse_flags_yaf_string)
        line_json['DestinationTCPFlags'] = netflow_flags(reverse_flags_yaf_string)
        line_json['SourceTCPFlags'] = netflow_flags(flags_yaf_string)
      end
      # SourceVLAN
      line_json['SourceVLAN'] = line_json['DestinationVLAN'] = line_json['vlanId'].hex.to_int
      # SourceTOS
      line_json['SourceTOS'] = line_json['DestinationTOS'] = line_json['ipClassOfService'].hex.to_int
      # host
      line_json['host'] = Socket.gethostname
      # timestamps
      line_json['StartTime'][10] = "T"
      line_json['StartTime'] += "Z"
      line_json['EndTime'][10] = "T"
      line_json['EndTime'] += "Z"
      line_json['@timestamp'] = line_json['EndTime']
      # *MACAddress
      line_json['InputSourceMACAddress'][17] = ""
      line_json['OutputDestinationMACAddress'][17] = ""
      # let me see some id
      id = line_json['StartTime'] + ';' + source_ip.to_s + ';' + destination_ip.to_s + ';' + line_json['SourcePort'].to_s + ';' + line_json['DestinationPort'].to_s + ';' + line_json['Protocol'].to_s
      id = Digest::MD5.hexdigest id

      # sending the bulk to elasticsearch
      elastic_bulk[:body].push({ index: { _index: 'kyn-netflow-' + line_json['@timestamp'][0...10], _type: elastic_type, _id: id } })
      elastic_bulk[:body].push(line_json.clone)
      counter += 1
      if counter == 5000
        client.bulk elastic_bulk
        puts "Bulk send, last date: #{line_json['@timestamp']}"
        elastic_bulk = { body: [] }
        counter = 0
      end

    end


end

# sending the bulk to elasticsearch (catch all ...)
if elastic_bulk != { body: [] }
  client.bulk elastic_bulk
end
