#!/usr/bin/python
#
# Name: makeFlows.py
#
# Author: Steve Chaplin
#
# Date: 19-09-2016
#
# Purpose: Generate some flow test data.
#
# Notes: Each test case is hard-coded separately (which is not ideal, but will work for now).
# See this for protocol numbers.
# http://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
#
####################################################################################################
#
from time import time
#
import Netflow
import Router
import Logger
#
####################################################################################################
# Test data generation functions.
####################################################################################################
# Test case 3.
# Continuously communicate in an entirely different pattern to the majority of systems.
#####
# More than 10x as many flows from one IP in 15 min compared to previous 15 min.
def test3a():
	global ts, logger
	#
	sourceAddress = '10.201.2.68'
	#
	logger.log('3a', 'More than 10x as many flows from ',
				sourceAddress,
				' in 15 mins')
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationAddress('10.201.3.0')
	#flow.setTs(ts)
	flow.setTs('now-15')
	flow.output()
	#
	for i in range(1, 30 + 1):
		flow.setTs(ts)
		flow.setDestinationAddress('10.201.3.{}'.format(i))
		flow.output()
		ts += 10
#
#####
# More than 10x as many bytes from one IP in 15 min compared to previous 15 min.
def test3b():
	global ts, logger
	#
	sourceAddress = '10.201.2.69'
	#
	logger.log('3b', 'More than 10x as many flows from ',
				sourceAddress,
				' in 15 mins')
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationAddress('10.201.3.4')
	#flow.setTs(ts)
	flow.setTs('now-15')
	flow.setBytes(10)
	flow.output()
	#
	ts += 60 * 20
	#
	# Now generate at least 10 times the numberof bytes (3000 in this case) in the next 15 min.
	# This does it in one flow.
	flow.setTs(ts)
	flow.setDestinationAddress('10.201.3.5')
	flow.setBytes(3000)
	flow.output()
	#
	ts += 1
	#
#
#####
# More than 10x as many flows to one IP in 15 min compared to previous 15 min.
def test3c():
	global ts, logger
	#
	destinationAddress = '10.201.3.10'
	#
	logger.log('3c', 'More than 10x as many flows to ',
				destinationAddress,
				' in 15 mins')
	#
	flow = Netflow.Netflow()
	flow.setDestinationAddress(destinationAddress)
	flow.setSourceAddress('10.201.2.70')
	#flow.setTs(ts)
	flow.setTs('now-15')
	flow.output()
	#
	ts += 60 * 20
	#
	for i in range(1, 30 + 1):
		flow.setSourceAddress('10.201.2.{}'.format(i))
		flow.setTs(ts)
		flow.output()
		ts += 10
#
#####
# More than 10x as many bytes to one IP in 15 min compared to previous 15 min.
def test3d():
	global ts, logger
	#
	destinationAddress = '10.201.3.11'
	#
	logger.log('3d', 'More than 10x as many byes to ',
				destinationAddress,
				' in 15 mins')
	#
	flow = Netflow.Netflow()
	flow.setDestinationAddress(destinationAddress)
	flow.setSourceAddress('10.201.2.100')
	#flow.setTs(ts)
	flow.setTs('now-15')
	flow.setBytes(10)
	flow.output()
	#
	ts += 60 * 20
	#
	#
	# Now generate at least 10 times the number of bytes (3000 in this case) in the next 15 min.
	# This does it in one flow.
	#
	flow.setTs(ts)
	flow.setSourceAddress('10.201.2.101')
	flow.setBytes(3000)
	flow.output()
	#
	ts += 1
#
#####
def test3():
	test3a()
	test3b()
	test3c()
	test3d()
#
####################################################################################################
# Test case 4
# Flow to destination port 21 or 23 with bytes > 0
#####
def test4():
	global ts, logger
	#
	logger.log('4', 'Flows to destination port 21 and 23 with bytes > 0')
	#
	flow = Netflow.Netflow()
	flow.setBytes(1000)
	flow.setSourceAddress('10.201.2.110')
	flow.setDestinationAddress('10.201.3.10')
	flow.setDestinationPort(21)
	#
	flow.setTs(ts)
	flow.output()
	#
	ts += 10 # 10 seconds later, a different source to port 23 on a different host
	#
	flow.setSourceAddress('10.201.2.111')
	flow.setDestinationAddress('10.201.3.11')
	flow.setDestinationPort(23)
	flow.setTs(ts)
	flow.output()
	#
	ts += 1
