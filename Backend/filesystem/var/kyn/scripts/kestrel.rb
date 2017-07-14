#!/usr/bin/ruby
# 

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




#
# ### Beschreibung:
# #
# # Bibliothek für Abfragen des netflow5-Indexes in Elasticsearch mit logischen Ausdrücken
# #
# #
# ###
# 
# ### Grammatik:
# # t -> ( t BINOP t ) | ( EVAL t ) | "SRC" | "DST" | "PROTO" | "PORT" | <const>
# # BINOP -> "MATCHES" | ">" | "<" | "=" | "AND" | "OR" | "+" | "-" | "*"
# # EVAL -> EVAL_OP starttime, endtime, t | EVAL_OP SCOPE_OP <const>, starttime, endtime, t
# # EVAL_OP -> "SUM" | "COUNT" | "DCARD" | "SCARD"
# # SCOPE_OP -> "SSCOPE" | "DSCOPE"
# ###
#
# ## GP Funktionsumfang
# # Mutation:
# # JA: Binop, Eval
# #
# # Rekombination:
# # JA: Binop 
# # NEIN: Eval (Wird noch als Terminalsymbol betrachtet, d.h. mit Rekombination können keinen neue Elasticsearch-Queries erzeugt werden.)
# # 
# ##
#
# ### To Do:
# # 
# #
# #
# ###


require "elasticsearch"
require 'openssl'
require 'ipaddr' 


OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE

ESHOST  = "http://elasticsearch:9200"
#ESHOST  = "http://10.11.12.117:9200"
#ESHOST  = "http://10.11.12.81:9200"


INDEX   = "kyn-netflow*"
SLICE   = 900
VERSION = "20170214"
DEBUG = false
DEBUG_PARSER = false
EVALSTART = "2014-11-11T00:00:00.000Z"
EVALEND   = "2018-11-11T00:00:00.000Z"
INTERVAL  = 900
SCOPESIZE = 2147483647
TICKER    = "kyn-newsticker"
EVENTINDEX = "kyn-events"
EVENTDESC = "kyn-eventdescription"
CEFVERSION = "0.1"

Intervals = [["now-15m", "now"], ["now-75m", "now-60m"], ["now-1455m", "now-1440m"], ["now-1h", "now"], ["now-1440m", "now"] , ["now-135m", "now-120m"]]

BinOp = ["MATCHES",">","<","=","AND","OR","+","-","*","/","AS","FOUND"]
Evals = ["SUM","COUNT","MIN","MAX","SCARD","DCARD","PCARD","LISTED"]
Key   = ["SRC","DST","PROTO","PRT","FLAGS"]
Scope = ["SSCOPE", "DSCOPE"]

$cache = Hash.new
$intel = Hash.new
$eventcache = Hash.new

# alert_events(elastic, use case name, value, time, host, src, dst, dt)
def alert_event(es,m,v,t,h,s,d,dt)
  # puts "EVENT: #{m} - #{v} / #{t} / #{h}"

  # Determine index name
  tl = Time.now.to_s
  event = "#{EVENTINDEX}-#{tl[0,4]}.#{tl[5,2]}.#{tl[8,2]}"

  # Get event description
  if ($eventcache[m] == nil)
    x = es.search index: "#{EVENTDESC}", body: { size: 1, query: { term: { use_case: "#{m}"} } }
  	begin
      eventid = x["hits"]["hits"][0]["_source"]["eventid"]  
      desc    = x["hits"]["hits"][0]["_source"]["description"]
      sev     = x["hits"]["hits"][0]["_source"]["severity"]
      ana     = x["hits"]["hits"][0]["_source"]["analytics"]
    rescue
      eventid = "0000"
      desc    = "unknown event"
      sev     = 0
      ana     = ""
    end
    $eventcache[m] = Hash.new
    $eventcache[m]["eventid"] = eventid
    $eventcache[m]["desc"] = desc
    $eventcache[m]["sec"] = sev
    $eventcache[m]["ana"] = ana
    
  else
    eventid = $eventcache[m]["eventid"]
    desc = $eventcache[m]["desc"]
    sev = $eventcache[m]["sec"]  
    ana = $eventcache[m]["ana"]
  end
  
  # Get event details
  
  if (ana != nil)
    p = Parser.new("#{ana.gsub(/TARGET/,h)}").get
    anadetails = p.evaluate
    anadetails.each do |k,v|
      dt << " #{k} (#{v})"
    end
  end
    
  # Write to EVENT index

  es.index index: "#{event}", type: "event", body: { use_case: "#{m}", description: "#{desc}", details: "Actual value: #{v}", "@timestamp" => "#{t}.000Z", host: "#{h}", script: "mad.rb #{VERSION}", eventid: "#{eventid}", details: "#{dt}", src: "#{s}", dst: "#{d}", severity: "#{sev}"}
  
  # CEF:Version|Device Vendor|Device Product|Device Version|Device Event Class ID|Name|Severity|[Extension]
  #Syslog.open("kyn", 0 | Syslog::LOG_DAEMON) { |s| s.warning "CEF:0|e-ito|kite|#{CEFVERSION}|#{eventid}|#{sev}|Host=#{h};Details=#{desc}" }

