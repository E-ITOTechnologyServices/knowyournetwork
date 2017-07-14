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




# Get list of group IDs from latest kyn-groups* index.

require "elasticsearch"
require 'openssl'
require "getoptlong"

ESHOST  = "http://localhost:9200"
INDEX   = "kyn-group"



OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE
eshost = ESHOST
index  = INDEX
age = false

opts = GetoptLong.new(
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--eshost", "-e", GetoptLong::REQUIRED_ARGUMENT],
  ["--age", "-a", GetoptLong::NO_ARGUMENT],  
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT]
)

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
  getGroups.rb | Syntax: 
  --help       Display this page
  --index      Elasticsearch Index prefix. Default: #{INDEX}
  --eshost     Elasticsearch Host. Default: #{ESHOST} 
  --age        Return age of latest grouping information in hours
    EOF
    exit 0
  when "--index"
    index = arg
  when "--eshost"
    eshost = arg
  when "--age"
    age = true
  end
end

client = Elasticsearch::Client.new url: "#{eshost}", log: false, transport_options: { request: { timeout: 3600 } }

# Start: Get latest group index

#z = client.cat.indices
#lines = z.map{|u| u["index"]}
##e = lines.select do |x|
#  x.include?("kyn-group")
#end
#gindex = e.sort.last
  
z = client.cat.indices
lines = z.split(/\n/)
e = lines.select do |x|
  x.include?(index)
end
gindices = Array.new
for l in e
  m = l.split(/ /)
  m.delete("")
  gindices.push(m[2])
end
gindex = gindices.sort.last
# End: Get latest group index

if (age == false)
  x = client.search index: "#{gindex}", body: { 
    query: { match_all:{}},
    aggs: {
      id: {
        terms: { 
         field: "group",
        size: 2147483647
       }
      }  
    } 
  }

  for y in x["aggregations"]["id"]["buckets"]
    puts "#{y['key']}"
  end
  
else
  x = client.search index: "#{gindex}", body: { 
    aggs: {
      age: {
        max: { 
         field: "@timestamp"
       }
      }  
    } 
  } 
  puts "#{x['aggregations']['age']['value']}"
end
