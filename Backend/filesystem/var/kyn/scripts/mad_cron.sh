#!/bin/sh
/var/kyn/scripts/mad.rb --new=/tmp/mad.txt --backtrace=1 --event --plain
/var/kyn/scripts/event2anomaly.rb