end

# alert_anomaly(elastic, use case name, value, time, host, src, dst, details)
def alert_anomaly(es,m,v,t,h,s,d,dt)
  # Determine index name
  t1 = Time.now.to_s
  ticker = "#{TICKER}-#{t1[0,4]}.#{t1[5,2]}.#{t1[8,2]}"
  rtz = "#{t.to_s[0,10]}T#{t.to_s[11,8]}.000Z"

  if ($eventcache[m] == nil)
    x = es.search index: "#{EVENTDESC}", body: { size: 1, query: { term: { use_case: "#{m}"} } }
  	begin
      eventid = x["hits"]["hits"][0]["_source"]["eventid"]  
      desc    = x["hits"]["hits"][0]["_source"]["description"]
      sev     = x["hits"]["hits"][0]["_source"]["severity"]
      ana     = x["hits"]["hits"][0]["_source"]["analytics"]
    rescue
      eventid = "0000"
      desc    = "unknown event"
      sev     = 0
      ana     = ""
    end
    $eventcache[m] = Hash.new
    $eventcache[m]["eventid"] = eventid
    $eventcache[m]["desc"] = desc
    $eventcache[m]["sec"] = sev
    $eventcache[m]["ana"] = ana
    
  else
    eventid = $eventcache[m]["eventid"]
    desc = $eventcache[m]["desc"]
    sev = $eventcache[m]["sec"]  
    ana = $eventcache[m]["ana"]
  end  

  
  if (ana != nil)
    p = Parser.new("#{ana.gsub(/TARGET/,h)}").get
    anadetails = p.evaluate
    anadetails.each do |k,v|
      dt << " #{k} (#{v})"
    end
  end
  
  # Write anomaly to newsticker
  es.index index: "#{ticker}", type: "news", body: { text: "#{desc}", "@timestamp" => "#{rtz}", host: "#{h}", src: "#{s}", dst: "#{d}", priority: "#{sev}", eventid: "#{eventid}", value: "#{v}", details: "#{dt}" }
  #puts    "TICKER: #{ticker}, type: news, body: { text: #{m}, @timestamp => #{rtz}, host: #{h}, src: #{s}, dst: #{d}, priority: #{sev}, eventid: #{eventid}, details: #{desc}, value: #{v} }"
end


def esdate(x,w)
  r = x
  x.upcase!
  z = 0
  
  # Bestimme Zeitzones-Offset
  (d,t,zone) = Time.local(*$evaltime).to_s.split(/ /)
  o = zone[1].to_i * 36000 + zone[2].to_i * 3600 + zone[3].to_i * 600 + zone[4].to_i * 60
  if zone[0] == "-"
    o = o * (-1)
  end

  if (x.index("NOW") != nil)
    (y,z) = x.split(/-/)
    tz = 0
    if (z != nil)
      #d, h, m, s
      while (z[-1] == " ")
        z.chop!
      end
    
     
      tz = z[-1]
      z.chop!

      case tz
        when "M"
          z = z.to_i * 60
        when "H"
          z = z.to_i * 3600
        when "D"
          z = z.to_i * 86400
      end
      
      if ((z.to_i/INTERVAL)*INTERVAL != z)
        puts "All timestamps must fit the #{INTERVAL}s grid (#{x}/#{w}/#{z}). Aborting!"
        exit
      end
    end
    # Bestimme Startstempel
    (d,t,zone) = (Time.local(*$evaltime) - o - z.to_i - w).to_s.split(/ /)
    puts " Warp to #{$evaltime} - #{o} - #{z.to_i} - #{w} = #{t}" if DEBUG
    (t1,t2,t3) = t.split(/\:/)
    r = "#{d}T#{t1}:#{t2}"
  end
  return r
