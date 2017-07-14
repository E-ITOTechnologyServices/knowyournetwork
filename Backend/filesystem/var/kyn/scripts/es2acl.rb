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



require "date"
require "getoptlong"
require "pathname"
require "json"
require "elasticsearch"
require 'openssl'

OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE

VERSION    = "20170130-01"
BASERULES  = "base-rules.acl"
NEWRULES   = "new-rules.acl"
POLICY     = "kyn-policy"
IPADMIN    = "http://network-tools.com/default.asp?prog=express&host="

MAX_NET = 7     # Maximum number of additional subnet bits during superneting
MAX_GRP = 32    # Maximum number of additional communications opened during grouping
MAX_PRT = 16    # Number of ports that will be replaced by "any"
MAX_INET = 16   # Number of sources/destination that will be replaced with "any"
ANY = -1        # Constant used to denote "any" port
IP  = -1

$start_time = Time.now
$debug_file = nil
$debug = true
$digit = 1
$xdebug = true

$groupnum  = 1
$sgroupnum = 1
$maxnet = MAX_NET

$id = 0

$any = {}
$ngroups = []
$sgroups = []
$nnames = {}
$aclname = "ACL_OFFICE"

$outside_interfaces = {}
$unknownexporter = {}

# Facilitate performance measurement
class Profiler
	def initialize
		@t1 = 0
		@t2 = 0
		@t = 0
		@c = 0
	end

	def start
		@t1 = Time.now
		@c = @c + 1
	end

	def stop
		@t2 = Time.now
		@t = @t + (@t2 - @t1)
	end

	def getCount
		return @c
	end

	def getTotal
		return @t.round(5)
	end

	def getAvg
		return (@t/@c).round(5)
	end

end

# extend Integer class
class Integer
  # Takes bytes
  def toFilesize
    {
      " B"  => 1024,
      " KB" => 1024 * 1024,
      " MB" => 1024 * 1024 * 1024,
      " GB" => 1024 * 1024 * 1024 * 1024,
      " TB" => 1024 * 1024 * 1024 * 1024 * 1024
    }.each_pair { |e, s| return "#{(self.to_f / (s / 1024)).round(1)}#{e}" if self < s }
  end

  def toDots
    self.to_s.reverse.gsub(/(\d{3})(?=\d)/,  '\\1,').reverse
  end
end

# extend String class
class String
  def is_i?
    /\A[-+]?\d+\z/ === self
  end
end

#
# A Service has a Protocol and a Port
#
class Service
  def initialize
    @port = 0
    @name = ""
    @dcache = Hash.new
  end

  def setName(s)
    @name = s
  end

  def getName
    @name
  end

  def toCisco
    if (@port == ANY)
      r = "eq any"
    else
      r = "eq #{@port}"
    end
    return r
  end

  def toJSON
    p = @port
    if (@port == ANY)
      p = "ANY"
    end
    j = { "Port" => p, "Protocol" => "#{@proto}" }
    return j
  end

  def distance(d)
    ret = 0
    if d.instance_of?(Service)
        ret = 1 if ((@port != d.getPort) || (@proto == d.getProto))
    else
      #$t31.start
      if @dcache[d] == nil
        ret = d.distance(self)
        @dcache[d] = ret
      else
        ret = @dcache[d]
      end
      #$t31.stop
    end
    return ret
  end

  def setPort(p)
    @port = p
  end

  def getPort
    @port
  end

  def setProto(p)
    @proto = p.to_s #<-
  end

  def getProto
    @proto
  end

  def to_s
    r = "#{@port}/#{@proto}"
    r = "ANY/#{@proto}" if @port == ANY
    return r
  end

  def equals(x)
    r = 0
    r = 1 if (x.instance_of?(Service)) && (@port == x.getPort) && (@proto == x.getProto)
    return r
  end

  def matches(x) # Does this Service match/include Service x?
    r = 0
    #$t5.start
    #$debug_file.puts "Service match proto: #{@proto} #{@proto.class} #{x.getProto} #{x.getProto.class}" if $debug
    if (@proto == "ip") || (@proto == x.getProto)
      r = 1 if @port == ANY
      if x.instance_of?(Service)
        # compare with Service
        #$debug_file.puts "Service match port: #{@port} #{@port.class} #{x.getPort} #{x.getPort.class}" if $debug
        r = 1 if @port == x.getPort
      end
    end
    #$t5.stop
    return r
  end

  def getSize
    g = 1
    g = 65_536 if @port == ANY
    return g
  end
end

#
# Like services but with a range of ports like 5900-5950
#
class PortRange < Service
  def initialize
    @start_port = 0
    @end_port = 0
    @proto = ""
    @name = ""
  end

  def toCisco
    r = "object-group service g-Service-#{$sgroupnum} #{@svc.getProto}\n"
    @svc.getPort.each do |_p|
      r += "port-object range #{@start_port} #{@end_port}\n"
    end
    r
  end

  def toJSON
    #j = { "Range" => { "Proto" => @proto, "Start" => @start_port, "End" => @end_port }}
    j = {"Port" => "#{@start_port}-#{@end_port}", "Protocol" => "#{@proto}"}
    return j
  end

  def distance(d)
    ret = 0
    if d.instance_of?(Service)
      if (@start_port <= d.getPort && @end_port >= d.getPort && d.getPort == getProto)
        ret = 0
      else
        ret = 1
      end
    elsif d.instance_of?(PortRange)
      if (@end_port < d.getStartPort) || (@start_port > d.getEndPort)
        r = getSize + d.getSize
      else
        # included completely?
        if (@start_port >= d.getStartPort) && (@end_port <= d.getEndPort)
          r = (@start_port - d.getStartPort) + (d.getEndPort - @end_port)
        end
        # d in self
        if (@start_port <= d.getStartPort) && (@end_port >= d.getEndPort)
          r = (d.getStartPort - @start_port) + (@end_port - d.getEndPort)
        end
        # d starts before
        if (d.getStartPort < @start_port) && (d.getEndport < @EndPort)
          r = (@start_port - d.getStartPort) + (@end_port - d.getEndPort)
        end
        # d ends later
        if (@start_port < d.getStartPort) && (@EndPort < d.getEndPort)
          r = (d.getStartPort - @start_port) + (d.getEndPort - @end_port)
        end
      end
    else
      # Argument is Service Group
      d.getService.each do |x|
        r += distance(x)
      end
    end
    return ret
  end

  def getPort
    "#{@start_port}-#{@end_port}"
  end

  def setStartPort(p)
    @start_port = p
  end

  def getStartPort
    @start_port
  end

  def setEndPort(p)
    @end_port = p
  end

  def getEndPort
    @end_port
  end

  def setProto(p)
    @proto = p.to_s #<-
  end

  def getProto
    @proto
  end

  def to_s
    r = "#{@start_port}-#{@end_port}/#{@proto}"
    r
  end

  def equals(x)
    r = 0
    if x.instance_of?(PortRange)
      if (@StartPort == x.getStartPort) && (@EndPort == x.getEndPort) && (@proto == x.getProto)
        r = 1
      end
    end
    r
  end

  def matches(x) # Does this Service match/include Service x?
    ret = 0
    if (@proto == x.getProto) || (@proto == "ip")
      if x.instance_of?(Service)
        # compare with Service
        # puts "DEBUG: #{@start_port} #{@end_port} #{x.getPort} #{@start_port.class} #{@end_port.class} #{x.getPort.class}"
        ret = 1 if (@start_port <= x.getPort) && (@end_port >= x.getPort)
      end
      if x.instance_of?(ServiceGroup)
        x.getService.each do |k|
          ret = 1 if matches(k) == 1
        end
      end
      if x.instance_of?(PortRange)
        if @proto == x.getProto && @start_port <= x.getStartPort && @end_port >= x.getEndPort
          ret = 1
        end
      end
    end
    return ret
  end

  def getSize
    @end_port - @start_port
  end
end

#
# Service Groups can have Service, PortRange or other ServiceGroup as childs
#
class ServiceGroup < Service
  def initialize
    super
    @svc = []
    @childs = []
    @proto = nil
    @name = "g-service-" + $sgroupnum.to_s
    @description = ""
    $sgroupnum += 1
  end

  def self.all
    ObjectSpace.each_object(self).to_a
  end

  def toCisco
    r = "no object-group service #{getName}\n"
    r += "object-group service #{getName}\n"
    @svc.each do |p|
      if p.instance_of?(PortRange)
        r += " #{p.getProto} range #{p.getStartPort} #{p.getEndPort}\n"
      else
        if (p.getPort == ANY)
          r += " #{p.getProto} eq any\n"
        else
          r += " #{p.getProto} eq #{p.getPort}\n"
        end
      end
    end
    r += "!"
    r
  end

  def toTable
    r = "<tr><td>service</td><td>#{getName}</td><td>"
    @svc.each do |p|
      if p.instance_of?(PortRange)
        r += "#{p.getProto} range #{p.getStartPort} #{p.getEndPort}<br />"
      else
        r += "#{p.getProto} eq #{p.getPort}<br />"
      end
    end
    r += "</td></tr>"
    r
  end

  def flat
    flatarray = []
    unless @svc.nil?
      @svc.each do |i|
        if i.instance_of?(ServiceGroup)
          flatarray.concat(i.flat)
        else
          flatarray.push(i)
        end
      end
    end
    flatarray
  end

  def toCiscoFlat
    r = "no object-group service #{getName}\n"
    r += "object-group service #{getName}\n"
    flat.each do |p|
      if p.instance_of?(PortRange)
        r += " #{p.getProto} range #{p.getStartPort} #{p.getEndPort}\n"
      else
        r += " #{p.getProto} eq #{p.getPort}\n"
      end
    end
    r += "!"
    r
  end

  def distance(d)
    r = 0
    @svc.each do |i|
      r += i.distance(d)
    end
    return r
  end

  def getPort
    r = []
    @svc.each do |u|
      if u.getPort.instance_of?(Array)
        r.concat(u.getPort)
      else
        r << u.getPort
      end
    end
    return r
  end

  def addService(p)
    if !p.instance_of?(ServiceGroup)
      match = 0
      @svc.each do |x|
        if x.equals(p) == 1
          match = 1
        end
      end
      if match == 0
        if (!$any[@proto].nil? && (p.matches($any[@proto]) == 1))
          puts "ANY: #{p} is #{$any[@proto]}"
          @svc = Array.new
          @svc.push(p)
        else
          @svc.push(p)
        end
      end
    else
      found = false
      p.getService.each do |d|
        @svc.each do |e|
          found = true if d.equals(e) == 1
        end
        #@svc.push(d) if found == false
        self.addService(d) if found == false
        found = false
      end
    end
  end

  def getSize
    r = 0
    @svc.each do |h|
      r += h.getSize
    end
    return r
  end

  def setDesc(n)
    @description = n
  end

  def getDesc
    @description
  end

  def getService
    @svc
  end

  def addChild(n)
    @childs.push(n) unless @childs.include?(n)
  end

  def getChild
    @childs.sort! { |a, b| a.to_s.downcase <=> b.to_s.downcase }
  end

  def to_s
    r = nil
    if !$any[@proto].nil? && (matches($any[@proto]) == 1)
      r = "ANY/#{@proto}"
    else
      r = ""
      @svc.sort_by! { |s| s.getPort.to_i }.each do |x|
        r += " " + x.to_s
      end
    end
    # any returns nil
    return r
  end


  def toJSON
    q = nil
    if !$any[@proto].nil? && (matches($any[@proto]) == 1)
      q = { "name" => "", "members" => [ "Port" => "ANY", "Protocol" => "#{@proto}" ]}
    else
      d = Array.new
      @svc.sort_by! { |s| s.getPort.to_i }.each do |x|
        p = x.toJSON
        d.push(p)
      end
      q = { "name" => self.getName, "members" => d }
    end
    return q
  end

  def equals(x)
    r = 0
    r = 1 if ( (matches(x) == 1) && (x.matches(self) == 1) )
    return r
  end

  def matches(x) # Does this Service match/include Service x?
    r = 0
    $debug_file.puts "Service Group #{getService} matches #{x}?" if $debug
    @svc.each do |y|
      $debug_file.print "Service Group member #{y} (#{y.class}) matches #{x}? " if $debug
      if y.matches(x) == 1
        r = 1
        $debug_file.puts "Services do not match!" if $debug
      else
        $debug_file.puts "Services match!" if $debug
      end
    end
    return r
  end

  def toHtml(n, bg, fg)
    r = ""
    lgroup = ServiceGroup.all.select { |x| x.getName == n }[0]
    if lgroup.nil?
      r += "<li class='node' style='padding-left:10px;'><span class='leaf fg-white'>"
      port = n.getPort
      port = "ANY" if port == "-1"
      if n.getProto.is_i?
        r += "<img class=icon src='icons/ip_service.png' /> #{port}"
      else
        r += "<img class=icon src='icons/#{n.getProto}_service.png' /> #{port}"
      end
      r += "</span></li>"
    else
      if lgroup.getChild.any?
        r += "<li class='node collapsed' style='padding-left:10px;'><span class='leaf fg-#{fg}'><img src='icons/service_group.png' class='icon' /> #{lgroup.getName}</span>"
        r += "<ul>"
        r += "<div class='popover marker-on-top' style='background-color:##{lightenColor(bg)}; padding:unset;padding-top:0.25rem;'>"
        lgroup.getChild.each do |k|
          r += lgroup.toHtml(k, lightenColor(bg), "white")
        end
        r += "</div></ul></li>"
      else
        r += "<li class='node'><span class='leaf fg-white'><img src='icons/service_group.png' class='icon' /> #{lgroup.getName}</span></li>"
      end
    end
    r
  end

  def lightenColor(hex_color, amount = 0.05)
    rgb = hex_color.scan(/../).map { |color| color.hex}
    rgb[0] = [(rgb[0].to_i + 255 * amount).round, 255].min
    rgb[1] = [(rgb[1].to_i + 255 * amount).round, 255].min
    rgb[2] = [(rgb[2].to_i + 255 * amount).round, 255].min
    "%02x%02x%02x" % rgb
  end