#
####################################################################################################
# Test case 5
# Continuous communication in entirely different pattern.
#####
# Upload 1Mb every minute many times to same destination using encrypted connection.
def test5a():
	global ts, logger
	#
	destination = '10.201.3.110'
	repeat = 1000
	#
	logger.log('5a', 'Upload 1Mb every minute (',
			repeat,
			' times) on encrypted connection to same destination (',
			destination,
			')')
	#
	flow = Netflow.Netflow()
	flow.setBytes(1024*1024)
	flow.setSourceAddress('10.201.2.110')
	flow.setDestinationPort(22)
	flow.setDestinationAddress(destination)

	for i in range(1, repeat + 1):
		flow.setTs(ts)
		flow.output()
		ts += 60
#
#####
# Attempt to telnet to every router in class C network.
def test5b():
	global ts, logger
	#
	destination = '10.201.{}.1'
	repeat = 255
	#
	logger.log('5b', 'Attempt to telnet to each router (',
				'10.201.*.1) in class C network once a second')
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress('10.201.2.111')
	flow.setDestinationPort(23)
	#
	for i in range(1, repeat + 1):
		flow.setDestinationAddress(destination.format(i))
		flow.setTs(ts)
		ts +=1
		flow.output()
#
#####
# Attempt to repeatedly ftp large packets to same destination.
def test5c():
	global ts, logger
	#
	destination = '10.201.3.200'
	repeat = 100
	mtu = 64 * 1024 - 1
	#
	logger.log('5c', 'Attempt to repeatedly ftp large packets (MTU=',
				mtu,
				') to destination ',
				destination)
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress('10.201.2.111')
	flow.setDestinationAddress(destination)
	flow.setDestinationPort(21)
	flow.setMTU(mtu)
	flow.setBytes(mtu * 3)
	#
	for i in range(1, repeat + 1):
		flow.setTs(ts)
		ts +=10
		flow.output()
#
#####
# Repeatedly upload and download from same host using https.
def test5d():
	global ts, logger
	#
	aEnd = '10.201.2.123'
	bEnd = '10.201.3.202'
	aBytes = 100
	bBytes = 13000
	repeat = 100
	#
	logger.log('5d', 'Repeatedly upload and then download from same remote site (',
				bEnd,
				') using https (C&C?)')
	#
	flow = Netflow.Netflow()
	flow.setDestinationPort(443)
	#
	for i in range(1, repeat + 1):
		flow.setSourceAddress(aEnd)
		flow.setDestinationAddress(bEnd)
		flow.setBytes(aBytes)
		#
		flow.setTs(ts)
		flow.output()
		ts +=1
		#
		flow.setSourceAddress(bEnd)
		flow.setDestinationAddress(aEnd)
		flow.setBytes(bBytes)
		#
		flow.setTs(ts)
		flow.output()
#
#####
# Access dodgey website.
def test5e():
	global ts, logger
	#
	logger.log('5e', 'Download dodgey stuff on https!')
	#
	aEnd = '10.201.2.33'
	bEnd = '31.192.116.24'
	aBytes = 16 * 1024
	repeat = 30
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress(aEnd)
	flow.setDestinationAddress(bEnd)
	flow.setBytes(aBytes)
	flow.setDestinationPort(443)
	#
	for i in range(1, repeat + 1):
		flow.setTs(ts)
		flow.output()
		ts +=3
#
#####
# Use UDP broadcast.
def test5f():
	global ts, logger
	#
	logger.log('5f', 'UDP broadcast 16 bytes 1000 times')
	#
	aEnd = '10.201.2.10'
	bEnd = '239.255.255.255'
	protocol = 17
	aBytes = 16
	repeat = 1000
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress(aEnd)
	flow.setDestinationAddress(bEnd)
	flow.setProtocol(protocol)
	flow.setBytes(aBytes)
	#
	for i in range(1, repeat + 1):
		flow.setTs(ts)
		flow.output()
		ts +=5