end

# 2016-05-31T07:00:00.000Z
def intDate(s)
  y  = s[0,4].to_i
  mo = s[5,2].to_i
  d  = s[8,2].to_i
  h  = s[11,2].to_i
  m  = s[14,2].to_i
  s  = s[17,2].to_i
  t = Time.local(y,mo,d,h,m,s)
  return t.to_i
end

class Service
  def initialize(s)
    @s = s
  end
  
  def to_s
    return @s.to_s
  end
  
  def to_i
    return @s.to_i
  end
  
  def set(s)
    @s = s
  end
  
  def to_f
    return @s.to_f
  end
  
  def get
    return @s
  end
end

class TermSym
  def initialize(x)
    self.set(x)
  end 
  
  def scoped
    return false
  end
  
  def clone
    c = TermSym.new(@s)
    return c
  end
  
  def getSize
    return 0
  end
  
  def set(x)
    @s = x
  end
  
  def clearTimeWarp
    # Dummy method
  end
  
  def timeWarp(t)
    # Dummy method
  end
  
  def setParent(p)
    @parent = p
  end
  
  
  def getParent
    return @parent
  end
  
  


  def to_s
    return @s.to_s
  end
  
  def evaluate
    r = @s
    if (@s.instance_of? IPAddr)
      i1 = @s.to_range.first
      i2 = @s.to_range.last
      r = '{ "gte": "' + "#{i1}" + '", "lte": "' + "#{i2}" + '"}'
    else
      case @s
      when "SRC"
        r = "SourceAddress"
      when "DST"
        r = "DestinationAddress"
      when "PROTO"
        r = "Protocol"
      when "PRT"
        r = "DestinationPort"
      when "FLAGS"
        r = "TCPFlags"
      end
    end
    return r
  end

end

