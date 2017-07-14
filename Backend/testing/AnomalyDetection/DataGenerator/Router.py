#
# Package: Router
#
# Author: Steve Chaplin
#
# Date: 19-09-2016
#
# Purpose: This is a class that can generate a router record in JSON
# format according to how it is configured.
#
####################################################################################################
#
from time import gmtime
import random
import math
#
class Router:

	# Format of timestamps
	TIMESTAMP = '{YY:04d}-{MM:02d}-{DD:02d}T{HH:02d}:{MI:02d}:{SS:02d}.{MS:03d}Z'
	# MTU
	MTU = 1500
	# The index record for each flow
	INDEX = '{"index":{"_index":"kyn-demo-routing","_type":"routing"}}'
	# The flow template
	FLOW = '{{"@timestamp": "{timeStamp}", "source": "{sourceAddress}", "destination": "{destinationAddress}", "router": "{routers}"}}'

	def __init__(self):
		self.values = {
			'@timestamp': '',
			'sourceAddress': '10.201.2.70',
			'destinationAddress': '10.79.193.39',
			'routers': [],
			'mtu': self.MTU,
			}

	def __setValue(self, key, value):
		self.values[key] = value

	def __getValue(self, key):
		return self.values[key]

	#
	# Set the timestamp value.
	# Currently, the entire method is replaced to allow the caller to pass strings which
	# are used directly. There is no attempt to set a meaningful time.
	#
	#def setTs(self, ts):
	#	self.__setValue('@timestamp', self.__epochToString(ts))
	def setTs(self, ts):
		if 'now' not in str(ts):
			ts = 'now'
		self.__setValue('@timestamp', ts)

	def setSourceAddress(self, address):
		self.__setValue('sourceAddress', address)

	def setDestinationAddress(self, address):
		self.__setValue('destinationAddress', address)

	def setMTU(self, bytes):
		self.__setValue('mtu', bytes)

	def clearRoute(self):
		self.values['routers'] = []

	def addRouter(self, router):
		self.values['routers'].append(router)

	def output(self):
		print self.INDEX
		print self.FLOW.format(timeStamp=self.__getValue('@timestamp'),
							   sourceAddress=self.__getValue('sourceAddress'),
							   destinationAddress=self.__getValue('destinationAddress'),
							   routers=','.join(self.__getValue('routers')),
							   )

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