end

#
# Single IP address object
# ALL IPs need to have a netmask!
#
class Ip
  def initialize(i, m)
    setaddr(i)
    setmask(m)
    update
    setName("")
  end

  def <=>(other)
    if other.instance_of?(Group)
      r = 0
    else
      r = self.to_i <=> other.to_i
    end
    r
  end

  def setName(n)
    @name = n
  end

  def clearName
    @name = ""
  end

  def getName
    if (@name == "")
      n = @addr_o.join(".") + "/" + maskCidr
    else
      n = @name
    end
    n
  end

  def getCIDR
    @addr_o.join(".") + "/" + maskCidr
  end

  def equals(i)
    (contains(i) && (i.contains(self)))
  end

  def supernet
    0
  end

  # ALT
  def distanceNet(i)
    # Calculates distance for superneting
    r = 0
    if i.instance_of?(Ip)
      if maskInt > i.maskInt
        m = i.maskInt
      else
        m = maskInt
      end
      a1 = self.to_i & m
      a2 = i.to_i & m
      if  a1 ^ a2 > 0
        r = Math.log2(a1 ^ a2).ceil
      else
        r = 0
      end
    else
      i.getIps.each do |ip|
        r += distanceNet(ip)
      end
    end
    return r
  end

  def distanceGroup(i)
    dist = 0
    #$t4.start
    if i.instance_of?(Group)
      #$t4.stop
      #$t2.start
      i.getIps.each do |d|
        dist += (size + d.size) if equals(d) == 0
      end
      #$t2.stop
    else
      #$t4.stop
      #$t2.start
      dist = (size + i.size) if equals(i) == 0
      #$t2.stop
    end
    return dist
  end

  def setaddr(i)
    @addr_o = []
    @addr_o = i.split(".")
    update
  end

  def setmask(m)
    @mask_o = []
    @mask_o = m.split(".")
    update
  end

  def spawn
    Ip.new(to_s, maskString)
  end

  # Get IP in various formats
  def to_s
    getName
  end

  def toCisco
    @addr_o.join(".") + " " + maskString
  end

  def toJSON(static)
    j = ""
    if (maskString != "255.255.255.255") && (getCIDR != "0.0.0.0/0")
      j = {"Address" => "#{self.getCIDR}", "Mask" => "#{maskString}"}
    else
      j = {"Address" => "#{@addr_o.join('.')}", "Mask" => "#{maskString}"}
    end
    return j
  end

  def short
    @addr_o.join(".")
  end

  def get
    @addr_o
  end

  def to_i
    @addr_o[0].to_i * (2**24) + @addr_o[1].to_i * (2**16) + @addr_o[2].to_i * (2**8) + @addr_o[3].to_i
  end

  # Get Mask in various formats
  def maskString
    @mask_o.join(".")
  end

  def maskCidr
    (32 - Math.log((2**32 - maskInt), 2)).floor.to_s
  end

  def maskCisco
    (255 ^ @mask_o[0].to_i).to_s + "." + (255 ^ @mask_o[1].to_i).to_s + "." + (255 ^ @mask_o[2].to_i).to_s + "." + (255 ^ @mask_o[3].to_i).to_s
  end

  def getMask
    @mask_o
  end

  def maskInt
    @mask_o[0].to_i * (2**24) + @mask_o[1].to_i * (2**16) + @mask_o[2].to_i * (2**8) + @mask_o[3].to_i
  end

  def update
    unless @mask_o.nil?
      @start   = (maskInt & self.to_i)
      @end     = (getStart + (4294967295 - maskInt))
    end
  end

  def getStart
    @start
  end

  def getEnd
    @end
  end

  def combine(i)
    if i.instance_of?(Group)
      i.getIps.each do |ip|
        combine(ip)
      end
    else
      if ((getStart == i.getStart)||(getEnd == i.getEnd)||(i.contains(self) == 1))
        if (maskInt) > (i.maskInt)
          setaddr(i.short)
          setmask(i.maskString)
        end
      else
        d = (self.to_i ^ i.to_i)
        if d > 0
          newmask = (2**(Math.log2(d + 1).ceil) - 1)
          nmask_o = []

          for j in 0..3
            nmask_o[3 - j] = (255 - (newmask >> (8 * j))) & 255
          end
          for j in 0..3
            @mask_o[j] = (nmask_o[j].to_i & @mask_o[j].to_i) & i.getMask[j].to_i
          end
          for j in 0..3
            @addr_o[3 - j] = (@addr_o[3 - j].to_i & @mask_o[3 - j].to_i)
          end
        end
      end
    end
    update
  end

  def contains(e)
    r = 1
    if (e.instance_of?(Group))
      e.getIps.each do |x|
        r = 0 if (getStart > x.getStart) || (getEnd < x.getEnd)
      end
    else
      r = 0 if (getStart > e.getStart) || (getEnd < e.getEnd)
    end
    r
  end

  def size
    getEnd - getStart + 1
  end

  def groupmembers
    return 1
  end
end

#
# Group of IP addresses
#
class Group < Ip
  def initialize
    super("0.0.0.0", "0.0.0.0")
    @ips = []
    @childs = []
    setName("g-network-#{$groupnum}")
    setDesc("")
    $groupnum += 1
    @hash = 0
    @hits = 0
  end

  def sortByMask
    @ips.sort! { |a, b| a.maskInt <=> b.maskInt }
  end

  def self.all
    ObjectSpace.each_object(self).to_a
  end

  def hash
    @hash
  end

  def supernet
    if $debug
      $debug_file.puts "SUPERNET called for #{self}"
      @ips.each do |v|
        $debug_file.puts "SUPERNET structure #{v} #{v.class}"
      end
    end

    @ips.each do |x|
      if x.instance_of?(Group)
        $debug_file.puts "SUPERNET branching to #{x}" if $debug
        x.supernet
      end
    end

    $debug_file.puts "SUPERNET checking #{@ips.size} IPs" if $debug

    for x in 0..@ips.size - 1
      for y in (@ips.size - 1).downto(0)
        if @ips[x].instance_of?(Ip) && @ips[y].instance_of?(Ip) && x != y && x < @ips.size && y < @ips.size
          if @ips[x].distanceNet(@ips[y]) < $maxnet
            $debug_file.puts "SUPERNET: MATCH: #{@ips[x]} #{@ips[y]}" if $debug
            @ips[x].combine(@ips[y])
            @ips.delete(@ips[y])
            $debug_file.puts "SUPERNET: AFTER: #{@ips[x]}" if $debug
          else
            $debug_file.puts "SUPERNET: TOO FAR: #{@ips[x]} #{@ips[y]}" if $debug
          end
        end
      end
    end

    # Re-Calculate Hash
    @hash = 0
    @ips.each do |i|
      @hash = (@hash + i.to_i) % (2**31)
    end

    0
  end

  def setDesc(n)
    @description = n
  end

  def getDesc
    @description
  end

  def addHit
    @hits += 1
  end

  def getHits
    @hits
  end

  def updateChilds
    @childs = []
    @ips.each do |x|
      if x.instance_of?(Group)
        x.updateChilds
        addChild(x.getName)
      else
        addChild(x.getCIDR)
      end
    end
  end

  def addChild(n)
    @childs.push(n) unless @childs.include?(n)
  end

  def getChild
    @childs.sort! { |a, b| a.downcase <=> b.downcase }
    @childs.sort_by { |ip| ip.split(".").map { |octet| octet.to_i } }
  end

  # t=1: remove overlapts c=0: ignore overlaps
  # f=1: dont create nested groups
  def addIp(i, t)
    unless i.nil?
      if t == 1
        if i.instance_of?(Group)
          @ips.each do |x|
            if !i.instance_of?(Group) && i.contains(x) == 1
              $debug_file.puts "AddIp: Purge #{self} since #{i}contains #{x}" if $debug
              @ips.delete(x)
            end
          end
        end
        @ips.push(i) if contains(i) == 0
      else
        @ips.push(i)
      end

      @hash = 0
      @ips.each do |ip|
        @hash = (@hash + ip.to_i) % (2**31)
      end
    end
    $debug_file.puts "AddIp: Add #{i} to #{self}" if $debug
  end

  def contains(e)
    # Return 1 if all of the IPs in e are matched by at least one IP in this group
    ret = 0
    if e.instance_of?(Group)
      # Argument is LIST
      nret = 0
      e.getIps.each do |ex| # Check all IPs in given group
        @ips.each do |i|
          if i.contains(ex) == 0
            nret = 1 # One given IP is not covered -> No match.
          else
            ret = 1 # One covered IP
          end
        end
      end
      ret = 0 if nret == 1
    else
      # Argument is single IP
      @ips.each do |i|
        ret = 1 if i.contains(e) == 1
      end
    end
    $debug_file.puts "#{e} (#{e.class}) in #{self}? #{ret}" if $debug
    puts "#{e} (#{e.class}) in #{self}? #{ret}" if $xdebug
    ret
  end

  def getIps
    @ips
  end

  def to_s
    @ips.join(" ")
  end

  def groupmembers
    r = 0
    for i in @ips
      r = r + i.groupmembers
    end
    return r
  end

  def toJSON(static)
    i = 0
    p = Array.new
    for a in @ips
      p.push(a.toJSON(static))
    end
    r = Hash.new
    if (static)
      r = { "name" => self.getName, "members" => p , "html" => self.toHtml(self.getName+ "_nested", "black", false)}
    else
      r = { "name" => self.getName, "members" => p  }
    end
    return r
  end

  def flat
    flatarray = []
    @ips.each do |i|
      if i.instance_of?(Group)
        flatarray.concat(i.flat)
      else
        flatarray.push(i)
      end
    end
    flatarray
  end

  def toCiscoFlat
    r = ""
    f = flat.sort_by { |ip| ip.getCIDR.split(".").map { |octet| octet.to_i } }

    # Start: Combine adjacent subnets and remove overlapping networks
    fordel = []

    f.each do |ff|
      f.each do |fff|
        if (fff != ff) && (!fordel.include?(ff)) && (ff.contains(fff) == 1)
          fordel.push(fff)
        end
        newmask =  ff.to_i & (2 * (ff.maskInt ^ 0xFFFFFFFF) + 1) # Neue Maske invers: 1 dort, wo neue Maske 0
        if (fff != ff) && (!fordel.include?(ff)) && (!fordel.include?(fff)) && ((ff.getEnd + 1) == fff.getStart) && (ff.maskInt == fff.maskInt) && (ff.to_i & newmask == 0)
          ff.combine(fff)
          fordel.push(fff)
        end
      end
    end

    fordel.each do |rm|
      f.delete(rm)
    end
    # End: Combine adjacent subnets and remove overlapping networks

    r += "no object-group network #{@name}\n"
    r += "object-group network #{@name}\n"
    f.sort_by { |ip| ip.getCIDR.split(".").map { |octet| octet.to_i } }.each do |i|
      r += " #{i.short} /#{i.maskCidr}\n" unless i.instance_of?(Group)
    end

    r += "!"
    r
  end

  def getCIDR
    r = []
    @ips.sort_by! { |ip| ip.short.split(".").map { |octet| octet.to_i } }.each do |x|
      r.push x.getCIDR
    end
    r.join(" ")
  end

  def toTable
    r = "<tr><td>network</td><td>#{getName.gsub(/_nested$/, '')}</td><td>"
    if @childs.empty? && @ips.empty?
      r += "<i>empty</i>"
    elsif @childs.empty?
      @ips.each do |i|
        r += "#{i}<br />"
      end
    else
      @childs.each do |i|
        r += "#{i.gsub(/_nested/, '')}<br />"
      end
    end
    r += "</td></tr>"
    r
  end

  def combine(i)
    addIp(i, 1)
    # Check for overlaps
    update = 1
    while (update == 1)
      update = 0
      @ips.each do |x|
        @ips.each do |y|
          unless x == y
            if x.contains(y)
              @ips.delete(y)
              clearName
              update = 1
              break
            end
          end
        end
        break if (update == 1)
      end
    end
    @hash = 0
    @ips.each do |x|
      @hash = (@hash + x.to_i) % (2**31)
    end
  end

  def distanceGroup(i)
    dist = 0
    @ips.each do |x|
      dist += i.distanceGroup(x)
    end
    return dist
  end

  def distanceNet(j)
    # Calculates distance for superneting
    r = []
    if j.instance_of?(Group)
      r = j.getIps
    else
      r.push(j)
    end
    dn = 0
    @ips.each do |i|
      r.each do |k|
        dn = k.distanceNet(i) if k.distanceNet(i) > dn
      end
    end
    dn
  end

  def size
    s = 0
    @ips.each do |i|
      s += i.size
    end
    s
  end

  def toHtml(n, color, rec)
    r = ""
    lgroup = Group.all.select { |x| x.getName == n }[0]
    if lgroup.nil?
      if n =~ %r{/32}
        r += "<li class='object-dropdown lara-smaller-options-dropdown' data-ip='#{n.gsub(%r{/32}, "" )}'><span class='tree-label host dropdown-toggle' data-toggle='dropdown' role='button' aria-haspopup='true' aria-expanded='false'><span>#{n.gsub(%r{/32}, "")}</span><i class='fa fa-chevron-down'></i></span></li>"
      else
        r += "<li class='object-dropdown lara-smaller-options-dropdown' data-ip='#{n}'><span class='tree-label network dropdown-toggle' data-toggle='dropdown' role='button' aria-haspopup='true' aria-expanded='false'><span>#{n}</span><i class='fa fa-chevron-down'></i></span></li>"
      end
    else
      #return r if lgroup.getName.match(NEWNET) && lgroup.getChild.length < 2
      if lgroup.getChild.any?
        if rec
          r += "<li><input type='checkbox' id='c-#{n.gsub(/_nested$/, "")}-#{$digit}' /><label for='c-#{n.gsub(/_nested$/, "")}-#{$digit}' class='tree-label group'>#{n.gsub(/_nested$/, "")}</label>"
          $digit += 1
          r += "<ul>"
        end
        lgroup.getChild.each do |k|
          r += lgroup.toHtml(k, lightenColor(color),true)
        end
        r += "</ul></li>"
      else
        r += "<li><span class='tree-label group'>#{lgroup.getName.gsub(/_nested$/, "")}</span></li>";
      end
    end
    r
  end

  def lightenColor(hex_color, amount = 0.05)
    rgb = hex_color.scan(/../).map { |color| color.hex }
    rgb[0] = [(rgb[0].to_i + 255 * amount).round, 255].min
    rgb[1] = [(rgb[1].to_i + 255 * amount).round, 255].min
    rgb[2] = [(rgb[2].to_i + 255 * amount).round, 255].min
    "%02x%02x%02x" % rgb
  end
