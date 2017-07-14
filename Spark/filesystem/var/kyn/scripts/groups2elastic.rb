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




require "elasticsearch"
require 'openssl'
require 'getoptlong'
require "json"

OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE

ESHOST  = "http://elasticsearch:9200"
INDEX   = "kyn-groups"
FILE    = "/tmp/kmeans_output_rue.json"



opts = GetoptLong.new(
  ["--eshost", "-e", GetoptLong::REQUIRED_ARGUMENT],
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT],
  ["--file", "-f", GetoptLong::REQUIRED_ARGUMENT]  
)

tl = Time.now.to_s
index = "#{INDEX}-#{tl[0,4]}.#{tl[5,2]}.#{tl[8,2]}"
eshost   = ESHOST
filename = FILE

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
    
groups2elastic | Syntax: 

  --eshost|-e     elasticsearch host, default: #{eshost}
  --help|-?       display this page
  --index|-i      index to write to, default: #{index}  
  --file|-f       input file, default: #{FILE}  
  
EOF
    exit 0
  when "--eshost"
    eshost = arg
  when "--index"
    index = arg
  when "--file"
    filename = arg
  end
end



client = Elasticsearch::Client.new url: "#{eshost}", log: false #, transport_options: { request: { timeout: 3600 } }

rtz = "#{tl.to_s[0,10]}T#{tl.to_s[11,8]}.000Z"

f = File.new(filename,"r")

j = JSON.load(f)

j.each do |k,v|
  for h in v
    client.index index: "#{index}", type: "group", body: { host: "#{h}", group: "#{k}","@timestamp" => "#{rtz}" }
  end
end

f.close
