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




# import_ip_cluster.rb 20161208
# Markus Jahnke
# ingest calculated ip clusters

require 'elasticsearch'
require 'getoptlong'
require 'ipaddr'
require 'time'

# disable proxy 
ENV['http_proxy']=nil

eshost = "http://localhost:9200"
elastic_bulk = { body: [] }
cluster = {}
counter = 0
lines = 0
avg = 0
file = '/var/kyn/tmp/cluster_score.csv'
es_index = 'kyn-cluster'
ingest = true

opts = GetoptLong.new(
  ["--eshost", "-e", GetoptLong::REQUIRED_ARGUMENT],
  ["--esindex", "-x", GetoptLong::REQUIRED_ARGUMENT],
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--ingest", "-i", GetoptLong::REQUIRED_ARGUMENT]  
)

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
    
IP cluster importer | Syntax: 

  --eshost|-e     elasticsearch host, default: #{eshost}
  --esindex|-x    elasticsearch index, default: #{es_index}
  --help|-?       display this page
  --ingest|-i     file to read from, default: #{file}  

EOF
    exit 0
  when "--eshost"
    eshost = arg
  when "--esindex"
    es_index = arg
  when "--ingest"
    ingest = true
    file = arg
  end
end

if ingest then

# calculate average
  f = File.open(file, 'r')
  puts "calculating avg #{file}"
  f.each_line do |line|
    line.strip!
    avg = avg + line.split(',')[-1].to_f
    lines += 1
  end
  f.close
  avg = avg / lines  
  puts "average is #{avg}"


# ingest to elasticsearch
  client = Elasticsearch::Client.new url: eshost #, log: true
  f = File.open(file, 'r')
  puts "ingesting #{file}"
  f.each_line do |line|
    line.strip!
    ip = line.split(',')[0]
    cluster['@timestamp'] = Time.now.iso8601
    cluster['host'] = ip
    if avg == 0 then 
      cluster['score'] = 0
    else 
      cluster['score'] = line.split(',')[-1].to_f / avg
    end
    # puts line
    # puts cluster['score']
    elastic_bulk[:body].push({ index: { _index: es_index + '-' + Time.now.strftime('%Y-%m-%d'), _type: 'cluster', _id: cluster['host'] } })
    elastic_bulk[:body].push(cluster.clone)
    counter += 1
    if counter == 5000
      client.bulk elastic_bulk
      elastic_bulk = { body: [] }
      counter = 0
    end
  end
  f.close
  # sending the bulk to elasticsearch (catch all ...)
  if elastic_bulk != { body: [] }
    client.bulk elastic_bulk
  end
  puts "done"
end
