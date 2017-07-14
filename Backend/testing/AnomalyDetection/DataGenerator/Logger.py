#
# Package: Logger
#
# Author: Steve Chaplin
#
# Date: 19-09-2016
#
# Purpose: Write a simple log message
#
####################################################################################################
#
class Logger:

	def __init__(self):
		self.values = {
			'count': 0,
			'on': True,
		}

	def on(self):
		self.values['on'] = True

	def off(self):
		self.values['on'] = False

	def log(self, name, *args):
		self.values['count'] += 1
		if self.values['on']:
			print '# Test:{} Case:{} {}'.format(self.values['count'], name, ''.join(str(i) for i in args))
