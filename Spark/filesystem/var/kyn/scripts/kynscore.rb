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




require "configparser"
require "open3"
# XINPUT:  To be replaced with input file name upon execution
# XOUTPUT: To be replaced with output file name upon execution
# XSIZE:   To be replaced with number of clusters upon execution
# XINDEX:  To be replaced with ES index upon execution
# XEVENT:  To be replaced with list of relevant event ids upon execution
# XGROUP:  To be replaced with list of relevant groups upon execution
#CFG          = '/var/kyn/scripts/kynscore.cfg'
CFG = 'kynscore.cfg'
EXTRACT_CMD  = '/var/kyn/scripts/extractMetrics.rb --output=XOUTPUT --groups=XGROUP --event=XEVENT'
#ANALYSIS_CMD = {'KMEANS'=> '/usr/local/spark/bin/spark-shell --driver-memory 16G --executor-memory 32G -i /home/hadoopuser/kyn_cluster_20161230.scala --conf spark.driver.args="XINPUT XOUTPUT XSIZE"'}
UPLOAD_CMD   = '/var/kyn/scripts/import_ip_cluster.rb --esindex XINDEX --ingest XINPUT'
GROUP_CMD    = '/var/kyn/scripts/getGroups.rb'
IMPORT_CMD   = '/var/kyn/scripts/groups2elastic.rb'
TEMPFILE     = "kyn-analysis"
ESHOST       = "http://localhost:9200"

def toSec(i)
  c = i[-1]
  m = 1
  case c
    when "y"
      m = 86400 * 365 
    when "m"
      m = 86400 * 28
    when "w"
      m = 86400 * 7
    when "d"
      m = 86400
    when "h"
      m = 3600
    when "s"
      m = 1
  end
  return i.chop.to_i * m
end

def configError(e)
  puts e
  exit
end

# 1. Lese Namen der Score-Konfigurationen aus Sektion "active"
# 2. a) Lese Score-Konfigurationen und speichere Namen der Profiling-Konfigurationen im Array "profile_names"
# 2. b) Lese Score-Konfigurationen und speichere Namen der Anomaly-Konfigurationen im Array "anomaly_names"
# 3. a) Lesen der Profiling-Konfigurationen

profile_names = Array.new
anomaly_names = Array.new

profile_configs = Hash.new
anomaly_configs = Hash.new
score_configs   = Hash.new

cp = ConfigParser.new(CFG)

# Set eshost if included in config file.
if cp["eshost"] != nil
  eshost = cp["eshost"].map{|x,y| "#{x}:#{y}"}[0]
else
  eshost = ESHOST
end

# Search for active config sets
cp["active"].each do |x,y| # 1.
  score = cp[x]
  profile_names.push(score["profile"]) # 2.
  anomaly_names.push(score["anomaly"])
end

for p in profile_names # 3.
  if (p.upcase != "NONE")
    if (cp[p] == nil)
      configError("Profiling algorithm not defined: #{p}")
    end
    profile_configs[p] = Hash.new
    profile_configs[p]["algo"]   = cp[p]["algo"]
    profile_configs[p]["clusters"] = cp[p]["clusters"]
    profile_configs[p]["age"] = cp[p]["freq"]
    profile_configs[p]["index"] = cp[p]["index"]
    profile_configs[p]["files"] = cp[p]["files"]
    profile_configs[p]["cmd"] = cp[p]["cmd"]
  
    if (cp[p]["index"] == nil)
      configError("No index name supplied for profiling algorithm #{p}")
    end
    # Check if update needed
    if (profile_configs[p]["age"] != nil)
      refage = toSec(profile_configs[p]["age"])
      stdout, stderr, status = Open3.capture3(GROUP_CMD,"--index=#{profile_configs[p]["index"]}","--age","--eshost=#{eshost}")
      puts "Time of last categorization: #{stdout}"
      age = Time.now - Time.at(stdout.to_f/1000)
      puts "Categorization #{p}: Current ags is #{age}. Maximum allowed age is #{refage}"
      if age > refage
        # Update required
        stdout, stderr, status = Open3.capture3(profile_configs[p]["cmd"])
	puts "Result of profiling update: #{stdout}/#{stderr}/#{status} "
        idx = profile_configs[p]["index"]+"-"+Time.now.to_s[0,4]+"."+Time.now.to_s[5,2]+"."+Time.now.to_s[8,2]
        fle = profile_configs[p]["files"]
        stdout, stderr, status = Open3.capture3(IMPORT_CMD,"--file=#{fle}","--index=#{idx}","--eshost=#{eshost}")
	puts "Result of profiling import: #{stdout}/#{stderr}/#{status} "
      end
    end
    # Get groups
    puts "GROUPS: #{profile_configs[p]["index"]}"
    stdout, stderr, status = Open3.capture3(GROUP_CMD,"--index=#{profile_configs[p]["index"]}","--eshost=#{eshost}")
    profile_configs[p]["clustermembers"] = stdout.split("\n")
  else
    profile_configs[p] = ""
  end