class Evaluator < TermSym
  
  def initialize(x,scopeop,scope,s,e,y)
    #puts "New Evaulator: #{x}/#{scopeop}/#{scope}/#{s}/#{e}/#{y}" if DEBUG_PARSER
    self.set(x,scopeop, scope,s,e,y)
    @client = Elasticsearch::Client.new url: "#{ESHOST}", log: false #, transport_options: { request: { timeout: 3600 } }
    @defaults = 0
  end
  
  def defaults
    return @defaults
  end

  
  def clone
    c = Evaluator.new(@s.clone, @sd.clone, @ed.clone, @a.clone)
    return c
  end
  
  def set(x, scopeop, scope, s, e, y)
    if (!Evals.include?(x))
      puts "Illegal evaluator operator: #{x}"
      exit
    end
    @s = x
    @a = y
    @start = esdate(s,0)
    @ende = esdate(e,0)
    @scope = scope
    @scopeop = scopeop
    if (scopeop != "")
      @scoped = true
    else
      @scoped = false
    end
    @sd = s
    @ed = e
  end
  
  def scoped
    return @scoped
  end
  
  def timeWarp(t)
    # Turn eval time back by t seconds
    print "Time Warp by #{t}s at #{$evaltime}: (#{@sd})/(#{@ed})/#{@start}/#{@ende}" if DEBUG
    @start = esdate(@sd,t)
    @ende = esdate(@ed,t)
    puts "-> (#{@sd})/(#{@ed})/#{@start}/#{@ende}" if DEBUG
    @a.timeWarp(t)
  end
  
  def clearTimeWarp
    @start = esdate(@sd,0)
    @ende = esdate(@ed,0)
    @a.clearTimeWarp
  end
    
  
  def getSize
    return @a.getSize + 1
  end
  
  def getStart
    return @sd
  end
  
  def getEnd
    return @ed
  end
  
  def getOp
    return @s
  end
  
  def getTerm
    return @a
  end
  
  def to_s
    #return "(#{@s}#{@a.to_s}, [#{@sd} - #{@ed}] (Current eval time: #{@start} - #{@ende}))"
    return "(#{@s}#{@a.to_s})"
  end
  
  def evaluate
    postfilter = false
    lookup = false
    qry = @a.evaluate
    puts "--> Processing Query: #{qry} class:(#{qry.class}) s:(#{@s})" if DEBUG
 
    if (qry.class == String)
      qry2 = qry
    end
    
    filterscope_open  = ""
    filterscope_close = ""

    if (qry.class != String) && (qry[0] == "FOUND")
      if qry[0] == "LISTED"
        qry2 = qry[1]
      end
      if qry[0] == "FOUND"
        filterscope_open  = ',"aggregations": { "xscope": { "terms": { "field": "'+qry[1]+'", "size": '+"#{$scopesize}"+'  }'
        filterscope_close = '}}}'
        qry2 = "match_all: {}"
        postfilter = true
      end
    end
    
    puts "Scope class: #{@scope.class}" if DEBUG
    
    filterhash = nil
    if (@scope.class == Evaluator)
      myscope = '"0.0.0.0/0"'
      filterhash = @scope.evaluate
    else
      myscope = @scope
    end
    
    if (@scopeop != "")
      # SourceScope
      if (@scopeop == "SSCOPE")
        if (qry.class == String)
          qry2 = qry
          qry2 = qry2 + ',{ "match": { "SourceAddress": '+"#{myscope}"+' } }'
        else
          qry2 = '{ "match": { "SourceAddress": '+"#{myscope}"+' } }'
        end
      else
        # DesctinationScope
        if (qry.class == String)  
          qry2 = qry
          qry2 = qry2 + ',{ "match": { "DestinationAddress": '+"#{myscope}"+' } }'
        else
          qry2 = '{ "match": { "DestinationAddress": '+"#{myscope}"+' } }'
        end
      end
    end
    
    if (qry.class == String)
      # Argument is filter
      cnt = qry2.scan(/range/).size + qry2.scan(/match/).size 
    end
    
    myIndex = INDEX
    
    puts "scopeop: #{@scopeop}" if DEBUG
    
    if (@scopeop != "")
      # Scoped. Return Hash
      if (@scopeop == "SSCOPE")
        # Scope Source
        if (qry.class != String) && (qry[0] == "FOUND")
          qry3 = ""
        else
          qry3 = '"'+$op2+'": { "bool": { "should": [ ' +"#{qry2}"+ '], "minimum_should_match": ' + "#{cnt}"+ '} },'
        end
        case @s
        when "SUM"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "sum": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "COUNT"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "value_count": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "MIN"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "min": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "MAX"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "max": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "SCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "SourceAddress" }}}'+filterscope_close+'}}}}'
        when "DCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "DestinationAddress" }}}'+filterscope_close+'}}}}'
        when "PCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "SourceAddress", "size": '+"#{$scopesize}"+' }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "DestinationPort" }}}'+filterscope_close+'}}}}'
        end
      else
        # Scope Destination
        if (qry.class != String) && (qry[0] == "FOUND")
          qry3=""
        else
          qry3 = '"'+$op2+'": { "bool": { "should": [ ' +"#{qry2}"+ '], "minimum_should_match": ' + "#{cnt}"+ '} },'
        end
        case @s
        when "SUM"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "sum": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "COUNT"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "value_count": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "MIN"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "min": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "MAX"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "max": { "field": "Bytes" }}}'+filterscope_close+'}}}}'
        when "SCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "SourceAddress" }}}'+filterscope_close+'}}}}'
        when "DCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "DestinationAddress" }}}'+filterscope_close+'}}}}'
        when "PCARD"
          myQuery = '{ "size": 0, "query": { "'+$op1+'": { '+qry3+' "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "DestinationAddress", "size": '+"#{$scopesize}"+'  }'+filterscope_open+',"aggregations": { "myagg": { "cardinality": { "field": "DestinationPort" }}}'+filterscope_close+'}}}}'
        end     
      end
    else
      # Not scoped. Return single value
      case @s
      when "SUM"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "sum": { "field": "Bytes" }}}}'
      when "COUNT"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "value_count": { "field": "Bytes" }}}}'
      when "MIN"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "min": { "field": "Bytes" }}}}'
      when "MAX"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "max": { "field": "Bytes" }}}}'
      when "SCARD"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "cardinality": { "field": "SourceAddress" }}}}'
      when "DCARD"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "cardinality": { "field": "DestinationAddress" }}}}'
      when "PCARD"
        myQuery = '{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "bool": { "should": [ '+"#{qry2}"+'], "minimum_should_match": '+"#{cnt}"+'} }, "filter": { "range": {  "StartTime": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "myagg": { "cardinality": { "field": "DestinationPort" }}}}'
      when "LISTED"
        puts "LISTED qry: #{qry}, #{qry.class}" if DEBUG
        myIndex = qry[2]        
        myQuery = ('{ "size": 0, "query": { "'+$op1+'": { "'+$op2+'": { "match_all": {} }, "filter": { "range": {  "@timestamp": { "gte": "'+"#{@start}"+'", "lte": "'+"#{@ende}"+'" } } } }},"aggregations": { "scope": { "terms": { "field": "'+"#{qry[1]}"+'", "size": '+"#{$scopesize}"+'  },"aggregations": { "myagg": { "value_count": { "field": "'+"#{qry[1]}"+'" }}}}}}')
        lookup = true
      end
    end
    # myQuery = '{ "size": 0, "query": { "filtered": { "query":  { "bool": { "should": [ { "range": { "netflow.ipv4_src_addr": { "lte": "192.168.12.255", "gte": "192.168.12.0" } } } ] } } } } }'
    
    # puts "Query: #{myQuery} / Index: #{myIndex}" 
    
    if $cache[myQuery] == nil
      puts "Executing JSON Query: #{myQuery}" if DEBUG
      x = @client.search index: "#{myIndex}", body: myQuery #search_type: 'count', 
      puts "Done." if DEBUG
      puts "Query Result: #{x}" if DEBUG
      r = nil
      if x["aggregations"] != nil
        r = x["aggregations"]
      else
        r = Hash.new
      end
      $cache[myQuery] = r
    else
      r = $cache[myQuery]
    end
    
    if (@scopeop != "") || (lookup)
      # Sum single value for scoped queries
      n = Hash.new
      for j in r["scope"]["buckets"]
        puts "Scoping: #{j}" if DEBUG
        if postfilter
          puts "Postfilter" if DEBUG
          for k in j["xscope"]["buckets"]
            if qry[2][k[$ipfield]] != nil
              if n[j[$ipfield]] == nil
                n[j[$ipfield]] = k["myagg"]["value"].to_f
              else
                n[j[$ipfield]] = n[j[$ipfield]] + k["myagg"]["value"].to_f
              end                    
            end
          end
        else
          puts "no postfilter" if DEBUG
          if n[j[$ipfield]] == nil
            n[j[$ipfield]] = j["myagg"]["value"].to_f
          else
            n[j[$ipfield]] = n[j[$ipfield]] + j["myagg"]["value"].to_f
          end
        end
      end
    else
      # Sum per IP for unscoped queries
      n =r["myagg"]["value"].to_f
    end
    if (@scope.class == Evaluator)
      n2 = n
      n = Hash.new
      for x in n2
        if (filterhash.include?(x[0]) )
          n[x[0]] = x[1]
        end
      end
    end
    puts "===> Query: #{myQuery}, Time: #{@start}-#{@ende}, Result: #{n}, Scopeop: #{@scopeop}" if DEBUG
    return n
  end
  