end

#
# flow and log timestamps
#
class Timestamp
  # 08.09.2015 13:33:46,645691
  def initialize(s)
    @str = s
    (@d, @t) = s.split(/\ /)
    (@day, @month, @year) = @d.split(/\./)
    (@hour, @minute, @secs) = @t.split(/:/)
    (@sec, @usec) = @secs.split(/,/)
    @ticks = (@year.to_i-1970) * 32140800 + (@month.to_i * 2678400) + (@day.to_i * 86400) + (@hour.to_i * 3600) + (@minute.to_i * 60) + @sec.to_i
  end

  def laterThan(t)
    r = 0
    r = 1 if t.getTicks < @ticks
    r
  end

  def getTicks
    @ticks
  end

  def to_s
    @str
  end

end
#
# Represents communication flows
#
class Flow
  def initialize(s, d, p)
    setSrc(s)
    setDst(d)
    setService(p)
  end

  def self.all
    ObjectSpace.each_object(self).to_a
  end

  def toCisco(ac)
    r = ""

    if @src.instance_of?(Group)
      ss = "object-group " + @src.getName
    else
      ss = @src.toCisco
      ss = "any" if ss == "0.0.0.0 0.0.0.0"
    end

    if @dst.instance_of?(Group)
      ds = "object-group " + @dst.getName
    else
      ds = @dst.toCisco
      ds = "any" if ds == "0.0.0.0 0.0.0.0"
    end

    if @svc.instance_of?(ServiceGroup)
      # Service Group
      r = "ip access-list extended #{$aclname} \n #{ac} object-group #{@svc.getName} #{ss} #{ds}"
    elsif $any[@svc.getProto].nil? || (!$any[@svc.getProto].nil? && @svc.matches($any[@svc.getProto]) == 1)
      # ANY
      r = "ip access-list extended #{$aclname} \n #{ac} #{@svc.getProto} #{ss} #{ds}"
    else
      # Service or Port Range
      r += "ip access-list extended #{$aclname} \n #{ac} #{@svc.getProto} #{ss} #{ds} #{@svc.toCisco}"
      $sgroupnum += 1
    end
    r += "\n!"
    r
  end

  def toTable
    r = "<tr><td>access-list</td><td>#{$aclname}</td><td>"

    ss = @src.getName
    ss = "ANY" if ss == "0.0.0.0/0"
    ds = @dst.getName
    ds = "ANY" if ds == "0.0.0.0/0"

    if @svc.instance_of?(ServiceGroup)
      # Service Group
      r += "#{@svc.getName}<br />From: #{ss}<br />To: #{ds}"
    elsif ($any[@svc.getProto].nil?) || (!$any[@svc.getProto].nil? && (@svc.matches($any[@svc.getProto]) == 1))
      # ANY
      r += "#{@svc.getProto}/ANY<br />From: #{ss}<br />To: #{ds}"
    else
      # Service or Port Range
      r += "#{@svc.getProto}/#{@svc.toCisco}<br />From: #{ss}<br />To: #{ds}<br />Port: #{@svc.toCisco}"
      $sgroupnum += 1
    end
    r += "</td>"
    r
  end

  def toJSON(static)
    s = @src.toJSON(static)
    d = @dst.toJSON(static)
    if (@src.class != Group)
      s = { "name" => "", "members" => [ s ] }
    end
    if (@dst.class != Group)
      d = { "name" => "", "members" => [ d ]}
    end

    se = nil
    if (@svc.class != ServiceGroup)
      se = { "name" => "", "members" => [ @svc.toJSON ]}
    else
      se = @svc.toJSON
    end


    j = { "SRC" => s, "DST" => d, "Service" => se }
    return j
  end

  def to_s
    srcs = @src.to_s.split(" ")
    dsts = @dst.to_s.split(" ")
    r = "#{srcs.uniq.join(' ')} -> #{dsts.uniq.join(' ')}:#{@svc}"
    r
  end

  # Method to print networks in HTML
  def netHtmlOutput(line,static)
    r = ""
    d = ""
    if line =~ %r{/32}
      if static == 0
        r += "<div class='dropdown-button'>\n"
        r += "<button class='button dropdown-toggle fg-white' style='text-align:left; min-width:17em; background-color:rgba(250, 104, 0, 0.8);'>\n"
        r += "<img class=icon src='icons/host_plain.png' /><a class='icon-text' target='_blank' href=#{IPADMIN + line}> #{line.gsub(%r{/32}, "")}</a>\n</button>\n"
        r += "<ul class='split-content d-menu' data-role='dropdown'>\n"

        r += "<li class='menu-title'>Investigate</li>"
        r += "<li><a class='icon-text' target='_blank' href=#{IPADMIN + line}><span class='mif-location icon'></span><span style='background-colorx:rgba(98, 167, 39, 1.0);' class='xfg-white'> Check networktools.com&nbsp;</span></a></li>\n"
        #r += "<li><a href='#'><span class='mif-zoom-in icon'></span><span style='background-colorx:rgba(98, 167, 39, 1.0);' class='xfg-white'> Get Host Report&nbsp;</span></a></li>\n"
        #r += "<li><a href='#'><span class='mif-settings-ethernet icon'></span><span style='background-colorx:rgba(98, 167, 39, 1.0);' class='xfg-white'> Get 24h Conversations&nbsp;</span></a></li>\n"
        r += "</ul></div><br />\n"
      else
        r += "<img class=icon src='icons/host_plain.png' /><a class='icon-text' target='_blank' href=#{IPADMIN + line}> #{line.gsub(%r{/32}, "")}</a><br />"
      end
    elsif line == "0.0.0.0/0"
      r += "<img class=icon src='icons/any_object.png' /> ANY<br />"
    else
      if static == 0
        fline = line.gsub(/[\/\.]/, "_")
        d += "<div data-role='dialog' id='dialog1_#{fline}' class='padding20' data-close-button='true' data-overlay='true' data-overlay-color='op-dark' data-overlay-click-close='true'>"
        d += "<iframe src='https://10.254.253.171/lc-landing-page/smc.html#/hostlistview?keyword=#{line}' style='overflow:hidden;margin:0;padding:0;width:1400px;height:800px;' frameborder='0'></iframe>"
#        d += "<iframe src='https://10.254.253.171/lc-landing-page/smc.html#host/#{line}' style='overflow:hidden;margin:0;padding:0;width:1400px;height:800px;' frameborder='0'></iframe>"
        d += "</div>"
        d += "<div data-role='dialog' id='dialog2_#{fline}' class='padding20' data-close-button='true' data-overlay='true' data-overlay-color='op-dark' data-overlay-click-close='true'>"
        d += "<iframe src='https://www.ultratools.com/tools/ipWhoisLookupResult?ipAddress=#{line.gsub(%r{/.*},'')}' style='overflow:hidden;margin:0;padding:0;width:1400px;height:800px;' frameborder='0'></iframe>"
        d += "</div>"
        d += "<div data-role='dialog' id='dialog3_#{fline}' class='padding20' data-close-button='true' data-overlay='true' data-overlay-color='op-dark' data-overlay-click-close='true'>"
        d += "<iframe src='dialog.html?address=#{line}' style='width:1400px;height:800px;' frameborder='0'></iframe>"
        d += "</div>"

        # Icons siehe http://metroui.org.ua/font.html
        r += "<div class='dropdown-button'>"
        r += "<button class='button dropdown-toggle fg-white' style='text-align:left; min-width:17em; background-color:rgba(250, 104, 0, 0.8);'>"
        r += "<img class=icon src='icons/network.png' /> #{line.gsub(%r{/32}, "")}<br />"
        r += "</button>"
        r += "<ul class='split-content d-menu' data-role='dropdown'>"
        r += "<li class='menu-title'>Investigate</li>"

        r += "<li><a class='icon-text' target='_blank' href=#{IPADMIN + line}><span class='mif-location icon'></span><span style='background-colorx:rgba(98, 167, 39, 1.0);' class='xfg-white'> Check networktools.com&nbsp;</span></a></li>\n"

        #r += "<li><a onclick=\"showDialog('dialog1_#{fline}')\"><span class='mif-list icon'></span> Get Top Active Hosts</a></li>"
        #r += "<li><a onclick=showDialog('dialog2_#{fline}')><span class='mif-settings-ethernet icon'></span> Get 24h Conversations</a></li>"
        #r += "<li><a onclick=showDialog('dialog3_#{fline}')><span class='mif-home icon'></span> Local HTML</a></li>"
        r += "</ul>"
        r += "</div><br />"
      else
        r += "<img class=icon src='icons/network.png' /><a class='icon-text' target='_blank' href=#{IPADMIN + line}> #{line}</a><br />"
      end
    end
    return r,d
  end

  def toHtml(static)
    r = ""
    d = ""
    if @src.instance_of?(Group) && @src.getName !~ /g-network/
      if @src.getName =~ /Ignored/
        r += "<td class='fg-white shadow' style='background-color:rgba(250, 104, 0, 0.5);'>"
      else
        r += "<td>"
      end
      r += "<div class='treeview' data-role='treeview'><ul>"
      r += @src.toHtml(@src.getName + "_nested", "487D12") # Recursive code ahead
      r += "</ul></div>"
    else
			if @src.getCIDR.split(" ").count > 2 and static == 0
        r += "<td class='fg-white shadow' style='background-color:rgba(250, 104, 0, 0.8);'>"
				r += "<div class='treeview' data-role='treeview'>"
				r += "<ul><li class='node collapsed'><span class='mif-more-horiz'></span>&nbsp;<span class='leaf fg-white'><i><b>Multiple Sources</b></i></span>"
				r += "<ul style='display: none;'>"
				@src.getCIDR.split(" ").each do |line|
					a,b = netHtmlOutput(line,static)
					r += "<li>#{a}</li>"
					d += b
				end
				r += "</ul></li></ul></div>"
			else
				r += "<td>"
				@src.getCIDR.split(" ").each do |line|
					a,b = netHtmlOutput(line,static)
					r += a
					d += b
				end
			end
    end
    r += "</td>"
    if @dst.instance_of?(Group) && @dst.getName !~ /g-network/
      if @dst.getName =~ /Ignored/
        r += "<td class='fg-white shadow' style='background-color:rgba(250, 104, 0, 0.5);'>"
      else
        r += "<td>"
      end
      r += "<div class='treeview' data-role='treeview'><ul>"
      r += @dst.toHtml(@dst.getName + "_nested", "487D12") # Recursive code ahead
      r += "</ul></div>"
    else
			if @dst.getCIDR.split(" ").count > 2 and static == 0
        r += "<td class='fg-white shadow' style='background-color:rgba(250, 104, 0, 0.8);'>"
				r += "<div class='treeview' data-role='treeview'>"
				r += "<ul><li class='node collapsed'><span class='mif-more-horiz'></span>&nbsp;<span class='leaf fg-white'><i><b>Multiple Destinations</b></i></span>"
				r += "<ul style='display: none;'>"
				@dst.getCIDR.split(" ").each do |line|
					a,b = netHtmlOutput(line,static)
					r += "<li>#{a}</li>"
					d += b
				end
				r += "</ul></li></ul></div>"
			else
				r += "<td>"
				@dst.getCIDR.split(" ").each do |line|
					a,b = netHtmlOutput(line,static)
					r += a
					d += b
				end
			end
    end
    r += "</td>"
    if @svc.instance_of?(ServiceGroup) && @svc.getName !~ /g-service/
      r += "<td style='background-color:rgba(28, 183, 236, 0.3);'>"
      r += "<div class='treeview' data-role='treeview'>\n<ul>"
      r += @svc.toHtml(@svc.getName, "487D12", "black")
      r += "</ul></div>"
    elsif @svc.getPort.to_s == "-1" && @src.to_s == "0.0.0.0/0" && @dst.to_s == "0.0.0.0/0"
      r += "<td style='padding-left:25px;background-color:rgba(28, 183, 236, 0.3);'>"
      r += "<img class=icon src='icons/#{@svc.getProto}_service.png' /> ANY<br />"
    elsif @svc.getPort.to_s == "-1"
      r += "<td style='padding-left:25px'>"
      r += "<img class=icon src='icons/#{@svc.getProto}_service.png' /> ANY<br />"
    else
      r += "<td>"
      f = 0
      @svc.to_s.split(" ").each do |line|
        (port, proto) = line.split("/")
        r += "<nobr>" if f == 0
        if proto.is_i?
          r += "<img class=icon src='icons/ip_service.png' /> #{port}"
        elsif proto == "igmp"
          r += "<img class=icon src='icons/ip_service.png' /> #{port}"
        else
          r += "<img class=icon src='icons/#{proto}_service.png' /> #{port}"
        end
        if f == 1
          r += "</nobr><br />"
          f = 0
        else
          r += "&nbsp;"
          f = 1
        end
      end
    end
    r += "</td>"
    return r, d
  end

  def setSrc(s)
    @src = s
  end

  def setDst(d)
    @dst = d
  end

  def setService(dp)
    @svc = dp
  end

  def addService(dp)
    if @svc.instance_of?(ServiceGroup)
      @svc.addService(dp)
    else
      svc2 = ServiceGroup.new
      svc2.addService(@svc)
      @svc = svc2
    end
  end

  def getSrc
    @src
  end

  def getDst
    @dst
  end

  def getService
    @svc
  end

  def spawn
    Flow.new(getSrc.spawn, getDst.spawn, getService)
  end

  def distanceNet(f)
    getSrc.distanceNet(f.getSrc) + getDst.distanceNet(f.getDst) + 1
  end

  def distanceGroup(f)
    #$t10.start
    # Calculate distance for grouping

    #$t21.start
    pr = getService.getProto
    #$t21.stop
    #$t22.start
    l = getService.distance(f.getService)
    #$t22.stop
    #$t23.start
    s = getSrc.distanceGroup(f.getSrc)
    #$t23.stop
    #$t24.start
    d = getDst.distanceGroup(f.getDst)
    #$t24.stop
    #$t25.start
    m1 = getSrc.contains(f.getSrc)
    #$t25.stop
    #$t26.start
    m2 = getDst.contains(f.getDst)
    #$t26.stop
    #$t27.start
    m3 = getService.matches(f.getService)
    #$t27.stop
    a = 1
    #$t10.stop
    #$t11.start
    if (m1 + m2 + m3 >= 2)
      #$t11.stop
      a = 0
    else
      #$t11.stop
      #$t12.start
      a *= s if s > 0
      a *= d if d > 0
      a *= l if (l > 0 && ($any[pr] != nil) && (getService.matches($any[pr]) == 0))
      #$t12.stop
    end
    return a
  end

  def combine(f)
    getSrc.combine(f.getSrc)
    getDst.combine(f.getDst)
    puts "Combining #{getService} and #{f.getService}"
    if (getService.instance_of?(ServiceGroup) && f.getService.instance_of?(ServiceGroup))
      for s in f.getService.getService
        getService.add(s)
      end
    elsif f.getService.instance_of?(ServiceGroup)
      f.getService.add(getService)
      setService(f.getService)
    elsif getService.instance_of?(ServiceGroup)
      getService.add(f.getService)
    else
      sg = ServiceGroup.new
      sg.setProto(getService.getProto)
      sg.addService(getService)
      sg.addService(f.getService)
      setService(sg)
    end
  end

  def size
    @src.size * @dst.size * @svc.getSize
  end

