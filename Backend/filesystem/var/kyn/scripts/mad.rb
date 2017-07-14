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



# Manual Anomaly Detector (MAD)

require '/var/kyn/scripts/kestrel.rb'
require "syslog"
require "getoptlong"
require "mysql2"

# Mysql constants
@db_host = "mysql"
@db_user = "middleware"
@db_pass = "kyn_midd1eware_password"
@db_name = "aql_db"

CFG = "config.cfg"
INTERVAL = 900 # 15 minutes
# BACKTRACE = 96 * 30 # 1 month if interval = 900
BACKTRACE = 96 * 7 # 1 week if interval = 900
FILENAMEDEFAULT = "mad_ie.out"
OUTINDEX = "kyn-anomalymetrics"

def readDbRules()
    t = Array.new
    r = Array.new
    d = Array.new
    a = Array.new
    client = Mysql2::Client.new(:host => @db_host, :username => @db_user, :password => @db_pass, :database => @db_name)
    results = client.query("SELECT * from ruleset")
    results.each do |row|
        expr = row['rule']
        desc = row['description']
        if (desc == nil)
            desc = expr
        end
        p = Parser.new("#{expr}").get
        r.push(p)
        d.push(desc)
        a.push(expr)
        puts "Term: #{p}" if DEBUG
        end
    t.push(r)
    t.push(d)
    t.push(a)
    return t
end

def readConfig(n)
  f = File.new("#{n}","r")
  t = Array.new
  r = Array.new
  d = Array.new
  a = Array.new
  while l = f.gets
    l.chomp!
    (expr,desc) = l.split(/\;/)
    if (desc == nil)
      desc = expr
    end
    puts "Line: #{l}" if DEBUG
    if l[0] != "#"
      p = Parser.new("#{expr}").get
      r.push(p)
      d.push(desc)
      a.push(expr)
      puts "Term: #{p}" if DEBUG
     end
  end
  t.push(r)
  t.push(d)
  t.push(a)
  return t
end

def readPrevious(n)
  f = File.new("#{n}", "r")
  t = Array.new
  r = Array.new
  d = Array.new
  a = Array.new
  preresults = Hash.new

  l = f.gets
  l.chomp!
  headline = l.split(/\;/)
  timestamps = headline[4,headline.size-1]

  while l = f.gets
    l.chomp!
    fields = l.split(/\;/)
    expr=fields[0]
    desc=fields[1]
    host=fields[2]
    if (desc == "")
      desc = expr
    end
    if l[0] != "#"
      if !a.include?(expr)
        p = Parser.new("#{expr}").get
        r.push(p)
        d.push(desc)
        a.push(expr)
        if preresults[desc] == nil
          preresults[desc] = Hash.new
        end
        preresults[desc][host] = fields[4, fields.size-1]
        puts "Term: #{p}"  if DEBUG
      else
        puts "Skipped term: #{p}"  if DEBUG
      end
    end
  end
  
  t.push(r)
  t.push(d)
  t.push(a)
  t.push(preresults)
  t.push(timestamps)
  f.close
  # return Parser, Description, Expression, previous results, timestamps
  return t
end


opts = GetoptLong.new(
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--config", "-c", GetoptLong::REQUIRED_ARGUMENT],
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT],   
  ["--host", "-h", GetoptLong::REQUIRED_ARGUMENT],  
  ["--update", "-u", GetoptLong::REQUIRED_ARGUMENT],  
  ["--new", "-n", GetoptLong::REQUIRED_ARGUMENT],  
  ["--backtrace", "-b", GetoptLong::REQUIRED_ARGUMENT],
  ["--term", "-t", GetoptLong::REQUIRED_ARGUMENT],  
  ["--xout", "-x", GetoptLong::NO_ARGUMENT],   
  ["--plain", "-p", GetoptLong::NO_ARGUMENT], 
  ["--event", "-e", GetoptLong::NO_ARGUMENT],   
  ["--alert", "-a", GetoptLong::NO_ARGUMENT],
  ["--start", "-s", GetoptLong::REQUIRED_ARGUMENT],
  ["--finish", "-f", GetoptLong::REQUIRED_ARGUMENT]
)

