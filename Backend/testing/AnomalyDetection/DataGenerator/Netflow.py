#
# Package: Netflow
#
# Author: Steve Chaplin
#
# Date: 19-09-2016
#
# Purpose: This is a class that can generate a netflow record in JSON
# format according to how it is configured.
#
####################################################################################################
#
from time import gmtime
import random
import math
#
class Netflow:

	# Format of timestamps.
	TIMESTAMP = '{YY:04d}-{MM:02d}-{DD:02d}T{HH:02d}:{MI:02d}:{SS:02d}.{MS:03d}Z'
	# Number of seconds after flow start that the record is timestamped.
	TS_DELAY = 2
	# Number of seconds after flow start that the flow ends.
	EVENT_LEN = 1
	# MTU - Used to give a [meaningful?] value to packet count based on bytes.
	MTU = 1500
	INDEX = '{"index":{"_index":"kyn-demo-netflow","_type":"nfdump_0"}}'
	FLOW = '{{"@timestamp":"{timeStamp}","host":"DE-MUNC-KYN-1-e0","StartTime":"{startTime}","EndTime":"{endTime}","Duration":0,"SourceAddress":"{sourceAddress}","DestinationAddress":"{destinationAddress}","SourcePort":{sourcePort},"DestinationPort":{destinationPort},"ForwardingStatus":0,"TCPFlags":"{tcpFlags}","Protocol":"{protocol}","SourceTOS":0,"InputPackets":{inputPackets},"Bytes":{inputBytes},"InterfaceIN":1,"InterfaceOUT":8,"SourceAS":65020,"DestinationAS":0,"SourceMask":16,"DestinationMask":24,"DestinationTOS":0,"Direction":0,"NextHOPAddress":"10.79.192.1","SourceVLAN":0,"DestinationVLAN":211,"InputSourceMACAddress":"6c:9c:ed:25:f7:00","OutputDestinationMACAddress":"f4:4e:05:4f:35:c3","RouterIPAddress":"{routerIPAddress}","EngineID":0,"bps":13066,"pps":25,"bpp":65}}'

	def __init__(self):
		self.values = {
			'@timestamp': '',
			'startTime': '',
			'endTime': '',
			'sourceAddress': '10.201.2.70',
			'destinationAddress': '10.79.193.39',
			'sourcePort': '56530',
			'destinationPort': '135',
			'tcpFlags': '.APRS.',
			'protocol': '6',
			'inputPackets': '10',
			'inputBytes': '600',
			'mtu': self.MTU,
			'routerIPAddress': [ '10.127.184.33' ],
			}

	def __setValue(self, key, value):
		self.values[key] = value

	def __getValue(self, key):
		return self.values[key]

	#
	# Set the various timestamp values using the built in delays.
	# Currently, the entire method is replaced to allow the caller to pass strings which
	# are used directly. There is no attempt to set meaningful times nor to
	# use the built in delay and event length.
	#
	#def setTs(self, ts):
	#	self.__setValue('@timestamp', self.__epochToString(ts + self.TS_DELAY))
	#	self.__setValue('startTime', self.__epochToString(ts))
	#	self.__setValue('endTime', self.__epochToString(ts + self.EVENT_LEN))
	def setTs(self, ts):
		if 'now' not in str(ts):
			ts = 'now'
		self.__setValue('@timestamp', ts)
		self.__setValue('startTime', ts)
		self.__setValue('endTime', ts)

	def setSourceAddress(self, address):
		self.__setValue('sourceAddress', address)

	def setDestinationAddress(self, address):
		self.__setValue('destinationAddress', address)

	def setSourcePort(self, port):
		self.__setValue('sourcePort', port)

	def setDestinationPort(self, port):
		self.__setValue('destinationPort', port)

	def setTCPFlags(self, flags):
		self.__setValue('tcpFlags', flags)

	def setProtocol(self, protocol):
		self.__setValue('protocol', protocol)

	def clearRouters(self):
		self.values['routerIPAddress'] = []

	def addRouter(self, router):
		self.values['routerIPAddress'].append(router)

	#
	# In addition to setting byte count in the flow, this also attempts to set a [fairly]
	# meaningful value for the number of packets - divides bytes by mtu and rounds up.
	#
	def setBytes(self, bytes):
		self.__setValue('inputBytes', bytes)
		self.__setValue('inputPackets', int(math.ceil(float(bytes) / self.__getValue('mtu'))) if bytes > self.__getValue('mtu') else 1)

	def setMTU(self, bytes):
		self.__setValue('mtu', bytes)

	def output(self):
		print self.INDEX
		print self.FLOW.format(timeStamp=self.__getValue('@timestamp'),
							   startTime=self.__getValue('startTime'),
							   endTime=self.__getValue('endTime'),
							   sourceAddress=self.__getValue('sourceAddress'),
							   destinationAddress=self.__getValue('destinationAddress'),
							   sourcePort=self.__getValue('sourcePort'),
							   destinationPort=self.__getValue('destinationPort'),
							   tcpFlags=self.__getValue('tcpFlags'),
							   protocol=self.__getValue('protocol'),
							   inputPackets=self.__getValue('inputPackets'),
							   inputBytes=self.__getValue('inputBytes'),
							   routerIPAddress=','.join(self.__getValue('routerIPAddress')),
							   )

	#
	# Convert supplied epoch to string in the specified format. Didn't use strftime
	# since I wanted to add a random milli-seconds.
	#
	@classmethod
	def __epochToString(cls, epoch):
		ts = gmtime(epoch)
		return cls.TIMESTAMP.format(YY=ts.tm_year,
									MM=ts.tm_mon,
									DD=ts.tm_mday,
									HH=ts.tm_hour,
									MI=ts.tm_min,
									SS=ts.tm_sec,
									MS=random.randint(0, 999))