end

for q in anomaly_names
  puts q
  anomaly_configs[q] = Hash.new
  if (cp[q]["cmd"] == nil)
    configError("Within config for detection #{q}: No command specified.")
  end
  
  if ((cp[q] == nil)||(cp[q]["algo"] == nil) )
    configError("Within config for detection #{q}: Algorithm not set or not known: #{anomaly_configs[q]["algo"]}")
  end
  anomaly_configs[q]["cmd"] = cp[q]["cmd"]

  if cp[q]["clusters"] == nil
    anomaly_configs[q]["clusters"] = 0
  else
    anomaly_configs[q]["clusters"] = cp[q]["clusters"]
  end
  anomaly_configs[q]["algo"]   = cp[q]["algo"]
end


cp["active"].each do |x,y|
  score_configs[x] = Hash.new
  score_configs[x]["anomaly"] = cp[x]["anomaly"]
  score_configs[x]["events"]  = cp[x]["events"]
  score_configs[x]["index"]   = cp[x]["index"]
  score_configs[x]["run"]     = cp[x]["run"]
  score_configs[x]["groups"]  = cp[x]["groups"]
  score_configs[x]["profile"]  = cp[x]["profile"]
  
  if (cp[x]["groups"] == nil)
    score_configs[x]["groups"] = "ALL"
  end
  
  if (cp[x]["events"] == nil)
    score_configs[x]["events"] = "ALL"
  end
  
  if ((score_configs[x]["anomaly"] == nil) ||(anomaly_configs[score_configs[x]["anomaly"]] == nil))
    configError("Within config for score #{x}: Algorithm not set or not known: #{score_configs[x]["anomaly"]}")
  end
  
  if ((score_configs[x]["profile"] == nil) ||(profile_configs[score_configs[x]["profile"]] == nil))
    configError("Within config for score #{x}: Profiling algorithm not set or not known: #{score_configs[x]["profile"]}")
  end
  
  
end

puts score_configs
puts anomaly_configs
puts profile_configs


cp["active"].each do |x,y|
 
  aconf  = score_configs[x]["anomaly"]
  groups = anomaly_configs[aconf]["groups"]
  events = score_configs[x]["events"]
  size   = anomaly_configs[aconf]["clusters"]
  groups = score_configs[x]["groups"]
  index  = score_configs[x]["index"]
  analysis_algo = anomaly_configs[aconf]["algo"]
  cmd    = anomaly_configs[aconf]["cmd"]

  # groups: 
  # list   -> run cmds with groups in list
  # "each" -> run cmds for each group available
  # "all"  -> run cmds with groups "ALL"
  idxsuffix = false
  gstack = Array.new
  if (groups.upcase != "EACH")
    gstack.push(groups)    
  else
    gstack = profile_configs[score_configs[x]["profile"]]["clustermembers"]
    idxsuffix = true
  end
  puts "GROUPS: #{groups} GSTACK: #{gstack}"
  
  for lgroup in gstack
  
    # Generate temp file names
    t = rand(10000)
    chain1 = "/tmp/#{TEMPFILE}1#{t}.tmp"
    chain2 = "/tmp/#{TEMPFILE}2#{t}.tmp" 
    chain3 = "/tmp/#{TEMPFILE}3#{t}.tmp" 
  
    lindex = index
    if (idxsuffix)
      lindex = "#{index}_#{lgroup}"
    end
    
    extract_cmd  = EXTRACT_CMD.gsub(/XOUTPUT/,chain1).gsub(/XGROUP/,lgroup).gsub(/XEVENT/,events) + " --eshost=#{eshost}"
    analysis_cmd = cmd.gsub(/XINPUT/,chain1).gsub(/XOUTPUT/,chain2).gsub(/XSIZE/,size)
    cat_cmd      = "cat #{chain2}/* >> #{chain3}"
    upload_cmd   = UPLOAD_CMD.gsub(/XINDEX/,lindex).gsub(/XINPUT/,chain3) + " --eshost=#{eshost}"
    rm_cmd       = "rm -rf #{chain1} #{chain2} #{chain3}"
    
    puts extract_cmd
    system(extract_cmd)
    puts analysis_cmd
    system(analysis_cmd)
    puts cat_cmd
    system(cat_cmd)
    puts upload_cmd
    system(upload_cmd)
    puts rm_cmd
    system(rm_cmd)
  end
end

