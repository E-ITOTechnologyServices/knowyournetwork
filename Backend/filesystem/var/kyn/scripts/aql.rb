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
# # Abfrage des netflow5-Indexes in Elasticsearch mit logischen Ausdrücken
# #
# #
# ###
# 
# ### Grammatik:
# # t -> ( t BINOP t ) | ( EVAL t ) | "SRC" | "DST" | "PROTO" | "PORT" | <const>
# # BINOP -> "MATCHES" | ">" | "<" | "=" | "AND" | "OR" | "+" | "-" | "*"
# # EVAL -> EVAL_OP starttime, endtime, t
# # EVAL_OP -> "SUM" | "COUNT"
# ###
#
# Die Operatoren "MATCHES" und "EVAL" verknüpfen den logischen Ausdruck mit den Daten im Index. "Matches" definiert den Filter, "Eval" die Aggregation (zu aggregierendes Feld und Zeitraum). 
#
# ### Beispiele:
# # t = ((BYTES((SRC MATCHES 192.168.12.0/24) AND (DST MATCHES 0.0.0.0/0)), [2016-04-05T08:05 - 2016-04-05T08:20]) * 2)
# # Ergebnis: Zweifache der aus dem Netz 192.168.12.0/24 übertragenen Bytes am 05.04.16 im Zeitraum von 08:05 bis 08:20.
# #
# # t = ((BYTES((SRC MATCHES 192.168.12.0) AND (DST MATCHES 0.0.0.0)), [2016-04-05T08:33 - 2016-04-05T08:48]) + (BYTES(DST MATCHES 192.168.12.0), [2016-04-05T08:33 - 2016-04-05T08:48]))
# # Ergebnis: Summe der aus dem Netz 192.168.12.0/24 und in das Netz 192.168.12.0/24 übertragenen Bytes am 05.04.16 im Zeitraum von 08:33 bis 08:48.
# ###
#
# ### To Do:
# # 
# #
# #
# ###

require "./kestrel.rb"
require "elasticsearch"
require 'openssl'
require 'ipaddr' 
require "getoptlong"


OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE


ESDEBUG = false
VERSION = "2016-09-12"

opts = GetoptLong.new(
  ["--help", "-?", GetoptLong::NO_ARGUMENT],
  ["--term", "-t", GetoptLong::REQUIRED_ARGUMENT],
  ["--host", "-h", GetoptLong::REQUIRED_ARGUMENT],  
  ["--esdebug", "-e", GetoptLong::NO_ARGUMENT],    
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT]  
)

term = ""
$index = INDEX
eshost = ESHOST
esdebug = ESDEBUG
opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
  aql.rb Version #{VERSION} | Syntax: 
  --term     Evalute given term and display result
  --index    Elasticsearch Index. Default: #{INDEX}
  --host     Elasticsearch Host. Default: #{ESHOST}
  --esdebug  Elasticsearch debug output. Default: #{ESDEBUG}
    EOF
    exit 0
  when "--term"
    term = arg
  when "--host"
    eshost = arg
  when "--index"
    $index = arg
  when "--esdebug"
    esdebug = true
  end
end


$client = Elasticsearch::Client.new url: "#{eshost}", log: "#{esdebug}" #, transport_options: { request: { timeout: 3600 } }


# Bestimme Zeitzones-Offset
(d,t,zone) = Time.now.to_s.split(/ /)
o = zone[1].to_i * 36000 + zone[2].to_i * 3600 + zone[3].to_i * 600 + zone[4].to_i * 60
if zone[0] == "-"
  o = o * (-1)
end

# Bestimme Startstempel
(d,t,zone) = (Time.now - o - SLICE).to_s.split(/ /)
(t1,t2,t3) = t.split(/\:/)
d_start = "#{d}T#{t1}:#{t2}"

# Endstempel Startstempel
(d,t,zone) = (Time.now - o).to_s.split(/ /)
(t1,t2,t3) = t.split(/\:/)
d_end = "#{d}T#{t1}:#{t2}"

if (term == "")
  puts "Anomaly Query Language"
  puts "===================================================================================================================="
  puts "Syntax:"
  puts '    t -> ( t BINOP t ) | ( EVAL t ) | "SRC" | "DST" | "PROTO" | "PRT" | <const>'
  puts '    BINOP -> "MATCHES" | ">" | "<" | "=" | "AND" | "OR" | "+" | "-" | "*"'
  puts '    EVAL -> EVAL_OP, starttime, endtime'
  puts '    EVAL_OP -> "SUM" | "COUNT" | "MIN" | "MAX" | "SCARD" | "DCARD"'
  puts
  puts "Zeitstempel fuer Copy & Paste:"
  puts '"' + "#{d_start}" + '"'
  puts '"' + "#{d_end}" + '"'
  puts
  puts "===================================================================================================================="
  puts "Beispielzeilen:"
  puts "- Datenvolumen aus Netz 192.168.12.0/24 ins Netz 10.0.0.0/8 innerhalb der letzen 15 Minuten."
  puts 'SUM "now-15m", "now",((SRC MATCHES "192.168.12.0/24") AND (DST MATCHES "10.0.0.0/8"))'
  puts " - Anzahl unterschieldlicher Ziele für Quelle 110.1.45.38/32 innerhalb des letzten Tages"
  puts 'DCARD "now-1d", "now", SRC MATCHES "110.1.45.38/32"'
  puts " - Durchschnittliche Bytes/Session für Port 80 in den letzten 15 Minuten"
  puts '(SUM "now-15m", "now", PRT MATCHES 80)/(COUNT "now-15m", "now", PRT MATCHES 80)'
  puts " - Mehr Sessions auf Port 53 als auf Port 80 in den letzten #{SLICE} Sekunden?"
  puts '(COUNT "' + "#{d_start}" +'", "'+ "#{d_end}" +'",PRT MATCHES 53) > (COUNT "' + "#{d_start}" +'", "'+ "#{d_end}" +'",PRT MATCHES 80)'
  puts " - Bytes aus den Netzen 192.168.12.0/24 und 192.168.13.0/24 in den letzten #{SLICE} Sekunden"
  puts 'SUM "' + "#{d_start}" +'", "'+ "#{d_end}" +'",((SRC MATCHES "192.168.12.0/24") OR (SRC MATCHES "192.168.13.0/24"))'
  puts "===================================================================================================================="
  puts '"quit" beendet das Programm.'

  print "> "
  while l = gets.chomp!
    puts l
    l.upcase!
    if (l=="QUIT")
      exit
    end
   # begin
      t1 = Parser.new(l)
      puts t1.to_s
  #  rescue
   #  puts "Syntax error"
  #  end
    print "> "
  end
else
  t1 = Parser.new(term.upcase)
  puts t1.to_s
end

