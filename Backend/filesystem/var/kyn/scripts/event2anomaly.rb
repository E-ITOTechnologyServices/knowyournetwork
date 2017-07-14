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



require '/var/kyn/scripts/kestrel.rb'
require "elasticsearch"
require 'hashie'
require "getoptlong"
require 'openssl'

OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE

MYINDEX = "kyn-events-*"
MAX = 10000
PORT = 80
PERPORT = false
VERSION = "0.1"
INTERVAL = 900
ADIFF = [1, 4, 2, 3]
DESCRIPTION = "kyn-eventdescription"


class Bloomfilter
  def initialize
    @bloom = Array.new
    for i in 0..(2**24)-1
      @bloom[i] = 0
    end
  end
  
  def seen(s)
    r = false
    p1 = FNV.new.fnv1a_64(s).to_i%(2**24)
    p2 = p1
    #p2 = Digest::MurmurHash1.hexdigest(s).to_i(16)%(2**24)
    p3 = Digest::SHA1.hexdigest(s).to_i(16)%(2**24)
    p4 = Digest::MD5.hexdigest(s).to_i(16)%(2**24)
    if (@bloom[p1] + @bloom[p2] + @bloom[p3] + @bloom[p4]) < 4
      r = false
    else
      r = true
    end
    return r
  end
  
  def register(s)
    p = Array.new
    p[0] = FNV.new.fnv1a_64(s).to_i%(2**24)
    p[1] = p[0]
    # p[1] = Digest::MurmurHash1.hexdigest(s).to_i(16)%(2**24)
    p[2] = Digest::SHA1.hexdigest(s).to_i(16)%(2**24)
    p[3] = Digest::MD5.hexdigest(s).to_i(16)%(2**24)
    for i in 0..3
      if @bloom[p[i]] == nil
        @bloom[p[i]] = 1
      else
        @bloom[p[i]] = @bloom[p[i]] + 1
      end
    end
  end
end



class FNV
  INIT32  = 0x811c9dc5
  INIT64  = 0xcbf29ce484222325
  PRIME32 = 0x01000193
  PRIME64 = 0x100000001b3
  MOD32   = 2 ** 32
  MOD64   = 2 ** 64

  def fnv1_32(data)
    hash = INIT32

    data.bytes.each do |byte|
      hash = (hash * PRIME32) % MOD32
      hash = hash ^ byte
    end

    hash
  end

  def fnv1_64(data)
    hash = INIT64

    data.bytes.each do |byte|
      hash = (hash * PRIME64) % MOD64
      hash = hash ^ byte
    end

    hash
  end

  def fnv1a_32(data)
    hash = INIT32

    data.bytes.each do |byte|
      hash = hash ^ byte
      hash = (hash * PRIME32) % MOD32
    end

    hash
  end

  def fnv1a_64(data)
    hash = INIT64

    data.bytes.each do |byte|
      hash = hash ^ byte
      hash = (hash * PRIME64) % MOD64
    end

    hash
  end
end




### Main starts here!

#starttime   = Time.now - 7200 -INTERVAL

reftime = Time.now - 7200
starttime = Time.new(2016,9,20,0,0,0)
endtime   = Time.new(2016,9,28,23,59,59)

excluded  = ["0100", "0108", "0109","0303", "0107", "0401", "0402" ]

bloom = Array.new
for j in 0..3
  bloom[j] = Array.new
  for i in 0..(2**24)-1
    bloom[j][i] = 0
  end
end

known = 0
notknown = 0

index    = MYINDEX
eshost   = ESHOST
interval = INTERVAL


puts "Connecting..."
$esclient = Elasticsearch::Client.new url: "#{eshost}", timeout: 1800, log: false, transport_options: { request: { timeout: 1800 } }


x = $esclient.search index: "#{DESCRIPTION}", body: { size: 1000, query: {
    match_all: {}
  }
}


desc = Hash.new
uc   = Hash.new
sev  = Hash.new

for a in x["hits"]["hits"]
  usecase = a["_source"]["use_case"]
  description = a["_source"]["description"]
  eventid = a["_source"]["eventid"]
  severity = a["_source"]["severity"]
  
  desc[eventid] = description
  uc[eventid]  = usecase
  sev[eventid] = severity
end

timeline = Array.new
for i in 0..3
  timeline[i] = Hash.new
end


bf = Bloomfilter.new
overall = Hash.new


x = $esclient.search index: "#{index}", body: { size: 0, query: {
    match_all: {}
  },
    aggs: {
        intervals: { 
          date_histogram: { 
            field: "@timestamp", interval: "#{interval}s", min_doc_count: 1
            } , aggs: { 
              host: {
                terms: {
                  field: "host",
                  size: 2147483647 
                }, aggs: {
                eventid: {
                  terms: {
                    field: "eventid",
                    size: 2147483647
                  }, aggs: {
                    src: {
                      terms: {
                        field: "src",
                        size: 2147483647
                      }, aggs: {
                        dst: {
                          terms: {
                            field: "dst",
                            size: 2147483647
                          }, aggs: {
                            details: {
                              terms: {
                                field: "details",
                                size: 2147483647
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }


  
  max = 1
  if (x["hits"]["total"].to_i < max) || (max == 0)
    max = x["hits"]["total"].to_i
  end

  puts "Processing #{x["hits"]["total"].to_i} entries."

  dat = Array.new

  bf = Bloomfilter.new
  predictors   = Hash.new
  anomaly      = Hash.new
  anocnt       = Hash.new
  entropy      = Hash.new
  active       = Hash.new
  timelookup   = Hash.new
  bc           = Array.new
  segment      = 0
  presegment   = 0
  prog = 0 

  
  for a in x["aggregations"]["intervals"]["buckets"]
    date = a["key_as_string"]
    for b in a["host"]["buckets"]
      host = b["key"]
      for c in b["eventid"]["buckets"]
        eventid = c["key"]
        count   = c["doc_count"]
        if  (excluded.include?(eventid) == false)
          (dd,tt) = date.split(/T/)
          (year, month, day) = dd.split(/-/)
          (hour, min, sect)= (tt.chop).split(/:/)
          (sec, secm) = sect.split(/./)
          rt = Time.new(year.to_i, month.to_i, day.to_i, hour.to_i, min.to_i, sec.to_i)
          segment = ((rt.to_i)/interval)*interval
          if bf.seen("#{host}-#{eventid}") == false
              puts "Event #{rt} (#{starttime}- #{endtime}) : #{host} / #{eventid} / #{desc[eventid]} / #{sev[eventid]} / #{uc[eventid]}"
              if ((reftime-rt) < INTERVAL)
                # alert_anomaly(elastic, use case name, value, time, host, src, dst, details)
		            if (eventid == "0108") || (eventid == "0109")
                  for e in c["src"]["buckets"]
                    sr = e["key"]
                    for f in e["dst"]["buckets"]
                      ds = f["key"]
                      details = ""
                      for det in f["details"]["buckets"]
                        details = "#{details} #{det["key"]}"
                      end
                      puts "New_event #{reftime - rt} seconds ago: #{rt}: #{host} / #{eventid} / #{desc[eventid]} / #{sev[eventid]} / #{uc[eventid]}/ #{sr} / #{ds} /#{details}"
                      alert_anomaly($esclient, "#{uc[eventid]}", 0, "#{rt}", "#{sr}", "#{sr}", "#{ds}", "#{details}" )
			              end
		              end
              else
			          alert_anomaly($esclient, "#{uc[eventid]}", 0, "#{rt}", "#{host}", "", "", "#{details}" )
              end        
          end
          bf.register("#{host}-#{eventid}")
        end
      end
    end
  end
end

