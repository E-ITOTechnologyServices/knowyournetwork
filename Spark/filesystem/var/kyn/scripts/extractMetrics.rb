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



OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE

ESHOST  = "http://127.0.0.1:9200"
INDEX   = "kyn-events-201*"
GINDEX  = "kyn-groups"
FILENAME = "kmeansinput.csv"

opts = GetoptLong.new(
  ["--eshost", "-l", GetoptLong::REQUIRED_ARGUMENT],
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT],
  ["--output", "-o", GetoptLong::REQUIRED_ARGUMENT],
  ["--events", "-e", GetoptLong::REQUIRED_ARGUMENT],  
  ["--groups", "-g", GetoptLong::REQUIRED_ARGUMENT],   
)

filename = FILENAME
events = nil
groups = nil
eshost = ESHOST
index  = INDEX
groupfilter = false
eventfilter = false

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
    
extractMetrics | Syntax: 

  --eshost|-e     elasticsearch host, default: #{eshost}
  --help|-?       display this page
  --index|-i      index to read from, default: #{INDEX}  
  --output|-o     output file base name, default: #{FILENAME}  
  --events|-e     events to include, default: all events 
  --groups|-g     groups to include, default: all groups

  
EOF
    exit 0
  when "--eshost"
    eshost = arg
  when "--index"
    index = arg
  when "--output"
    filename = arg
  when "--events"
    if arg.upcase == "ALL"
      eventfilter = false
    else
      events = arg.split(/,/)
      eventfilter = true
    end
  when "--groups"
    if arg.upcase == "ALL"
      groupfilter = false
    else
      groups = arg.split(/,/)
      groupfilter = true
    end
  end
end

client = Elasticsearch::Client.new url: "#{eshost}", log: false, transport_options: { request: { timeout: 3600 } }





iplist = nil
flt = "{}"
if (groupfilter)
  z = client.cat.indices
  lines = z.split(/\n/)
  e = lines.select do |x|
    x.include?(GINDEX)
  end
  gindices = Array.new
  for l in e
    m = l.split(/ /)
    m.delete("")
    gindices.push(m[2])
  end
  gindex = gindices.sort.last

  iplist = Array.new
  flt = '{ "bool": { "should": [ '
  for g in groups
    if groups.index(g) > 0
      flt = flt + ","
    end
    flt = flt +'{ "match": { "group": "'+"#{g}"+'" } }'
  end
  flt = flt +'], "minimum_should_match": 1} }'   
  
  qry = '{ "query": '+flt+'}'
  y = client.search index: "#{gindex}", scroll: "5m", body: qry

  
  while not y['hits']['hits'].empty?
    for z in y["hits"]["hits"]
      iplist.push(z["_source"]["host"])
    end
    scrollid = y["_scroll_id"]
    y = client.scroll scroll: '5m', scroll_id: scrollid
  end
end

flt = '{ "match_all": {}}'
# Read all events
if eventfilter
  eventlist = events
else
  qry = '{ "size": 0, "query": '+flt+', "aggs": { "events": { "terms": { "field": "eventid", "size": 2147483647 } } } }'
  x = client.search index: "#{index}", body: qry

  # Build list of known events
  eventlist = Array.new
  for y in x["aggregations"]["events"]["buckets"]
    eventlist.push(y["key"])
  end
end

idx = index
# Read events for all hosts
  
#flt = "{match_all: {}}"
flt = "{}"

if eventfilter
  flt = '{ "bool": { "should": [ '
  for e in events
    if events.index(e) > 0
      flt = flt + ","
    end
    flt = flt +'{ "match": { "eventid": "'+"#{e}"+'" } }'
  end
  flt = flt +'], "minimum_should_match": 1} }'  
end

#qry = '{ size: 0, query: { filtered: { query: '+flt+',filter: { range: { "@timestamp": { gte: "now-24h", lte: "now" }}}}}, aggs: {ip: { terms: { field: "host",size: 0}, aggs: {events: { terms: {field: "eventid"}}}}}}'
qry = '{ "size": 0, "query": { "bool": { "must": '+flt+',"filter": { "range": { "@timestamp": { "gte": "now-24h", "lte": "now" }}}}}, "aggs": {"ip": { "terms": { "field": "host","size": 2147483647}, "aggs": {"events": { "terms": {"field": "eventid"}}}}}}'

x = client.search index: "#{idx}", body: qry
  

f = File.new(filename,"w")

for y in x["aggregations"]["ip"]["buckets"]
  ip = y['key']
  if (!groupfilter)||(iplist.include?(ip) == true)
    eventrecord = Hash.new
    for z in y["events"]["buckets"]
      eventid = z["key"]
      number   = z["doc_count"]
      eventrecord[eventid] = number
    end
    
    s = "#{ip} "
    for e in eventlist
      if eventrecord[e] == nil
        s = s + "0 "
      else
        s = s + "#{eventrecord[e]} "
      end
    end
    f.puts "#{s}"
  end
end

  
f.close