term = ""
alert = false
$index = INDEX
eshost = ESHOST
cfg = CFG
backtrace = BACKTRACE
mode = "update"
filename = FILENAMEDEFAULT
term = ""
outindex = ""
logscale = true
event = false
starttime = 0
endtime = 0
use_config_file = false
#GC.disable()

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
  Manual Anomaly Detector mad.rb Version #{VERSION} | Syntax: 
  --?          Display this page
  --config     Config file. Default: #{CFG}
  --index      Elasticsearch Index. Default: #{INDEX}
  --host       Elasticsearch Host. Default: #{ESHOST}  
  --update     Update output file given as argument. 
  --new        Create output file given as argument.
  --backtrace  Number of intervals (only valid with --new). Default #{BACKTRACE}
  --alert      Alert in #{TICKER} if metric exceeds |1|.
  --event      Send event to #{EVENTINDEX} if metric > 0
  --term       Run singe query instead of reading config from file.
  --xout      Write results to elasticsearch.
  --plain      Do not use log scale output
  --start      Starttime (e.g. 2016-11-11T00:00). Use with --finish. Overwrites --backtrace.
  --finish     Endtime   (e.g. 2016-11-11T23:59). Use with --start.  Overwrites --backtrace.
    EOF
    exit 0
  when "--index"
    $index = arg
  when "--alert"
    alert = true    
  when "--host"
    eshost = arg
  when "--config"
    cfg = arg
    use_config_file = true
  when "--backtrace"
    backtrace = arg.to_i   
  when "--term"
    term = arg     
  when "--update"
    mode = "update"
    filename = arg
  when "--xout"
    t = Time.now.to_s
    outindex = "#{OUTINDEX}-#{t[0,4]}.#{t[5,2]}.#{t[8,2]}"
  when "--plain"
    logscale = false
  when "--event"
    event = true
    logscale = false
  when "--new"
    mode = "new"
    filename = arg
  when "--start" 
    starttime = intDate(arg+':00.000Z')
  when "--finish"
    endtime = intDate(arg+':00.000Z')
  end
end

if (starttime * endtime == 0) && (starttime + endtime > 0)
  puts "Starttime and endtime need to be set"
  exit
end
if (starttime != 0)
  starttime    = (starttime / INTERVAL).ceil * INTERVAL
  endtime      = (endtime / INTERVAL).floor * INTERVAL
  backtrace    = (endtime - starttime)/INTERVAL
end


$esclient = Elasticsearch::Client.new url: "#{eshost}", log: true #, transport_options: { request: { timeout: 3600 } }

if (outindex != "")
  if !$esclient.indices.exists_template(name: 'kyn-anomalymetrics')
    $esclient.indices.put_template name: 'kyn-anomalymetrics', body: { template: 'kyn-anomalymetrics*', settings: { 'index.number_of_shards' => 1 }, 
    mappings: {
      _default_: {
        dynamic_templates: [
          {
            strings: {
              mapping: {
                index: "not_analyzed",
                type: "string"
              },
              match_mapping_type: "string"
            }
          }
        ],
        properties: {
          timestamp: { type: "date" },
          metric:    { type: "string"  },
          stability: { type: "float" },
          result:    { type: "float"},
          host:      { type: "string"}
        }
      } 
    } 
  }
  end
end

lines = Array.new
timestamps = Array.new


if (mode == "update")
  # return Parser, Description, Expression, previous results, timestamps
  (expression, description, rawterms, preresults, pretimestamps) = readPrevious(filename)
  puts "preresults: #{preresults}" if DEBUG
  puts "pretimestamps: #{pretimestamps}" if DEBUG
elsif (term == "")
  if (use_config_file == true)
    (expression, description, rawterms) = readConfig(cfg)
  else 
    (expression, description, rawterms) = readDbRules()
  end
else
  (x,y,z) = term.split(/\;/)
  expression = Array.new
  description = Array.new
  rawterms = Array.new
  expression.push(Parser.new("#{x}").get)
  rawterms.push(x)
  description.push(y)
  hosts = Array.new
  hosts.push(z.chomp!)
end  
 
results = Array.new
for i in 0..expression.size-1
  results[i] = Array.new
end

f = File.new(filename,"w")

if (mode == "update")
  w = INTERVAL
else
  w = INTERVAL * backtrace
end

finishat = 0
timeslack = 0
if (starttime > 0)
  timeslack = (((Time.now.to_i/INTERVAL).ceil)*INTERVAL - starttime)
  w = timeslack
  finishat = ((Time.now.to_i/INTERVAL).floor)*INTERVAL - endtime
end


# Perform queries
j = 0 
while w > finishat
  
  t = esdate("now",w)
  timestamps[j] = t
  puts "Timestamp: #{t}"

  for i in 0..expression.size-1
    #puts "--> Evaluating expression #{i}: #{expression[i]}" 
    expression[i].timeWarp(w)
    x = expression[i].evaluate
    if x.instance_of?(Hash)
      results[i][j] = Hash.new
      x.each do |key, value|
        if (logscale)
          if (value.to_f > 0)
             results[i][j][key] = Math.log(value.to_f)
          else
            results[i][j][key] = 255
          end
        else
          results[i][j][key] = value.to_f
        end
      end
    else
      if (logscale)
        if (x.to_f > 0)
          y = Math.log(x.to_f)
        else
          y = 255
        end
      else
          y = x.to_f
      end
      results[i][j] = y
    end
    puts "--> Evaluation result: #{results[i][j]}" if DEBUG
  end
  w -= INTERVAL
  j += 1