end



class BinaryOp < TermSym
  def initialize(x,y,z)
    self.setLeft(y)
    self.setRight(z)
    self.setOp(x)
    @defaults = 0
  end 
  
  def defaults
    return @defaults
  end
  
  def clone
    c = BinaryOp.new(@s.clone, @a.clone, @b.clone)
    return c
  end
  
  def scoped
    if (self.getLeft.scoped || self.getRight.scoped)
      r = true
    else
      r = false
    end
    return r
  end
  
  def timeWarp(t)
    puts "#{self.getLeft} <- #{self.getOp} -> #{self.getRight}" if DEBUG
    self.getLeft.timeWarp(t)
    self.getRight.timeWarp(t)
  end
  
  def clearTimeWarp
    self.getLeft.clearTimeWarp
    self.getRight.clearTimeWarp
  end
  
  
  def getLeft
    return @a
  end

  def getRight
    return @b
  end
  
  def getOp
    return @s
  end
  
  def getSize
    if (@s != "MATCHES")
      i = 1
    else
      i = 0
    end
    return @a.getSize + @b.getSize + i
  end
  
  
  def setOp(x)
    if (!BinOp.include?(x))
      puts "Illegal binary operator: <#{x}> in #{self}. Not included in <#{BinOp}>"
      exit
    end
    @s = x
  end
  
  def setLeft(y)
    @a = y
  end
  
  def setRight(z)
    @b = z
  end
  
  
  def to_s
    return "(#{@a.to_s} #{@s} #{@b.to_s})"
  end
  
  def evaluate
    puts "Evaluation Start: #{@a} <- #{@s} -> #{@b}" if DEBUG
    r = -1
    x = @a.evaluate
    y = @b.evaluate
    case @s
    when "MATCHES"
      op = ""
      if (@a != nil && x.instance_of?(String) && x.include?("ADDRESS"))
        op = "range"
      else
        op = "match"
      end
      r = '{ "'+op+'": { "'+"#{@a.evaluate}"+'": '+"#{@b.evaluate}"+' } }'
    
    when "FOUND"
      r = Array.new
      r.push("FOUND")
      r.push(x)
      r.push(y)
    
    when "AS"
      # LISTED host AS MALWARE
      #        x       y
      r = Array.new
      r.push("AS")
      r.push(x.to_s.downcase.gsub(/"/,""))
      r.push(y.to_s.downcase.gsub(/"/,""))
      puts "AS: #{r}, #{r.class}" if DEBUG
    
    when "AND"
      if (x.instance_of?(Hash) && y.instance_of?(Hash) )
        r = Hash.new
        keys = x.keys + y.keys
        keys.uniq!
        for z in keys
          if x[z] == nil
            x[z]= @a.defaults
          end
          if y[z] == nil
            y[z]= @b.defaults
          end
        end
        for key in keys
          r[key] = x[key].to_i & y[key].to_i
        end       
      elsif (x.instance_of?(String) || y.instance_of?(String))
        r = "#{x},#{y}"
      else
        r = "#{x & y}"
      end
    when "OR"
      if (x.instance_of?(Hash) && y.instance_of?(Hash) )
        r = Hash.new
        keys = x.keys + y.keys
        keys.uniq!
        for z in keys
          if x[z] == nil
            x[z]= @a.defaults
          end
          if y[z] == nil
            y[z]= @b.defaults
          end
        end
        for key in keys
          r[key] = x[key].to_i | y[key].to_i
        end           
      elsif (x.instance_of?(String) || y.instance_of?(String))
        r = "#{x},#{y}"
      else
        r = "#{x | y}"
      end
    when "="
      if (x.instance_of?(Hash))
        r = Hash.new
        for key in x.keys
          r[key] = x[key].to_f == y.to_f
          if r[key] == true 
            r[key] = 1
          else
            r[key] = 0
          end          
        end 
        @defaults = 1
      else
        if (x == y)
          r = 1
        else
          r = 0
        end
      end
    when ">"
      if (x.instance_of?(Hash))
        r = Hash.new
        for key in x.keys
          r[key] = x[key].to_f > y.to_f
          if r[key] == true 
            r[key] = 1
          else
            r[key] = 0
          end          
        end 
        if (y.to_f >= 0)
          @defaults = 0
        else
          @defaults = 1
        end
      else
        r2 = x.to_f > y.to_f
        if (r2 == true)
          r = 1
        else
          r = 0
        end
      end
    when "<"
       if (x.instance_of?(Hash))
        r = Hash.new
        for key in x.keys
          r[key] = x[key].to_f < y.to_f
          if r[key] == true 
            r[key] = 1
          else
            r[key] = 0
          end
        end 
        if (y.to_f > 0)
          @defaults = 1
        else
          @defaults = 0
        end
      else     
       r2 = x.to_f < y.to_f
        if (r2 == true)
         r = 1
       else
         r = 0
        end
      end
    when "/"
      if (x.instance_of?(Hash) && y.instance_of?(Hash))
        r = Hash.new
        keys = x.keys + y.keys
        keys.uniq!
        for z in keys
          if x[z] == nil
            x[z]= @a.defaults
          end
          if y[z] == nil
            y[z]= @b.defaults
          end
        end
        for key in keys
          r[key] = x[key].to_f / y[key].to_f
        end
      elsif (x.instance_of?(Hash))
        r = Hash.new
        for key in x.keys
          r[key] = x[key].to_f / y.to_f
        end
      else
        r = x.to_f / y.to_f
      end
    when "*"
      if (x.instance_of?(Hash))
        keys = x.keys & y.keys
        keys.uniq!
        r = Hash.new        
        for key in keys
          r[key] = x[key].to_f * y[key].to_f
        end
      else
        r = x.to_f * y.to_f
      end
    when "+"
      if (x.instance_of?(Hash) && y.instance_of?(Hash))
        keys = x.keys + y.keys
        keys.uniq!
        for z in keys
          if x[z] == nil
            x[z]= @a.defaults
          end
          if y[z] == nil
            y[z]= @b.defaults
          end
        end
        r = Hash.new        
        for key in keys
          r[key] = x[key].to_f + y[key].to_f
        end
      elsif (x.instance_of?(Hash))
        r = Hash.new
        for key in x.keys
          r[key] = x[key].to_f + y.to_f
        end        
      else
        r = x.to_f + y.to_f
      end
    when "-"
      if (x.instance_of?(Hash))
        keys = x.keys + y.keys
        keys.uniq!
        for z in keys
          if x[z] == nil
            x[z]= 0
          end
          if y[z] == nil
            y[z]= 0
          end
        end
        r = Hash.new        
        for key in keys
          r[key] = x[key].to_f - y[key].to_f
        end
      else
        r = x.to_f - y.to_f
      end
    end
    puts "Evaluation Result: #{@a} <- #{@s} -> #{@b} ==> #{r}" if DEBUG
    return r
  end
end


class Parser
  def initialize(l)
    @sym = Array.new
    self.read(l)
  end
  
  def read(l)
    puts "Line: #{l}" if DEBUG
    # Zerlegen: Split für alle möglichen Symbole
    sources = Array.new
    sources.push(l.delete(" "))
    keywords = ["(",")",","] + BinOp + Evals + Key + Scope
    for x in keywords # versuche, an x zu teilen
      symbols = Array.new
      for y in sources # y ist aktueller substring
        c = 0
        if y.include?(x)
          t = y.split(x)
          puts "Splitted #{y} by #{x} to #{t}" if DEBUG
          # Repariere geklammerte Ausdrücke
          u = 0
          repaired = 0
          while u < t.size
            if (t[u].count('"').odd?)
              v = u + 1
              while t[u].count('""').odd?
                t[u] = t[u]+x+t[v]
                t.delete_at(v)
                repaired += 1
              end
            end
            u += 1
          end
          #
          for i in 0..t.size-1
            if t[i] != ""
              symbols.push(t[i])
            end
            if i < t.size-1
              symbols.push(x)
              c += 1
            end
          end
          while (c + repaired < (y.scan(x).count) )
            symbols.push(x)
            c += 1
          end
        else # non-splittable
          symbols.push(y)
        end # splittable
        symbols.flatten!
      end # substrung
      sources = symbols
    end # Versuche, an x zu teilen
    
    puts "Symbols: #{symbols}" if DEBUG_PARSER
    @sym = self.eval(symbols)
    puts "Evaluated: #{@sym}" if DEBUG_PARSER
  end 
  
  
  def eval(n)

    puts "#{n}: Evaluating." if DEBUG_PARSER
    r = nil
    c = 0
    l = 0
    
    # Umfassende Klammern entfernen
    gap = false
    while !gap
      if (n[0] == "(") && (n[-1] == ")")
        i = 1
        gap = false
        while i < n.size-1
          if n[i] == "("
            c += 1
          end
          if n[i] == ")"
            c -= 1
          end
          if c < 0
            gap = true
          end
          i+=1
        end
        if !gap
          n = n[1,n.size-2]
        end
      else
        gap = true
      end
    end

    puts "#{n}: Brackets removed: #{n}" if DEBUG_PARSER
    
    if (n.size == 1)
      x = n[0]
      begin
        x = IPAddr.new(n[0])
      rescue
        x = n[0]
      end
      begin
        if (x.to_i.to_s == x)
          j = x.to_i
        elsif (x.to_f.to_s == x)
          j = x.to_f
        else
          j = x
        end
      rescue
      end
      puts "#{n}: Terminal symbol added: #{j}" if DEBUG_PARSER
      r = TermSym.new(j)
    elsif !(Evals.include?(n[0]))
      c = 0
      while l < n.size
        if (n[l] == "(")
          c += 1
        end
        if (n[l] == ")")
          c -= 1
        end
        if c == 0 && BinOp.include?(n[l])
          puts "#{n}: Preparing binary operator: #{n[0,l]} and #{n[l+1, n.size]}" if DEBUG_PARSER
          a = eval(n[0,l])
          b = eval(n[l+1, n.size])
          if (a == "PRT")
            b2 = Service.new(b)
          else
            b2 = b
          end

          puts "#{n}: ARG L: #{a}/#{a.class} ARG_R: #{b2}/#{b2.class}" if DEBUG_PARSER
          r = BinaryOp.new(n[l], a, b2)
          puts "#{n}: Binary Operator added: OP: #{n[l]}, Left ARG: #{a}, Right ARG: #{b2}" if DEBUG_PARSER
        end
        l += 1
      end # while
     else       
      if Scope.include?(n[1])
        # ["COUNT", "DSCOPE", "\"0.0.0.0/\"", ",", "\"NOW-30D\"", ",", "\"NOW\"", ",", "SRC", "MATCHES", "\"10.0.0.0/8\""]
        puts "#{n}: Evaluating Scope Operator" if DEBUG
        if (n[2] == "LISTED")
          # ["COUNT", "DSCOPE", "LISTED", "\"NOW-30D\"", ",", "\"NOW\"", ",", "\"IP\"", "AS", "\"TEST-MALWARE\"", ",", "\"NOW-30D\"", ",", "\"NOW\"", ",", "SRC", "MATCHES", "\"10.0.0.0/8\""]
          puts "Listed scope" if DEBUG
          a = eval(n[15,n.size])
          n[3].delete!('"')  if n[3].include?('"')
          n[5].delete!('"')  if n[5].include?('"')
          n[11].delete!('"') if n[11].include?('"')
          n[13].delete!('"') if n[13].include?('"') 
          puts "Defining AS operator" if DEBUG
          arg1 = TermSym.new(n[7])
          arg2 = TermSym.new(n[9])
          op   = n[8]
          scopea = BinaryOp.new(op,arg1,arg2)
          puts "Scopeop: #{scopea.getLeft.class} #{scopea.getRight.class}" if DEBUG
          puts "Defining LISTED evaluator" if DEBUG
          scope  = Evaluator.new(n[2], "", "", n[3], n[5], scopea)
          r      = Evaluator.new(n[0], n[1], scope, n[11], n[13], a )
          puts "#{n}: Evaluator symbol with listed scope added: #{n[0]} #{n[1]} #{scope}: #{n[11]}: #{n[13]} : #{a}" if DEBUG_PARSER       
        else
          a = eval(n[8,n.size])
          n[4].delete!('"') if n[4].include?('"')
          n[6].delete!('"') if n[6].include?('"') 
          r = Evaluator.new(n[0], n[1], n[2], n[4], n[6], a )
          puts "#{n}: Evaluator symbol added: #{n[0]} #{n[1]} #{n[2]}: #{n[4]}: #{n[6]} : #{a}" if DEBUG_PARSER
        end
      else
        puts "#{n}: Evaluating Evaluator" if DEBUG
        a = eval(n[5,n.size])
        n[1].delete!('"') if n[1].include?('"')
        n[3].delete!('"') if n[3].include?('"')
        r = Evaluator.new(n[0], "", "", n[1], n[3], a)
        puts "#{n}: Evaluator symbol added: #{n[0]} : #{n[1]}: #{n[3]} : #{a}" if DEBUG_PARSER
      end
    end # if
    puts "#{n}: Returning from #{n}: #{r}" if DEBUG_PARSER
    return r
  end
  
  def get
    return @sym
  end
  
  def to_s
    return @sym.evaluate
  end
end


# MAIN: 
#
# Check for ES Version. 2.x stores IP addresses in field "key_as_string, 5.x used "key".
#


$evaltime = Time.at((Time.now.to_i/INTERVAL)*INTERVAL).to_a

client = Elasticsearch::Client.new url: "#{ESHOST}", log: false #, transport_options: { request: { timeout: 3600 } }


info = client.info["version"]["number"].split(/\./)[0].to_i

case info
when 2
  $ipfield = "key_as_string"
  $op2 = "query"
  $op1 = "filtered"
  $scopesize = 0
when 5
  $ipfield = "key"
  $op2 = "must"
  $op1 = "bool"
  $scopesize = SCOPESIZE
else
  puts "Unsupported ES major version: #{info}"
  exit
end

puts "Version: #{info}"