#
#####
def test5():
	test5a()
	test5b()
	test5c()
	test5d()
	test5e()
	test5f()
#
####################################################################################################
# Test case 6.
# Communication with known botnet command & control servers.
#####
def test6():
	global ts, logger
	#
	sourceAddress = '10.201.2.119'
	#
	logger.log('6', 'Access to command and control servers by ',
				sourceAddress)
	#
	destinationAddresses = [
		'222.186.34.56',
		'202.114.72.243',
		'93.174.93.21',
		'114.215.192.127',
		'71.13.212.6',
		'125.227.90.121',
		'47.88.102.185',
		]
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	#
	for destinationAddress in destinationAddresses:
		flow.setDestinationAddress(destinationAddress)
		flow.setTs(ts)
		flow.output()
		ts += 1
#
####################################################################################################
# Test case 7.
#####
# More than 10 flows with flags "....S." while there is no TCP flow with bytes > 0
# for this IP
def test7():
	global ts, logger
	#
	sourceAddress = '10.201.2.120'
	flags = '....S.'
	#
	logger.log('7', 'More than 10 flows from ',
				sourceAddress,
				' with TCP flags ',
				flags,
				' whilst no flow with bytes > 0')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	#
	for i in range(1, 20 + 1):
		flow.setTs(ts)
		flow.setTCPFlags(flags)
		flow.setDestinationAddress('10.201.3.{}'.format(i))
		flow.setBytes(10)
		flow.output()
		#
		ts += i
#
####################################################################################################
# Test case 8.
#####
# Flow to a new destination that has not been seen before.
# Hard to know if this one will work. I will use a destination address that is potentially
# invalid. Note, I am setting a destination port here since I need to know I can change it
# to a different one in test 9.
#
def test8():
	global ts, logger
	#
	destinationAddress = '255.255.3.0'
	destinationPort = 22
	#
	logger.log('8', 'Flow to a new destination (',
				destinationAddress,
				') that has not been seen before')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress('10.201.2.90')
	flow.setDestinationAddress(destinationAddress)
	flow.setDestinationPort(destinationPort)
	flow.setTs(ts)
	flow.output()
	ts += 1
#
####################################################################################################
# Test case 9.
#####
# Flow to a new destination/port combination that has not been seen before.
# Hard to know if this one will work. I will use the same destination address as test 8
# (so that I know it has been seen), but change the port to a different one.
#
def test9():
	global ts, logger
	#
	destinationAddress = '255.255.3.0'
	destinationPort = 443
	#
	logger.log('9', 'Flow to a new destination/port (',
				destinationAddress , '/' , destinationPort,
				') that has not been seen before')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress('10.201.2.91')
	flow.setDestinationAddress(destinationAddress)
	flow.setDestinationPort(destinationPort)
	flow.setTs(ts)
	flow.output()
	ts += 1
#
####################################################################################################
# Test case 10.
#####
# More than 5 flows to port 22 in the last 15 mins and less than 5 flows to port 22 in
# the last week. I can't guarantee this other than to again try and use an invalid
# ip address.
#
def test10():
	global ts, logger
	#
	logger.log('10', 'More than 5 flows to port 22 in the last 15 mins')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress('255.255.255.0')
	flow.setDestinationPort(22)
	for i in range(1, 10 + 1):
		flow.setTs(ts)
		flow.setDestinationAddress('10.201.3.{}'.format(i))
		flow.output()
		ts += 60; # One flow a minute for 10 mins.
#
####################################################################################################
# Test case 11.
#####
# Detect if a connection is immediately followed by a connection in the reverse direction.
#
def test11():
	global ts, logger
	#
	sourceAddress = '10.201.2.150'
	destinationAddress = '10.201.3.150'
	#
	logger.log('11', 'A connection from ',
				sourceAddress,
				' to ',
				destinationAddress,
				' is immediated followed by one in the reverse direction')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationAddress(destinationAddress)
	flow.setTs(ts)
	flow.output()
	ts += 1
	#
	flow.setSourceAddress(destinationAddress)
	flow.setDestinationAddress(sourceAddress)
	flow.setTs(ts)
	flow.output()
	#
	ts += 1