end  

puts "results: #{results}" if DEBUG

puts "Timestamps: #{timestamps} / #{timestamps[timestamps.size-1]}" if DEBUG


# Perform Alerting
# results["expression"]["time"] = ["host" => "value", "host" => "value", ...]
if alert
  for i in 0..results.size-1
    j = 0
    for data in results[i]
      if (data.instance_of?(Hash))
        data.each do |key, value|
          if (value  != 255) && (value  > 1) || (value  < -1)
            # alert_anomaly(elastic, use case name, value, time, host, src, dst)
            alert($esclient, description[i], value, timestamps[j], key,"","", "")
          end
        end
      else
        if (data  != 255) && (data  > 1) || (data  < -1)
          # alert_anomaly(elastic, use case name, value, time, host, src, dst)
          alert($esclient, description[i], data, timestamps[j], "", "", "", "")
        end        
      end
      j += 1
    end
  end
end

if event
  for i in 0..results.size-1
    j = 0
    for data in results[i]
      if (data.instance_of?(Hash))
        data.each do |key, value|
          if (value  > 0) 
            #alert_events(elastic, use case name, value, time, host, src, dst)
            alert_event($esclient, description[i], value, timestamps[j], key, "", "", "")
          end
        end
      else
        if (data  > 0) 
          #alert_events(elastic, use case name, value, time, host, src, dst)
          alert_event($esclient, description[i], data, timestamps[j], "", "", "", "")
        end        
      end
      j += 1
    end
  end
end

### Compile metrics, necessary if some expressions are scoped
# results["expression"]["time"] = ["host" => "value", "host" => "value", ...]

# Get list of hosts
hostlist = Array.new
for i in 0..expression.size-1
  hostlist[i] = Array.new
  for j in 0..timestamps.size-1
    if (results[i][j].instance_of?(Hash))
      hostlist[i].push(results[i][j].keys)
      hostlist[i].flatten!
    end
  end
  hostlist[i].uniq!
end

# Sort results: r[desc][host][time]
sortedresults = Hash.new
for i in 0..expression.size-1
  sortedresults[description[i]] = Hash.new
  if (!expression[i].scoped)
    sortedresults[description[i]][""] = Hash.new
    for j in 0..results[i].size-1
      sortedresults[description[i]][""][timestamps[j]] = results[i][j]
    end
  else
    for k in hostlist[i]
      sortedresults[description[i]][k] = Hash.new
      puts "-> #{expression[i]} #{k}: " if DEBUG
      for j in 0..results[i].size-1
        puts "#{timestamps[j]} #{results[i][j][k]}" if DEBUG
        sortedresults[description[i]][k][timestamps[j]] = results[i][j][k]
      end
    end
  end
end


# Merge pre-results with results
if (mode == "update")
  preresults.each do |desc, rest|
    rest.each do |host, values|
      for i in 0..values.size-1
        puts "Values: #{values[i]}" if DEBUG
        if (sortedresults[desc][host] == nil)
          sortedresults[desc][host] = Hash.new
        end
        sortedresults[desc][host][pretimestamps[i]] = values[i].to_f
        sortedresults[desc][host] = Hash[sortedresults[desc][host].sort]
      end
    end
  end
end

# Calculate Stability
stability = Hash.new
sortedresults.each do |desc, rest1|
  rest1.each do |host, rest2|
    stability["#{desc}/#{host}"] = 0  
    c = 0
    rest2.each do |time, value|
      stability["#{desc}/#{host}"] += (value.to_f ** 2)
      c+= 1.0
    end
    stability["#{desc}/#{host}"] = stability["#{desc}/#{host}"] / c
    stability["#{desc}/#{host}"] = 1 / stability["#{desc}/#{host}"] 
  end
end

# Output for r[desc][host][time]
f.print "Term;Metric;Host;Stability"
if (mode == "update")
  for t in 0..pretimestamps.size-1
    if !timestamps.include?(pretimestamps[t])
      f.print ";#{pretimestamps[t]}"
    end
  end
end
for t in 0..timestamps.size-1
  f.print ";#{timestamps[t]}"
end
sortedresults.each do |desc, rest1|
  rest1.each do |host, rest2|
    stab = stability["#{desc}/#{host}"]
    trm = rawterms[description.index(desc)]
    f.print "\n#{trm};#{desc};#{host};#{stab}"
    rest2.each do |time, value|
      f.print ";#{value}"
      if outindex != ""
        $esclient.index index: "#{outindex}", type: "analyticsevent", body: { timestamp: "#{time}", metric: "#{desc}", stability: "#{stab}", result: "#{value}", host: "#{host}" } 
      end
      puts "Term: #{trm} Metric: #{desc} Host: #{host} Time: #{time} Value: #{value}" if DEBUG
    end
  end
end

f.close