end

#
# Represents ACL entries = communication flow + action
#
class Rule
  def initialize(f, a)
    setFlow(f)
    setAction(a)
    clearHitcount
    clearStatic
    mod
  end


  def setId
    $id += 1
    @id = $id
  end

  def getId
    return @id
  end

  # Start: Track updates to rule
  def modified
    return @update
  end

  def mod
    setId
    @update = true
  end

  def ack
    @update = false
  end

  # End: Track updated to rule

  def clearStatic
    @static = 0
  end

  def setStatic
    @static = 1
  end

  def getStatic
    @static
  end

  def size
    @flow.size
  end

  def setFlow(f)
    @flow = f
  end

  def getFlow
    @flow
  end

  def setAction(a)
    @action = a
  end

  def getAction
    @action
  end

  def match(f, x, y)
    r = 0
    if ((r == 0) && (@flow.getService.matches(f.getService) == 1) && (@flow.getSrc.contains(f.getSrc) == 1) && (@flow.getDst.contains(f.getDst) == 1) )
      r = 1
      @hitcount += x
      @throughput += y
    end
    $debug_file.puts "Match #{f.getSrc} #{f.getDst} #{f.getService} in #{@flow.getSrc} #{@flow.getDst} #{@flow.getService} (#{@flow.getSrc.class} #{@flow.getDst.class} #{@flow.getService.class})? #{r}" if $debug
    $debug_file.puts "Match details: #{f.getSrc} #{f.getDst} #{f.getService}: #{(@flow.getSrc.contains(f.getSrc) == 1)} #{(@flow.getDst.contains(f.getDst) == 1)} #{(@flow.getService.matches(f.getService) == 1)}" if $debug
    r
  end

  def clearHitcount
    @hitcount = 0
    @throughput = 0
  end

  def getHitcount
    @hitcount
  end

  def getPrettyHitcount
    @hitcount.toDots
  end

  def setHitcount(s)
    @hitcount = s
  end

  def getThroughput
    @throughput
  end

  def getPrettyThroughput
    @throughput.toFilesize
  end

  def setThroughput(s)
    @throughput = s
  end

  def <=>(o)
    getHitcount <=> o.getHitcount
  end

  def spawn
    n = Rule.new(getFlow.spawn, getAction)
    n.setHitcount(getHitcount)
    n.setThroughput(getThroughput)
    n
  end

  def distanceNet(r)
    getFlow.distanceNet(r.getFlow)
  end

  def distanceGroup(r)
    getFlow.distanceGroup(r.getFlow)
  end

  def to_s
    getFlow.to_s
  end

  def toJSON(static)
    return getFlow.toJSON(static)
  end

  def combine(r)
    setThroughput(getThroughput + r.getThroughput)
    setHitcount(getHitcount + r.getHitcount)
    getFlow.combine(r.getFlow)
  end

end