#
####################################################################################################
# Test case 12.
#####
# Flow with protocol 47
#
def test12():
	global ts, logger
	#
	sourceAddress = '10.201.2.95'
	destinationAddress = '10.201.3.95'
	protocol = 47
	#
	logger.log('12', 'Flow from ',
				sourceAddress,
				' to ',
				destinationAddress,
				' using protocol ',
				protocol)
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationAddress(destinationAddress)
	flow.setProtocol(protocol)
	flow.setTs(ts)
	flow.output()
	#
	ts += 1
#
####################################################################################################
# Test case 13.
#####
# Single IP with flows to more than 100 destinations.
#
def test13():
	global ts, logger
	#
	sourceAddress = '10.201.2.96'
	#
	logger.log('13', 'Flow from ',
				sourceAddress,
				' to more than 100 destinations')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	#
	for i in range(1, 120 + 1):
		flow.setTs(ts)
		flow.setDestinationAddress('10.201.3.{}'.format(i))
		flow.output()
		ts += 1
#
####################################################################################################
# Test case 14.
#####
# Single IP with flows to more than 100 destinations on port 80.
#
def test14():
	global ts, logger
	#
	sourceAddress = '10.201.2.200'
	destinationPort = 80
	#
	logger.log('14', 'Flow from ',
				sourceAddress,
				' to more than 100 destinations on port ',
				destinationPort)
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationPort(destinationPort)
	#
	for i in range(1, 120 + 1):
		flow.setTs(ts)
		flow.setDestinationAddress('10.201.3.{}'.format(i))
		flow.output()
		ts += 1
#
####################################################################################################
# Test case 15.
#####
# Single IP with flows to more than 100 destination ports.
#
def test15():
	global ts, logger
	#
	sourceAddress = '10.201.2.201'
	destinationAddress = '10.201.3.215'
	#
	logger.log('15', 'Flow from ',
				sourceAddress,
				' to more than 100 destination ports')
	#
	flow = Netflow.Netflow()
	#
	flow.setSourceAddress(sourceAddress)
	flow.setDestinationAddress(destinationAddress)
	#
	for i in range(1, 120 + 1):
		flow.setTs(ts)
		flow.setDestinationPort(i)
		flow.output()
		ts += 1
#
####################################################################################################
# Test case 16
#####
#
def test16():
	global ts, logger
	#
	dufRouter = '172.16.1.1'
	normalCount = 30
	#
	logger.log(16, 'Normal route that suddenly varies by adding ',
				dufRouter)
	#
	flow = Netflow.Netflow()
	flow.setSourceAddress('10.1.1.5')
	flow.setDestinationAddress('10.1.2.10')
	#
	# Generate a load of normal routes.
	#
	flow.clearRouters()
	flow.addRouter('10.1.1.1')
	flow.addRouter('10.1.2.1')
	#route.setTs(ts)
	flow.setTs('now-30')
	for i in range(1, normalCount + 1):
		#flow.setTs(ts)
		flow.output()
		ts += 2
	#
	# Add one duf route in the middle.
	#
	flow.clearRouters()
	flow.addRouter('10.1.1.1')
	flow.addRouter(dufRouter)
	flow.addRouter('10.1.2.1')
	#flow.setTs(ts)
	flow.setTs('now-15')
	flow.output()
	ts += 2
	#
	# Now add all the normal ones again.
	#
	flow.clearRouters()
	flow.addRouter('10.1.1.1')
	flow.addRouter('10.1.2.1')
	for i in range(1, normalCount + 1):
		flow.setTs(ts)
		flow.output()
		ts += 2
#
####################################################################################################
# Run all test data generators
#
def makeData():
	test3()
	test4()
	test5()
	test6()
	test7()
	test8()
	test9()
	test10()
	test11()
	test12()
	test13()
	test14()
	test15()
	test16()
#
####################################################################################################
# Generate the data
#
ts = int(time())
logger = Logger.Logger()
logger.off()
makeData()
#
###################################################################################################