class Ruleset
  def initialize
    @rules = []
  end

  def escapePort(s)
    if (s.class == Array)
      r = Array.new
      for x in s
        r.push(escapePort(x))
      end
    elsif (s.class == Hash)
      r = Hash.new
      s.each do |k,v|
        if (k == "Port")
          r = r.merge({k => "#{v}"})
        else
          r = r.merge({k => escapePort(v)})
        end
     end
    else
      r = s
   end
    return r
  end

  def read(f)
    gservice = {}
    gnetwork = {}

    infile = File.read(f)

    # searching for service groups
    sgroups = infile.scan(/^(object-group service.*?)^!/m).flatten
    sgroups.each do |x|
      name = ""
      lines = x.split("\n")
      lines.each do |line|
        line.strip!
        # skip comments
        line = line.gsub(/\s*!.*/, "")
        next if line == ""
        if /object-group service/ =~ line
          name = line.gsub("object-group service ", "")
          gservice[name] = ServiceGroup.new
          gservice[name].setName(name)
        elsif /description (?<desc>.*)/ =~ line
          gservice[name].setDesc(desc)
        elsif /group-object (?<child>.*)/ =~ line
          gservice[name].addService(gservice[child])
          gservice[name].addChild(child)
        else
          (proto, type, port1, port2) = line.split(/\ /)
          if type == "range"
            pr = PortRange.new
            pr.setStartPort(port1.to_i)
            pr.setEndPort(port2.to_i)
            pr.setProto(proto)
            gservice[name].addService(pr)
            gservice[name].addChild(pr)
          elsif port1 == "any"
            gservice[name].addService($any[proto])
            gservice[name].addChild($any[proto])
          else
            s = Service.new
            s.setProto(proto)
            s.setPort(port1.to_i)
            gservice[name].addService(s)
            gservice[name].addChild(s)
          end
        end
      end
    end

    # searching for network groups
    ngroups = infile.scan(/^(object-group network .*?)^!/m).flatten
    # generate needed default groups before finding them
    #gnetwork[NEWINTERNALGROUP] = Group.new
    #gnetwork[NEWINTERNALGROUP].setName(NEWINTERNALGROUP)
    #gnetwork[NEWINTERNALGROUP].setDesc("All IPs discovered to be local to the OE locations")
    #gnetwork[NEWINTERNALGROUPNEST] = Group.new
    #gnetwork[NEWINTERNALGROUPNEST].setName(NEWINTERNALGROUPNEST)
    #gnetwork[NEWINTERNALGROUPNEST].setDesc("All IPs discovered to be local to the OE locations")
    #gnetwork[OLDINTERNALGROUP] = Group.new
    #gnetwork[OLDINTERNALGROUP].setName(OLDINTERNALGROUP)
    #gnetwork[OLDINTERNALGROUP].setDesc("All servers local to the OE locations")
    ngroups.each do |x|
      name = nil
      hname = nil
      fsindex = nil
      lines = x.split("\n")
      lines.each do |line|
        line.strip!
        # skip comments
        line = line.gsub(/\s*!.*/, "")
        next if line == ""
        if /object-group network/ =~ line
          name = line.gsub("object-group network ", "")
          hname = name + "_nested"
          if gnetwork[name].nil?
            gnetwork[name] = Group.new
            gnetwork[name].setName(name)
          end
          if gnetwork[hname].nil?
            gnetwork[hname] = Group.new
            gnetwork[hname].setName(hname)
          end
        elsif /^description (?<d>.*)$/ =~ line
          gnetwork[name].setDesc(d)
          gnetwork[hname].setDesc(d)
        elsif /(?<ip>\d{1,3}\.\d{1,3}\.\d{1,3}.\d{1,3}) (?<mask>.*)$/ =~ line
          mask = cidrToNetmask(mask.gsub(%r{/}, "")) if mask =~ %r{/}
          n = Ip.new(ip, mask)
          if name =~ /Netflow_Sources/
            sname = name.gsub(/-Netflow_Sources/, "")
            $outside_interfaces[ip] = [fsindex, sname]
            gnetwork[name].addIp(n, 0)
            gnetwork[name].addChild(n.getCIDR)
            gnetwork[hname].addIp(n, 0)
            gnetwork[hname].addChild(n.getCIDR)
            #gnetwork[NEWNET + "_" + sname] = Group.new
            #gnetwork[NEWNET + "_" + sname].setName(NEWNET + "_" + sname)
            #gnetwork[NEWNET + "_" + sname].addIp(n, 0)
            #gnetwork[NEWNET + "_" + sname].addChild(n.getCIDR)
            #gnetwork[NEWINTERNALGROUP].addIp(gnetwork[NEWNET + "_" + sname], 0)
            #gnetwork[NEWINTERNALGROUP].addChild(NEWNET + "_" + sname)
            #gnetwork[NEWINTERNALGROUPNEST].addIp(gnetwork[NEWNET + "_" + sname], 0)
            #gnetwork[NEWINTERNALGROUPNEST].addChild(NEWNET + "_" + sname)
          else
            gnetwork[name].addIp(n, 0)
            gnetwork[name].addChild(n.getCIDR)
            gnetwork[hname].addIp(n, 0)
            gnetwork[hname].addChild(n.getCIDR)
          end
        elsif /^group-object (?<goname>.*)$/ =~ line
          if name =~ /Netflow_Sources/
            fsindex = goname.gsub(/ifindex-(\d+)\-.*/i, "\\1")
          else
            gnetwork[name].addIp(gnetwork[goname], 0)
            gnetwork[name].addChild(goname)
            gnetwork[hname].addChild(goname+"_nested")
          end
        end
      end
    end

    # Flatten group object to improve performance
    gnetwork.each do |key, value|
      next if (key =~ /_nested/) || (key =~ /Netflow/)
      flat = value.flat
      gnetwork[key] = Group.new

      # Start: Remove overlaps and merge adjacent networks
      fordel = []

      flat.each do |ff|
        flat.each do |fff|
          if (fff != ff) && (!fordel.include?(ff)) && (ff.contains(fff) == 1)
            fordel.push(fff)
          end
          newmask =  ff.to_i & (2 * (ff.maskInt  ^ 0xFFFFFFFF) + 1) # Neue Maske invers: 1 dort, wo neue Maske 0
          if (fff != ff) && (!fordel.include?(ff)) && (!fordel.include?(fff)) && ((ff.getEnd + 1) == fff.getStart) && (ff.maskInt == fff.maskInt) && (ff.to_i & newmask == 0)
            ff.combine(fff)
            fordel.push(fff)
          end
        end
      end

      fordel.each do |rm|
        flat.delete(rm)
      end
      # End: Remove overlaps and merge adjacent networks

      flat.each do |i|
        gnetwork[key].addIp(i, 1)
        gnetwork[key].addChild(i.getCIDR)
        gnetwork[key].setName(key)
      end
      gnetwork[key].sortByMask
    end

    # Add internal networks rule if discovered is on
    # and the rule doesn't exist already
    #if $discover
    #  src = gnetwork[NEWINTERNALGROUP]
    #  dst = Ip.new("0.0.0.0", "0.0.0.0")
    #  tp = Service.new
    #  tp.setProto("ip")
    #  tp.setPort(ANY)
    #  fl = Flow.new(src, dst, tp)
    #  r = Rule.new(fl, "permit")
    #  if !$override
    #    r.setStatic
    #  end
    #  add(r)
    #  $debug_file.puts "Rule created: #{r} (discover rule)" if $debug
    #end

    # searching for acls
    acls = infile.scan(/^(ip access-list.*?)^!/m).flatten
    acls.each do |x|
      lines = x.split("\n")
      lines.each do |line|
        snet = nil
        dnet = nil
        tp = nil
        if /^ip access-list extended (?<n>.*)/ =~ line
          $aclname = n
          next
        end
        next if /established/ =~ line
        line.strip!
        # skip comments
        line = line.gsub(/\s*!.*/, "")
        next if line == ""
        arr = line.split(/\ /)
        # read the action
        action = arr.shift
        # Getting the protocol or service group
        if arr[0] == "object-group"
          tp = gservice[arr[1]]
          arr.shift(2)
        else
          # try to find a port
          if arr[-2] == "eq"
            tp = Service.new
            tp.setProto(arr[0])
            tp.setPort(arr[-1].to_i)
            arr.pop(2)
          elsif arr[-3] == "range"
            tp = ServiceGroup.new
            tp.setProto(arr[0])
            tp.setStartPort(arr[-2].to_i)
            tp.setEndPort(arr[-1].to_i)
            arr.pop(3)
          else
            tp = Service.new
            tp.setProto(arr[0])
            tp.setPort(ANY)
          end
          arr.shift
        end

        # Getting the source
        if arr[0] == "object-group"
          snet = gnetwork[arr[1]]
          arr.shift(2)
        elsif arr[0] == "any"
          snet = Ip.new("0.0.0.0", "0.0.0.0")
          arr.shift
        elsif arr[0] =~ /\d{1,3}\.\d{1,3}\.\d{1,3}.\d{1,3}/
          snet = Ip.new(arr[0], arr[1])
          arr.shift(2)
        else
          puts "Source not found in line #{line}!"
        end

        # and destination
        if arr[0] == "object-group"
          dnet = gnetwork[arr[1]]
          arr.shift(2)
        elsif arr[0] == "any"
          dnet = Ip.new("0.0.0.0", "0.0.0.0")
          arr.shift
        elsif arr[0] =~ /\d{1,3}\.\d{1,3}\.\d{1,3}.\d{1,3}/
          dnet = Ip.new(arr[0], arr[1])
          arr.shift(2)
        else
          puts "Destination not found in line #{line}!"
        end

        # creating a flow
        fl = Flow.new(snet, dnet, tp)
        # Set rule details here
        r = Rule.new(fl, action)
        if !$override
          r.setStatic
        end
        add(r)
        $debug_file.puts "Rule created: #{@r}" if $debug
      end
    end

    gnetwork.each do |name, grp|
      $ngroups.push(grp)
      $nnames[name] = grp
      $debug_file.puts "Network created: #{name} #{grp} (#{grp.class})" if $debug
    end

    gservice.each do |name, grp|
      $sgroups.push(grp)
      $debug_file.puts "Service created: #{name} #{grp} (#{grp.class})" if $debug
    end

  end

  def getStaticRules
    r = []
    @rules.each do |x|
      r.push(x) if x.getStatic == 1
    end
    r
  end

  def getDynamicRules
    r = []
    @rules.each do |x|
      r.push(x) if x.getStatic == 0
    end
    r.sort_by! { |a| a.getHitcount }.reverse
  end

  def add(a)
    @rules.push(a)
  end

  def del(x)
    @rules.delete_at(x)
  end

  def getRules
    @rules
  end

  def getSize
    x = 0
    @rules.each do |r|
      x += r.size
    end
    x
  end

  def getSizeLog
    Math.log10(getSize).round(2)
  end

  def getRule(i)
    r = nil
    r = @rules[i] if i < @rules.size
    r
  end

  def removeUnused
    @clnd = 1
    while (@clnd == 1)
      @clnd = 0
      for @i in 0..@rules.size - 1
        if (!@rules[@i].nil? && (@rules[@i].getHitcount == 0) && (@rules[@i].getStatic == 0))
          del(@i)
          @clnd = 1
          @i = 0 if @i > 0
        end
      end
    end
  end

  def match(f, x, z)
    r = 0
    y = 0
    @rules.each do |i|
      y += 1
      r = y if (r == 0) && (i.match(f, x, z) == 1)
    end
    r
  end

  def sort
    getDynamicRules.sort_by! { |a| a.getHitcount }.reverse
  end

  def print
    i = 0

    puts "=== Static Rules ==="
    getStaticRules.each do |x|
      i += 1
      puts "#{i}: #{x.getFlow} #{x.getAction} (#{x.getPrettyHitcount} hits/#{x.getPrettyThroughput})"
    end

    puts "=== Dynamic Rules ==="
    getDynamicRules.each do |x|
      i += 1
      puts "#{i}: #{x.getFlow} #{x.getAction} (#{x.getPrettyHitcount} hits/#{x.getPrettyThroughput})"
    end

  end

  # Call with print_cisco(filename, "dynamic|all“, mode)
  def toCiscoString()
    r = ""
    @rules.each do |x|
      # skip new internal
      r += "#{x.getFlow.getSrc.toCiscoFlat}" if x.getFlow.getSrc.instance_of?(Group)
      r += "#{x.getFlow.getDst.toCiscoFlat}" if x.getFlow.getDst.instance_of?(Group)
      r += "#{x.getFlow.getService.toCiscoFlat}" unless x.getFlow.getService.instance_of?(Service)
      r += "#{x.getFlow.toCisco(x.getAction)}"
    end
    return r
  end

  def printFile(f)
    file = File.new("#{f}", "w")
    i = 0
    file.puts "=== Ruleset ==="
    @rules.each do |x|
      i += 1
      file.puts "#{i}: #{x.getFlow} #{x.getAction} (#{x.getPrettyHitcount} hits/#{x.getPrettyThroughput} bytes)"
    end
    file.close
  end

  def printElastic(router,idx)
    #file = File.new("#{f}","w")

    sacllines = 0
    nacllines = 0
    # Print Applicability
    n = ""
    getStaticRules.sort_by { |a| a.getHitcount }.reverse.each do |x|
      # skip new internal
      if x.getFlow.getSrc.instance_of?(Group)
        x.getFlow.getSrc.toCiscoFlat.split("\n").each do |line|
          sacllines += 1
        end
      end
      if x.getFlow.getDst.instance_of?(Group)
        x.getFlow.getDst.toCiscoFlat.split("\n").each do |line|
          sacllines += 1
        end
      end
      if x.getFlow.getService.instance_of?(ServiceGroup)
        x.getFlow.getService.toCisco.split("\n").each do |line|
          sacllines += 1
        end
      end
      x.getFlow.toCisco(x.getAction).split("\n").each do |line|
        sacllines += 1
      end
    end
    getDynamicRules.sort_by { |a| a.getHitcount }.reverse.each do |x|
      if x.getFlow.getSrc.instance_of?(Group)
        x.getFlow.getSrc.toCiscoFlat.split("\n").each do |line|
          nacllines += 1
        end
      end
      if x.getFlow.getDst.instance_of?(Group)
        x.getFlow.getDst.toCiscoFlat.split("\n").each do |line|
          nacllines += 1
        end
      end
      if x.getFlow.getService.instance_of?(ServiceGroup)
        x.getFlow.getService.toCisco.split("\n").each do |line|
          nacllines += 1
        end
      end
      x.getFlow.toCisco(x.getAction).split("\n").each do |line|
        nacllines += 1
      end
    end

    fn = Pathname.new($fname).basename.to_s

    p9 = Array.new
    $nnames.sort_by { |_k,v| v.getHits }.reverse.each do |key, value|
      next unless key =~ /New_Net/
      next unless value.getHits > 0
      p9.push({ "name" => "#{key.gsub(NEWNET + "_", '')}", "count" => "#{value.getHits.toDots}" })
    end
    $unknownexporter.each do |key, value|
      p9.push({ "name" => "#{key}", "count" => "#{value.toDots}" })
    end

    py = "Resulting Cisco ACL Rules - Base #{sacllines} lines + New #{nacllines} lines generated on #{Time.now.strftime('%Y-%m-%d - %H:%M:%S')}"

    p0 = { "base_rules" => "#{sacllines}", "new_rules" => "#{nacllines}",  "time" =>  "#{Time.now.strftime('%Y-%m-%d - %H:%M:%S')}", "file" => "#{fn}", "routers" => p9 }

    # Print Static Rules
    p1 = Array.new
    getStaticRules.each do |x|
      p1.push({ "Flow" => x.getFlow.toJSON(true), "Action" => "#{x.getAction}", "Hits" => x.getHitcount, "Bytes" => x.getThroughput })
    end

    # Print Dynamic Rules
    p2 = Array.new
    getDynamicRules.each do |x|
      p2.push({ "Flow" => x.getFlow.toJSON(false), "Action" => "#{x.getAction}", "Hits" => x.getHitcount, "Bytes" => x.getThroughput })
    end

    # Print Top Sources for new rules
    i = 1
    p3 = Array.new
    Hash[$topsrc_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      if (value > 0)
        p3.push({ "no" => i, "ip" => "#{key}", "count" => value, "size" => $topsrc_bytes[key] })
        i += 1
      end
    end


    # Print Top Destinations for new rules
    i = 1
    p4 = Array.new
    Hash[$topdst_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      if (value > 0)
        p4.push({ "no" => i, "ip" => "#{key}", "count" => value, "size" => $topdst_bytes[key] })
        i += 1
      end
    end

    # Print Top Sources static rules
    i = 1
    p5 = Array.new
    Hash[$presrc_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      if (value > 0)
        p5.push({ "no" => i, "ip" => "#{key}", "count" => value, "size" => $presrc_bytes[key] })
        i += 1
      end
    end


    # Print Top Destinations static rules
    i = 1
    p6 = Array.new
    Hash[$predst_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      if (value > 0)
        p6.push({ "no" => i, "ip" => "#{key}", "count" => value, "size" => $predst_bytes[key] })
        i += 1
      end
    end

    # Print detailed definitions
    p7 = Array.new
    p8 = Array.new
    p9 = Array.new
    Group.all.sort_by(&:getName).each do |x|
      # show only the _nested groups - we remove the _nested string in the method toTable
      next if x.getName !~ /_nested$/
      p7.push({ "Group" => x.toJSON(false) })
    end
    ServiceGroup.all.sort_by(&:getName).each do |x|
      p8.push({ "ServiceGroup" => x.toJSON })
    end
    @rules.each do |x|
      p9.push({ "Rule" => x.getFlow.toJSON(false) })
    end


    # Will you be able just to split main JSON file (v-demoX.json) to three separated files?
    #  Applicability - applicability.json
    #  StaticRuleset and DynamicRuleset -ruleset.json
    #  Details - details.json
    # Could you please (for the next time) use next naming for top sources and destinations?
    #  top-destinations-dynamic.json
    #  top-destinations-static.json
    #  top-sources-dynamic.json
    #  top-sources-static.json
    # ACL rules I read from two files:
    #  base-rules.acl
    #  new-rules.acl

 #   px = "Ruleset tested in "+Time.at(Time.now - $start_time).utc.strftime("%M:%S")+" min - #{getRules.count} rules for #{$hits.toDots} hits in #{$bytes.toFilesize} traffic from "+DateTime.parse($starttime.to_s).strftime('%b %d') +" to "+DateTime.parse($endtime.to_s).strftime('%b %d')

#    rs1 = JSON.generate({ "Applicability" => p0 } )
    rs2 = JSON.generate({ "Ruleset" => { "StaticRuleset" => p1, "DynamicRuleset" => p2 }})
#    rs3 = JSON.generate({ "Details" => { "Groups" => p7, "Servicegroups" => p8, "Rules" => p9 } })
#    rs4 = JSON.generate({ "TopSourcesDynamic" => p3 } )
#    rs5 = JSON.generate({ "TopDestinationsDynamic" => p4 } )
#    rs6 = JSON.generate({ "TopSourcesStatic" => p5 } )
#    rs7 = JSON.generate({ "TopDestinationsStatic" => p6 } )
#    rs8 = JSON.generate({ "Headline" => px })
    rs4 = JSON.generate( p3 )
    rs5 = JSON.generate( p4 )
    rs6 = JSON.generate( p5 )
    rs7 = JSON.generate( p6 )



    tl = Time.now.to_s
    index = "#{idx}-#{tl[0,4]}-#{tl[5,2]}-#{tl[8,2]}"
    rtz = "#{tl.to_s[0,10]}T#{tl.to_s[11,8]}.000Z"

    s = Hash.new
    s["@timestamp"] = rtz
    s["name"] = router
    s["rules"] = escapePort(rs2)
    s["top_sources_static"] = rs6
    s["top_sources_dynamic"] = rs4
    s["top_destinations_static"] = rs7
    s["top_destinations_dynamic"] = rs5
    s["acl"]  = self.toCiscoString()
    $esclient.index index: "#{index}", type: "policy", body: s
    puts "ELASTIC: #{s}"
  end

  def printHtml(f)
    i = 0
    dialogs = ""
    file = File.new("#{f}", "w")
    file.puts "<!DOCTYPE html>"
    file.puts "<html>\n<head lang='en'>"
    file.puts "<meta charset='UTF-8'>"
    file.puts "<meta http-equiv='X-UA-Compatible' content='IE=edge'>"
    file.puts "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
    file.puts "<meta name='description' content=''>"
    file.puts "<meta name='keywords' content=''>"
    file.puts "<meta name='author' content=''>"
    file.puts "<link rel='shortcut icon' type='image/x-icon' href='favicon.ico' />"
    file.puts "<link href='css/metro.css' rel='stylesheet'>"
    file.puts "<link href='css/metro-icons.css' rel='stylesheet'>"
    file.puts "<link href='css/metro-responsive.css' rel='stylesheet'>"
    file.puts "<link href='css/metro-schemes.css' rel='stylesheet'>"
    file.puts "<link href='css/prettify.css' rel='stylesheet'>"
    file.puts "<link href='css/overwrite.css' rel='stylesheet'>"
    file.puts "<script src='css/jquery-2.1.3.min.js'></script>"
    file.puts "<script src='css/jquery.dataTables.min.js'></script>"
    file.puts "<script src='css/metro.js'></script>"
    file.puts "<script src='css/prettify.js'></script>"
    file.puts "<script>"
    file.puts "$.extend( $.fn.dataTable.defaults, {"
    file.puts "'lengthMenu': [ [10, 25, 50, 100, -1], [10, 25, 50, 100, 'All'] ]"
    file.puts "} );"
    file.puts "</script>"
    #    file.puts "<script>function showDialog(id){var dialog = $(id).data('dialog');dialog.open();}</script>"
    file.puts "<script> function showDialog(id){ var dialog = $('#'+id).data('dialog'); if (!dialog.element.data('opened')) { dialog.open(); } else { dialog.close(); } } </script>"

    file.puts "<title>Ruleset #{f}</title>"
    file.puts "</head>"
    file.puts "<body onload='init'>"
    file.puts "<div class='container page-content'>"
    file.puts "<h1 class='margin30 no-margin-left no-margin-right' >"
    file.puts "kyn firewall ruleset"
    file.puts "<div class='margin20'></div>"
    file.puts "<div class='panel success collapsed' data-role='panel'>"
    file.puts "<div class='heading'>"
    file.puts "<span class='icon mif-database'></span>"
    file.puts "<span class='title text-shadow'>Ruleset tested in #{Time.at(Time.now - $start_time).utc.strftime("%M:%S")} min - #{getRules.count} rules for #{$hits.toDots} hits in #{$bytes.toFilesize} traffic from #{DateTime.parse($starttime.to_s).strftime('%b %d')} to #{DateTime.parse($endtime.to_s).strftime('%b %d')}</span>"
    file.puts "</div>"
    file.puts "<div class='content bg-white'>"
    file.puts "<table id='example_table' data-page-length='10' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    file.puts "<thead class='text-shadow'><tr><th style='background-color: #D4D4D4;text-align:center'>No</th><th>Source</th><th>Destination</th><th>Service</th><th style='text-align=left'>Action</th><th>Comment</th></tr></thead>"
    file.puts "<tbody>"
    getStaticRules.each do |x|
      i += 1
      static = 1
      file.puts "<tr>\n<th class='text-shadow'>"
      file.puts "<div>"
      file.puts "#{sprintf('%02d',i)}"
      file.puts "</div>"
      content,dialog = x.getFlow.toHtml(static)
      file.puts content
      dialogs += dialog
      file.puts "<td>\n<img class=icon src=icons/#{x.getAction}_action.png> <a class=\"icon-text\">#{x.getAction}</a>\n</td>"
      file.puts "<td>\n#{x.getPrettyHitcount} hits<br />#{x.getPrettyThroughput}\n</td>"
      file.puts "</tr>"
    end
    getDynamicRules.each do |x|
      i += 1
      static = 0
      file.puts "<tr>\n<th class='text-shadow'>"
      file.puts "#{i}"
      content,dialog = x.getFlow.toHtml(static)
      file.puts content
      dialogs += dialog
      file.puts "<td><img class=icon src=icons/#{x.getAction}_action.png> <a class=icon-text>#{x.getAction}</a></td>"
      file.puts "<td>\n#{x.getPrettyHitcount} hits<br />#{x.getPrettyThroughput}\n</td>"
      file.puts "</tr>"
    end
    file.puts "</tbody>"
    file.puts "</table>"
    file.puts "<div class='margin60'></div>"
    fn = Pathname.new($fname).basename.to_s
    if $topsrc_hits.size > 0
      presum = ($presrc_hits.collect { |k, v| v }).inject(:+)
      newsum = ($topsrc_hits.collect { |k, v| v }).inject(:+)
      if presum.nil?
        percent = 100
      else
        percent = ((newsum * 100.0) / (presum + newsum)).round(2)
      end
      file.puts "<p><center>File: #{fn} - in lockdown mode #{newsum} connections (#{percent}%) would have been dropped.</center></p>"
    else
      file.puts "<p><center>File: #{fn} - in lockdown mode no connections would have been dropped.</center></p>"
    end
    file.puts "<div class='margin20'></div>"
    file.puts "<div class='panel collapsed' data-role='panel'>"
    file.puts "<div class='heading' style='background-color: #D4D4D4'>"
    file.puts "<span class='icon mif-database'></span>"
    file.puts "<span class='title text-shadow'>Top Source and Destination Hosts in new rules</span>"
    file.puts "</div>"
    file.puts "<div class='content bg-white'>"
    file.puts "<table><tbody><tr><td valign='top' width='50%'>"
    file.puts "<div class='margin40 no-margin-top no-margin-bottom'>"
    file.puts "<table style='float:left' id='example2_table' data-page-length='5' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    file.puts "<thead class='text-shadow'>"
    file.puts "<tr><th style='background-color: #D4D4D4;text-align:center'>No.</th><th>Top Source</th><th>Count</th></tr>"
    file.puts "</thead>"
    file.puts "<tbody>"
    num = 1
    Hash[$topsrc_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      file.puts "<tr><th class='text-shadow'>#{num}</th><td><a target='_blank' href=#{IPADMIN + key}>#{key}</a></td><td>#{value.toDots} hits<br />#{$topsrc_bytes[key].toFilesize}</td></tr>"
      num += 1
    end
    file.puts "</tbody>"
    file.puts "</table>"
    file.puts "</div>"
    file.puts "</td><td valign='top'>"
    file.puts "<div class='margin40 no-margin-top no-margin-bottom'>"
    file.puts "<table style='float:left' id='example3_table' data-page-length='5' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    file.puts "<thead class='text-shadow' >"
    file.puts "<tr><th style='background-color: #D4D4D4;text-align:center'>No.</th><th>Top Destination</th><th>Count</th></tr>"
    file.puts "</thead>"
    file.puts "<tbody>"
    num = 1
    Hash[$topdst_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      file.puts "<tr><th class='text-shadow' >#{num}</th><td><a target='_blank' href=#{IPADMIN + key}>#{key}</a></td><td>#{value.toDots} hits<br />#{$topdst_bytes[key].toFilesize}</td></tr>"
      num += 1
    end
    file.puts "</tbody>"
    file.puts "</table>"
    file.puts "</div>"
    file.puts "</td></tr></table>"
    file.puts "</div>"
    file.puts "<div class='margin20'></div>"
    file.puts "</div>"
    file.puts "<div class='panel collapsed' data-role='panel'>"
    file.puts "<div class='heading' style='background-color: #D4D4D4'>"
    file.puts "<span class='icon mif-database'></span>"
    file.puts "<span class='title text-shadow'>Top Source and Destination Hosts in static rules</span>"
    file.puts "</div>"
    file.puts "<div class='content bg-white'>"
    file.puts "<table><tbody><tr><td valign='top' width='50%'>"
    file.puts "<div class='margin40 no-margin-top no-margin-bottom'>"
    file.puts "<table style='float:left' id='example4_table' data-page-length='5' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    file.puts "<thead class='text-shadow' >"
    file.puts "<tr><th style='background-color: #D4D4D4;text-align:center'>No.</th><th>Top Source</th><th>Count</th></tr>"
    file.puts "</thead>"
    file.puts "<tbody>"
    num = 1
    Hash[$presrc_hits.sort_by { |_k, v| -v }[0..99]].map do |key, value|
      if (value > 0)
        file.puts "<tr><th class='text-shadow'>#{num}</th><td><a target='_blank' href=#{IPADMIN + key}>#{key}</a></td><td>#{value.toDots} hits<br />#{$presrc_bytes[key].toFilesize}</td></tr>"
        num += 1
      end
    end
    file.puts "</tbody>"
    file.puts "</table>"
    file.puts "</div>"
    file.puts "</td><td valign='top'>"
    file.puts "<div class='margin40 no-margin-top no-margin-bottom'>"
    file.puts "<table style='float:left' id='example5_table' data-page-length='5' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    file.puts "<thead class='text-shadow' >"
    file.puts "<tr><th style='background-color: #D4D4D4;text-align:center'>No.</th><th>Top Destination</th><th>Count</th></tr>"
    file.puts "</thead>"
    file.puts "<tbody>"
    num = 1
    Hash[$predst_hits.sort_by { |k, v| -v }[0..99]].map do |key, value|
      if value > 0
        file.puts "<tr><th class='text-shadow'>#{num}</th><td><a target='_blank' href=#{IPADMIN + key}>#{key}</a></td><td>#{value.toDots} hits<br />#{$predst_bytes[key].toFilesize}</td></tr>"
        num += 1
      end
    end
    file.puts "</tbody>"
    file.puts "</table>"
    file.puts "</div>"
    file.puts "</td></tr></table>"
    file.puts "</div>"
    #file.puts "<div class='margin20'></div>"
    #file.puts "</div>"
    #file.puts "<div class='margin20'></div>"
    #file.puts "<div class='panel collapsed' data-role='panel'>"
    #file.puts "<div class='heading' style='background-color: #D4D4D4'>"
    #file.puts "<span class='icon mif-database'></span>"
    #file.puts "<span class='title text-shadow'>Detailed network and service object and ACL definitions</span>"
    #file.puts "</div>"
    #file.puts "<div class='content bg-white'>"
    #file.puts "<table id='example6_table' data-page-length='10' class='dataTable compact striped hovered cell-hovered border bordered shadow' data-role='datatable' data-searching='true'>"
    #file.puts "<thead class='text-shadow' >"
    #file.puts "<tr><th style='background-color: #D4D4D4;text-align:center'>Type</th><th>Name</th><th>Item</th></tr>"
    #file.puts "</thead>"
    #file.puts "<tbody>"
    #Group.all.sort_by(&:getName).each do |x|
    #  # show only the _nested groups - we remove the _nested string in the method toTable
    #  next if x.getName !~ /_nested$/
    #  file.puts x.toTable
    #end
    #ServiceGroup.all.sort_by(&:getName).each do |x|
    #  file.puts x.toTable
    #end
    #@rules.each do |x|
    #  file.puts "#{x.getFlow.toTable}"
    #end
    #file.puts "</tbody>"
    #file.puts "</table>"
    #file.puts "</div>"
    file.puts "<div class='margin20'></div>"
    file.puts "</div>"
    file.puts "<div class='margin20'></div>"
    file.puts "</div>"
    file.puts "</div>"
    file.puts "<div class='margin20'></div>"
    file.puts "<div class='panel warning collapsed' data-role='panel'>"
    file.puts "<div class='heading'>"
    file.puts "<span class='icon mif-file-download'></span>"
    sacllines = 0
    nacllines = 0
    s = "no ip access-list extended #{$aclname}\nip access-list extended #{$aclname}\n permit tcp any any established\n!\n"
    n = ""
    getStaticRules.sort_by { |a| a.getHitcount }.reverse.each do |x|
      # skip new internal
      if x.getFlow.getSrc.instance_of?(Group)
        x.getFlow.getSrc.toCiscoFlat.split("\n").each do |line|
          s += line.to_s + "\n"
          sacllines += 1
        end
      end
      if x.getFlow.getDst.instance_of?(Group)
        x.getFlow.getDst.toCiscoFlat.split("\n").each do |line|
          s += line.to_s + "\n"
          sacllines += 1
        end
      end
      if x.getFlow.getService.instance_of?(ServiceGroup)
        x.getFlow.getService.toCisco.split("\n").each do |line|
          s += line.to_s + "\n"
          sacllines += 1
        end
      end
      x.getFlow.toCisco(x.getAction).split("\n").each do |line|
        s += line.to_s + "\n"
        sacllines += 1
      end
    end
    getDynamicRules.sort_by { |a| a.getHitcount }.reverse.each do |x|
      if x.getFlow.getSrc.instance_of?(Group)
        x.getFlow.getSrc.toCiscoFlat.split("\n").each do |line|
          n += line.to_s + "\n"
          nacllines += 1
        end
      end
      if x.getFlow.getDst.instance_of?(Group)
        x.getFlow.getDst.toCiscoFlat.split("\n").each do |line|
          n += line.to_s + "\n"
          nacllines += 1
        end
      end
      if x.getFlow.getService.instance_of?(ServiceGroup)
        x.getFlow.getService.toCisco.split("\n").each do |line|
          n += line.to_s + "\n"
          nacllines += 1
        end
      end
      x.getFlow.toCisco(x.getAction).split("\n").each do |line|
        n += line.to_s + "\n"
        nacllines += 1
      end
    end
    file.puts "<span class='title text-shadow'>Resulting Cisco ACL Rules - Base #{sacllines} lines + New #{nacllines} lines generated on #{Time.now.strftime('%Y-%m-%d - %H:%M:%S')}</span>"
    file.puts "</div>"
    file.puts "<div class='content bg-white'>"
    file.puts "<div class='example' data-text='Applicability'>"
    file.puts "<pre style='line-heigth:1em;'>"
    file.puts "! This ACL was generated on #{Time.now.strftime('%Y-%m-%d %H:%M:%S')} with logs (up to 2 hits per record) from:"
    file.puts "! File: #{fn}"
    file.puts "!"
    file.print "!"
    nex = 0
    $nnames.sort_by { |_k,v| v.getHits }.reverse.each do |key, value|
      next unless key =~ /New_Net/
      next unless value.getHits > 0
      file.print " #{key.gsub(NEWNET + "_", '')} (#{value.getHits.toDots} hits)"
      nex += 1
      file.print "\n!" if nex%3 == 0
    end
    $unknownexporter.each do |key, value|
      file.print " #{key} (#{value.toDots} hits)"
      nex += 1
      file.print "\n!" if nex%3 == 0
    end
    file.print "\n!"
    file.puts "</pre>"
    file.puts "</div>"
    if sacllines > 0
      file.puts "<div class='example' data-text='#{sacllines} lines - base rules'>"
      file.puts "<pre style='line-heigth:1em;'>"
      file.puts s
      file.puts "</pre>"
      file.puts "</div>"
    end
    if nacllines > 0
      file.puts "<div class='example' data-text='#{nacllines} lines - new rules'>"
      file.puts "<pre style='line-heigth:1em;'>"
      file.puts n
      file.puts "</pre>"
      file.puts "</div>"
    end
    file.puts "</div>"
    file.puts "<div class='margin20'></div>"
    file.puts "</div>"
    file.puts "</div>"
    file.puts dialogs
    file.puts "</body>"
    file.puts "</html>"
    file.close
  end


  # Call with print_cisco(filename, "dynamic|all“, mode)
  def printCisco(f, t, m)
    file = File.new("#{f}", m)
    file_base = File.new("#{BASERULES}", m)
    file_new  = File.new("#{NEWRULES}",  m)

    if m == "a"
      file.puts "! New rules" if m == "a"
    else
      fn = Pathname.new($fname).basename.to_s
      file.puts "! This ACL was generated on #{Time.now.strftime('%Y-%m-%d %H:%M:%S')} with logs (up to 2 hits per record) from:"
      file.puts "! File: #{fn}"
      file.print "!"
      nex = 0
      $nnames.sort_by { |_k,v| v.getHits }.reverse.each do |key, value|
        next unless key =~ /New_Net/
        next unless value.getHits > 0
        file.print " #{key.gsub(NEWNET + "_", "")} (#{value.getHits.toDots} hits)"
        nex += 1
        file.print "\n!" if nex%3 == 0
      end
      $unknownexporter.each do |key, value|
        file.print " #{key} (#{value.toDots} hits)"
        nex += 1
        file.print "\n!" if nex%3 == 0
      end
      file.puts "\n!"
      file.puts "no ip access-list extended #{$aclname}\nip access-list extended #{$aclname}\n permit tcp any any established\n!\n"
      #file_base.puts "no ip access-list extended #{$aclname}\nip access-list extended #{$aclname}\n permit tcp any any established\n!\n"
    end
    r = getDynamicRules if t == "dynamic"
    r = @rules if t == "all"
    r.each do |x|
      # skip new internal
      file.puts "#{x.getFlow.getSrc.toCiscoFlat}" if x.getFlow.getSrc.instance_of?(Group)
      file.puts "#{x.getFlow.getDst.toCiscoFlat}" if x.getFlow.getDst.instance_of?(Group)
      file.puts "#{x.getFlow.getService.toCiscoFlat}" unless x.getFlow.getService.instance_of?(Service)
      file.puts "#{x.getFlow.toCisco(x.getAction)}"
      if (t == "dynamic")
        file_new.puts "#{x.getFlow.getSrc.toCiscoFlat}" if x.getFlow.getSrc.instance_of?(Group)
        file_new.puts "#{x.getFlow.getDst.toCiscoFlat}" if x.getFlow.getDst.instance_of?(Group)
        file_new.puts "#{x.getFlow.getService.toCiscoFlat}" unless x.getFlow.getService.instance_of?(Service)
        file_new.puts "#{x.getFlow.toCisco(x.getAction)}"
      else
        file_base.puts "#{x.getFlow.getSrc.toCiscoFlat}" if x.getFlow.getSrc.instance_of?(Group)
        file_base.puts "#{x.getFlow.getDst.toCiscoFlat}" if x.getFlow.getDst.instance_of?(Group)
        file_base.puts "#{x.getFlow.getService.toCiscoFlat}" unless x.getFlow.getService.instance_of?(Service)
        file_base.puts "#{x.getFlow.toCisco(x.getAction)}"
      end
    end
    file.close
    file_new.close
    file_base.close
  end




  def clearHitcount
    for i in 0..@rules.size - 1
      @rules[i].clearHitcount
    end
  end

  # Mask helper function
  def cidrToNetmask(cidr)
    require "ipaddr"
    IPAddr.new("255.255.255.255").mask(cidr).to_s
  end

  def spawn
    n = Ruleset.new
    for i in 0..@rules.size - 1
      n.add(@rules[i].spawn)
    end
    n
  end

end

#=
#===> Main Starts here
#=

#$t1 = Profiler.new
#$t2 = Profiler.new
#$t3 = Profiler.new


$hits = 0
$bytes = 0

$topsrc_hits = {}
$topsrc_bytes = {}
$topdst_hits = {}
$topdst_bytes = {}

$presrc_hits = {}
$presrc_bytes = {}
$predst_hits = {}
$predst_bytes = {}

$any["tcp"] = Service.new
$any["tcp"].setProto("tcp")
$any["tcp"].setPort(ANY)

$any["udp"] = Service.new
$any["udp"].setProto("udp")
$any["udp"].setPort(ANY)

$any["icmp"] = Service.new
$any["icmp"].setProto("icmp")
$any["icmp"].setPort(ANY)

loglines = 0

opts = GetoptLong.new(
  ["--help", "-h", GetoptLong::NO_ARGUMENT],
  ["--eshost", "-e", GetoptLong::REQUIRED_ARGUMENT],
  ["--index", "-i", GetoptLong::REQUIRED_ARGUMENT],
  ["--router", "-r", GetoptLong::REQUIRED_ARGUMENT],
  ["--out", "-o", GetoptLong::REQUIRED_ARGUMENT],
  ["--base", "-b", GetoptLong::OPTIONAL_ARGUMENT],
  ["--maxgrp", "-m", GetoptLong::OPTIONAL_ARGUMENT],
  ["--subnet", "-s", GetoptLong::OPTIONAL_ARGUMENT],
  ["--any", "-a", GetoptLong::OPTIONAL_ARGUMENT],
  ["--filter", "-f", GetoptLong::OPTIONAL_ARGUMENT],
  ["--xdebug", "-x", GetoptLong::OPTIONAL_ARGUMENT],
  ["--merge", "-y", GetoptLong::NO_ARGUMENT],
  ["--group", "-g", GetoptLong::NO_ARGUMENT],
  ["--policy", "-p", GetoptLong::REQUIRED_ARGUMENT],
  ["--zany", "-z", GetoptLong::REQUIRED_ARGUMENT],
  ["--chunk", "-c", GetoptLong::OPTIONAL_ARGUMENT]
)


$debug = false
$fname = "rulebase.txt"
$chunk = 1000
fname_base = nil
$resume = nil
$maxnet = MAX_NET
maxgrp = MAX_GRP
maxprt = MAX_PRT
$discover = nil
filter = ""
$override = false
bydst = false
index = nil
eshost = nil
router  = nil
policy = POLICY
maxinet = MAX_INET

opts.each do |opt, arg|
  case opt
  when "--help"
    puts <<-EOF
  es2acl Version #{VERSION} | Syntax: es2acl --in <index name> [--out <output file name>] [--lines <number of log lines] [--base <basic rules file name>] [--chunk <chunk size>] [--discover <1|0>] [--maxgrp <int>] [--subnet <1-32>] [--any <int>]
  --eshost   Name of elasticsearch host (e.g. "http://eshost:9200")
  --in       Name of netflow index
  --router   Name of the router ("all" to process all routers)
  --out      Specifies the base file name of input file will be used for output files. (Default: "ruleset")
  --policy   Name of ES index to write to. (Default: "#{POLICY}")
  --base     Basic rule set to be imported. Parameter specifies ACL file in Cisco format. (Default: Generate rules from Netflow data without using basic rules)
  --chunk    Performance tuning parameter. (Default: 1000)
  --filter   Only process sources matching the group specified
  --maxgrp   Maximum number of additionally allowed connections during grouping (Default: #{MAX_GRP})
  --subnet   Largest subnet mask allowed when superneting two hosts (0-32) (Default: #{32 - MAX_NET})
  --any      Number of different services that will be replaced with "any" (Default: #{MAX_PRT})
  --zany     Number of different sources/destination in a rule to be replaced with "any" (Default: #{MAX_INET})
  --group    Group rules with common destination
    EOF
    exit 0
  when "--index"
    index = arg
  when "--eshost"
    eshost = arg
  when "--router"
    router = arg
  when "--out"
    fname_base = arg
  when "--lines"
    loglines = arg.to_i
  when "--base"
    $resume = arg
  when "--zany"
    maxinet = arg.to_i
  when "--chunk"
    if arg.to_i != 0
      $chunk = arg.to_i
    else
      puts "Invalid chunk specified"
      exit 1
    end
  when "--filter"
    filter = arg
  when "--subnet"
    s = arg.to_i
    if (s < 0) || (s > 32)
      puts "Invalid argument for subnet"
      exit 1
    else
      $maxnet = 32 - s
    end
  when "--any"
    maxprt = arg.to_i
  when "--merge"
    $override= true
  when "--maxgrp"
    maxgrp = arg.to_i
  when "--xdebug"
    $debug = true
  when "--discover"
    $discover = true if arg.to_i == 1
  when "--group"
    bydst = true
  when "--policy"
    policy = arg
  end
end

if (index == nil || eshost == nil)
  puts "No input host/index specified. Please try 'xml2acl -h'"
  exit 0
end




$starttime = Timestamp.new("31.12.2100 13:33:46,645691")
$endtime   = Timestamp.new("24.09.1978 05:33:46,645691")
$total = 0




$esclient = Elasticsearch::Client.new url: "#{eshost}", timeout: 1800, log: false, transport_options: { request: { timeout: 1800 } }

routerlist = Array.new
if (router.upcase == "ALL")
  routerdata = $esclient.search index: "#{index}", size: 0, body: { query: { match_all: {} }, aggs: { routers: { terms: { field: "RouterIPAddress"}}} }
  for x in routerdata["aggregations"]["routers"]["buckets"]
    r = x["key"].split(/\,/)
    routerlist.push(r)
    routerlist.flatten!
    routerlist.uniq!
  end
else
  routerlist.push(router)
end

fnameprefix = fname_base

for router in routerlist # Iterate over all routers

  fname_base = "#{fnameprefix}_#{router}"
  fname_htm = "#{fname_base}.htm"
  fname_fla = "#{fname_base}.cisco"
  fname_acl = "#{fname_base}.acl"
  fname_txt = "#{fname_base}.txt"
  fname_ipa = "#{fname_base}.ipam"
  fname_jso = "#{fname_base}"

  if $debug
    fname_debug = "#{fname_base}.dbg"
    $debug_file = File.new("#{fname_debug}", "w")
  end


  puts "Maxinet: #{maxinet}"

  puts "Reading index #{index}. Writing output to #{fname_htm}, #{fname_acl} and #{fname_txt}."


  # Read initial rulebase
  ruleset = Ruleset.new
  gfile = File.new(fname_acl, "w")
  if !$resume.nil? && File.file?($resume)
    puts "====================== Initial Ruleset ====================================="
    ruleset.read($resume)
    hfile = File.new($resume, "r")
    while (line = hfile.gets)
      gfile.puts(line)
    end
    ruleset.print
  else
    puts "Base policy file does not exist. Ignoring."
  end
  gfile.close

  # Process Traffic data
  offset = 0




  rawdata = $esclient.search index: "#{index}", scroll: "5m", size: $chunk, body: { query: { term: { RouterIPAddress: "#{router}" } } }



  while not rawdata['hits']['hits'].empty?
  # while (!file.eof?) && ((loglines == 0) || ($total < loglines))
    l = 0
    x = rawdata["hits"]["hits"]
    action = "permit"

    # puts "Processing records #{offset.toDots} - #{(offset + l).toDots} from #{$filelength.toDots} total (#{(100.0 / $filelength * offset).to_i} %)"
    offset += l

    #
    # Build policy from log
    #
    # puts x
    x.each do |entry|
      fields = entry["_source"]
      # Read fields from CP log
      src = fields["SourceAddress"]
      dst = fields["DestinationAddress"]

      bytes = fields["Bytes"]

      cliexporter = ""
      cliifindex = 0
      cliexporter2 = "" # NA in checkpoint
      srvexporter  = "" # NA in checkpoint
      srvexporter2 = "" # NA in checkpoint


      # 50;20Sep2015;23:59:00
      proto = fields["Protocol"]
      if (proto == 6)
        proto = "tcp"
      elsif (proto == 0)
        proto = "icmp"
      elsif (proto == 17)
        proto = "udp"
      else
        #proto = "ip"
      end


      dport = fields["DestinationPort"].to_i
      dport = -1 if dport == ""

      da = fields["StartTime"].to_s
      #$starttime = Timestamp.new("31.12.2100 13:33:46,645691")
      #2016-10-05 03:57:23 +0200
      th = Time.at(da[0,10].to_i).to_s
      time = Timestamp.new("#{th[8,2]}.#{th[5,2]}.#{th[0,4]} #{th[11,2]}:#{th[14,2]}:#{th[17,2]},000000")

      # Adjust $starttime/$endtime if necessary
      $endtime = time if time.laterThan($endtime) == 1
      $starttime = time if $starttime.laterThan(time) == 1


      $hits += 1       # global counter
      $bytes += bytes  # global counter

      # Create a service object
      p = Service.new
      p.setProto(proto)
      p.setPort(dport)
      s = Ip.new(src, "255.255.255.255")
      d = Ip.new(dst, "255.255.255.255")
      f = Flow.new(s, d, p)

      if ((src != "") && (dst != "") && ((filter == "") || ($nnames["#{filter}"].contains(s) == 1) || ($nnames["#{filter}"].contains(d) == 1)))
        status = ruleset.match(f, 1, bytes)
        # new value to array if not already there
        $presrc_hits[src]  = 0 if $presrc_hits[src].nil?
        $predst_hits[dst]  = 0 if $predst_hits[dst].nil?
        $presrc_bytes[src] = 0 if $presrc_bytes[src].nil?
        $predst_bytes[dst] = 0 if $predst_bytes[dst].nil?
        $topsrc_hits[src]  = 0 if $topsrc_hits[src].nil?
        $topdst_hits[dst]  = 0 if $topdst_hits[dst].nil?
        $topsrc_bytes[src] = 0 if $topsrc_bytes[src].nil?
        $topdst_bytes[dst] = 0 if $topdst_bytes[dst].nil?

        if (status == 0)
          # Add rule
          r = Rule.new(f, action)
          ruleset.add(r)
          ruleset.match(f, 1, bytes)
        else
          # Update Hitcount for predefined rules.
          if ruleset.getRule(status - 1).getStatic == 1
            $presrc_hits[src] += 1
            $predst_hits[dst] += 1
            $presrc_bytes[src] += bytes
            $predst_bytes[dst] += bytes
          end
        end

        if (status == 0) || (ruleset.getRule(status - 1).getStatic == 0)
          # Update Hitcount for dynamic rules.
          $topsrc_hits[src] += 1
          $topdst_hits[dst] += 1
          $topsrc_bytes[src] += bytes
          $topdst_bytes[dst] += bytes
        end
      else
        $debug_file.puts "Found a broken entry: #{entry}" if $debug
      end

    end

  ############################################
    puts "After new flows have been read:  #{ruleset.getRules.size} (2^#{ruleset.getSizeLog} allowed flows)"
    ruleset.print if $debug


    ruleset.getRules.each do |ru|
      if ru.getStatic == 0
        $debug_file.puts "SUPERNET Start: #{ru.getFlow.getSrc} and #{ru.getFlow.getDst}" if $debug
        ru.getFlow.getSrc.supernet
        ru.getFlow.getDst.supernet
      end
    end

    ############################################
    puts "After Superneting:                   #{ruleset.getRules.size} (2^#{ruleset.getSizeLog} allowed flows)"
    # END: Supernets
    ruleset.print if $debug

    print "Grouping"


    # START: Groups
    cache = Hash.new

    r = ruleset.getRules
    update = 1
    pass = 1
    pass = 2 if bydst

    #$t1.start
    while (update > 0)
      update = 0
      print ":"
      for i in 0..ruleset.getRules.size - 1
        for j in  0..ruleset.getRules.size - 1
          if (r[i].modified || r[j].modified) && (i != j) && (r[j].getStatic == 0) && (r[i].getStatic == 0)
            if ((cache[r[i].getId] != nil ) && (cache[r[i].getId][r[j].getId] != nil ))
              dist = cache[r[i].getId][r[j].getId]
            else
              #$t3.start
              dist = r[i].distanceGroup(r[j])
              #$t3.stop
              if cache[r[i].getId] == nil
                cache[r[i].getId] = Hash.new
              end
              cache[r[i].getId][r[j].getId] = dist
            end
            if (!bydst && (dist < maxgrp) && (r[i].getFlow.getService.getProto == r[j].getFlow.getService.getProto)) || (bydst && pass == 2 && (r[i].getFlow.getDst.contains(r[j].getFlow.getDst) == 1)) || (bydst && pass == 1 && r[i].getFlow.getSrc.contains(r[j].getFlow.getSrc) == 1 && r[i].getFlow.getSrc.instance_of?(Ip) && r[j].getFlow.getSrc.instance_of?(Ip))
              srcmatch = r[i].getFlow.getSrc.contains(r[j].getFlow.getSrc)
              dstmatch = r[i].getFlow.getDst.contains(r[j].getFlow.getDst)
              prtmatch = r[i].getFlow.getService.matches(r[j].getFlow.getService)
              puts "===> ADD Combining: #{r[i]} and #{r[j]}: #{dist} (#{srcmatch}/#{dstmatch}/#{prtmatch})" if $xdebug
              $debug_file.puts "===> ADD Combining: #{r[i]} and #{r[j]}: #{dist} (#{srcmatch}/#{dstmatch}/#{prtmatch})" if $debug
              if prtmatch == 0 # Start prtmacth
                if !r[i].getFlow.getService.instance_of?(ServiceGroup)
                  sg = ServiceGroup.new
                  sg.setProto(r[i].getFlow.getService.getProto)
                  sg.addService(r[i].getFlow.getService)
                  sg.addService(r[j].getFlow.getService)

                  found = false
                  $sgroups.each do |n|
                    if (n.equals(sg) == 1)
                      sg = n
                      found = true
                    end
                  end
                  $sgroups.push(sg) if (found == false)

                  r[i].getFlow.setService(sg)
                  puts "New Group: #{r[i].getFlow.getService} (was: #{r[i].getFlow.getService}, #{r[j].getFlow.getService})" if $xdebug
                else
                  r[i].getFlow.getService.addService(r[j].getFlow.getService)
                  puts "Update to Group: #{r[i].getFlow.getService}" if $xdebug
                end
              end # prtmatch
              if srcmatch == 0 # start srcmatch
                #$t4.start
                if r[i].getFlow.getSrc.instance_of?(Group)
                  if r[j].getFlow.getSrc.instance_of?(Group)
                    r[j].getFlow.getSrc.getIps.each do |o|
                      r[i].getFlow.getSrc.addIp(o, 1)
                    end
                  else
                    r[i].getFlow.getSrc.addIp(r[j].getFlow.getSrc, 1)
                  end
                else
                  # Create new group
                  g = Group.new
                  g.addIp(r[i].getFlow.getSrc, 1)
                  if r[j].getFlow.getSrc.instance_of?(Group)
                    r[j].getFlow.getSrc.getIps.each do |o|
                      g.addIp(o, 1)
                    end
                  else
                    g.addIp(r[j].getFlow.getSrc, 1)
                  end
                  found = false
                  $ngroups.each do |n|
                    next if n.size != g.size
                    next if n.hash != g.hash
                    if n.equals(g) == 1
                      g = n
                      found = true
                    end
                  end
                  $ngroups.push(g) if found == false
                  g.sortByMask
                  r[i].getFlow.setSrc(g)
                  puts "combined to: #{r[i]}" if $xdebug
                end
                #$t4.stop
              end # srcmatch
              if dstmatch == 0
                if r[i].getFlow.getDst.instance_of?(Group)
                  if r[j].getFlow.getDst.instance_of?(Group)
                    r[j].getFlow.getDst.getIps.each do |o|
                      r[i].getFlow.getDst.addIp(o, 1)
                    end
                  else
                    r[i].getFlow.getDst.addIp(r[j].getFlow.getDst, 1)
                  end
                  puts "ADD: #{r[i].getFlow.getDst}" if $xdebug
                else
                  # Create new group
                  g = Group.new
                  g.addIp(r[i].getFlow.getDst, 1)
                  if r[j].getFlow.getDst.instance_of?(Group)
                    r[j].getFlow.getDst.getIps.each do |o|
                      g.addIp(o, 1)
                    end
                  else
                    g.addIp(r[j].getFlow.getDst, 1)
                  end
                  found = false
                  $ngroups.each do |n|
                    next if n.size != g.size
                    next if n.hash != g.hash
                    if n.equals(g) == 1
                      g = n
                      found = true
                    end
                  end
                  $ngroups.push(g) if found == false
                  g.sortByMask
                  r[i].getFlow.setDst(g)
                  puts "combined to: #{r[i]}" if $xdebug
                end
              end # dstmatch
              oldid_i = r[i].getId
              oldid_j = r[j].getId
              r[i].mod
              r[i].setHitcount(r[i].getHitcount + r[j].getHitcount)
              r[i].setThroughput(r[i].getThroughput + r[j].getThroughput)
              #for k in ruleset.getRules
              #  if cache[k.getId] == nil
              #    cache[k.getId] = Hash.new
              #  end
              #  if (cache[k.getId][oldid_i] != nil) && (cache[k.getId][oldid_j] != nil)
              #    cache[k.getId][r[i].getId] = cache[k.getId][oldid_i] + cache[k.getId][oldid_j]
              #  end
              #end
              ruleset.del(j)
              puts "deleted #{j}" if $xdebug
              update = 1
              print "."
              break
            end # IF dist
          end # IF i!=j
        end # for j
        if update == 1
          break
        else
          if j < i
            r[i - 1].ack
          else
            r[i].ack
          end
        end
      end # for i
      if (update == 0 && pass == 2)
        update = 1
        pass = 1
        puts "Pass 1 finished."
        for d in 0..r.size-1
          r[d].mod
        end
      end
    end # while

    #$t1.stop
    puts "\n"
    # END: Groups


    ruleset.sort
    ruleset.print

    ############################################
    puts "\n"
    puts "After Grouping:                      #{ruleset.getRules.size} (2^#{ruleset.getSizeLog} allowed flows)"
    ruleset.print if $debug

    # START: src/dst "Any"
    r = ruleset.getRules
    for i in 0..ruleset.getRules.size - 1
      puts "#{r[i]}: #{r[i].getFlow.getSrc.groupmembers} <-> #{maxinet}"
      if (r[i].getFlow.getSrc.groupmembers > maxinet) && (r[i].getStatic == 0)
        r[i].getFlow.setSrc(Ip.new("0.0.0.0", "0.0.0.0"))
      end
      if (r[i].getFlow.getDst.groupmembers > maxinet) && (r[i].getStatic == 0)
        r[i].getFlow.setDst(Ip.new("0.0.0.0", "0.0.0.0"))
      end
    end


    ############################################
    puts "After replacing src/dst with ANY:   #{ruleset.getRules.size} (2^#{ruleset.getSizeLog} allowed flows)"
    ruleset.print if $debug


    # START: Service "Any"
    r = ruleset.getRules
    for i in 0..ruleset.getRules.size - 1
      if (r[i].getFlow.getService.getSize > maxprt) && (r[i].getStatic == 0) && !$any[r[i].getFlow.getService.getProto].nil?
        if r[i].getFlow.getService.instance_of?(ServiceGroup)
          puts "Adding #{$any[r[i].getFlow.getService.getProto]} to #{r[i].getFlow.getService}"
          r[i].getFlow.getService.addService($any[r[i].getFlow.getService.getProto])
        else
          sg = ServiceGroup.new
          sg.setProto(r[i].getFlow.getService.getProto)
          sg.addService($any[r[i].getFlow.getService.getProto])
          r[i].getFlow.setService(sg)
        end
      end
    end


    ############################################
    puts "After replacing service  with ANY:   #{ruleset.getRules.size} (2^#{ruleset.getSizeLog} allowed flows)"
    ruleset.print if $debug

    puts "==============================================================================================================================================="
    ruleset.sort
    ruleset.print
    puts "==============================================================================================================================================="

    scrollid = rawdata["_scroll_id"]
    rawdata = $esclient.scroll scroll: '5m', scroll_id: scrollid
    puts rawdata["hits"]["hits"].size
  end

  # sort hash
  $unknownexporter.values.sort.reverse

  ruleset.print

  # Add drop rule at end
  p = Service.new
  p.setProto("ip")
  p.setPort(ANY)
  s = Ip.new("0.0.0.0", "0.0.0.0")
  d = Ip.new("0.0.0.0", "0.0.0.0")
  f = Flow.new(s, d, p)
  r = Rule.new(f, "deny")
  ruleset.add(r)




  #print json to Elastic
  if policy.upcase != "NONE"
    ruleset.printElastic(router, policy)
  end
  # Print Cisco-style with flat groups
  ruleset.printCisco(fname_fla, "all", "w")

  # Print Cisco-style with old rules 1:1
  r = []
  r.push(ruleset.getRule(1)) if $discover
  r.concat(ruleset.getDynamicRules)
  z = Ruleset.new
  r.each do |y|
    z.add(y)
  end
  z.printCisco(fname_acl, "dynamic", "a")

  # Print HTML-style
  ruleset.printHtml(fname_htm)

  puts "=== Top 10 Sources by hitcount (new rules)==="
  Hash[$topsrc_hits.sort_by { |_k, v| -v }[0..9]].each do |key, value|
    puts "#{key}: #{value.toDots} hits / #{$topsrc_bytes[key].toFilesize}"
  end

  puts "=== Top 10 Destinations by hitcount (new rules)==="
  Hash[$topdst_hits.sort_by { |_k, v| -v }[0..9]].each do |key, value|
    puts "#{key}: #{value.toDots} hits / #{$topdst_bytes[key].toFilesize}"
  end


  puts "=== Top 10 Sources by hitcount (predefined rules)==="
  Hash[$presrc_hits.sort_by { |_k, v| -v }[0..9]].each do |key, value|
    if (value > 0)
      puts "#{key}: #{value.toDots} hits / #{$presrc_bytes[key].toFilesize}"
    end
  end

  puts "=== Top 10 Destinations by hitcount (predefined rules)==="
  Hash[$predst_hits.sort_by { |_k, v| -v }[0..9]].each do |key, value|
    if (value > 0)
      puts "#{key}: #{value.toDots} hits / #{$predst_bytes[key].toFilesize}"
    end
  end

  if $topsrc_hits.size > 0
    presum = ($presrc_hits.collect { |_k, v| v }).inject(:+)
    newsum = ($topsrc_hits.collect { |_k, v| v }).inject(:+)
    if !presum.nil?
      percent = 100
    else
      percent = ((newsum * 100.0) / (presum + newsum)).round(2)
    end
    puts "In lockdown mode #{newsum} connections (#{percent}%) would have been dropped."
  else
    puts "In lockdown mode no connections would have been dropped."
  end

  puts "======================================================================================"
  puts "=================================== Statistics ======================================="
  puts "======================================================================================"
  puts "First session: #{$starttime}"
  puts "Last session:  #{$endtime}"
  puts "Total # of hits: #{$hits.toDots} / #{$bytes.toFilesize}"
  puts "Addresses seen: #{$topsrc_hits.size} sources / #{$topdst_hits.size} destinations."
  puts "======================================================================================"

  # Display only if discover switch was present
  if $discover
    $unknownexporter.each do |key, value|
      puts "WARNING: Unknown exporter #{key}: #{value.toDots} hits"
    end

    puts "Discovered internal devices/networks:"
    f = File.new("#{fname_ipa}", "w")
    id = 1

    $nnames.sort_by { |_k,v| v.getHits }.reverse.each do |key, value|
      next unless key =~ /New_Net/
      # Only if more than own IP
      next unless value.getHits > 1
      puts " #{key.gsub(NEWNET + "_", "")} (#{value.getHits.toDots} hits):"
      n = key.dup
      f.puts '<host-group id="' + "#{id}" + '" name="' + "#{n}" + '" host-baselines="true" suppress-excluded-services="true" inverse-suppression="false" host-trap="false">'
      id += 1
      value.getIps.sort.each do |y|
        puts "  #{y}"
        f.puts "<ip-address-ranges>#{y}</ip-address-ranges>"
      end
      f.puts "</host-group>"
    end

    f.close
  end
end # Iterate over routers



