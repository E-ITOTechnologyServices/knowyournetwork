#!/bin/bash

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




SPAN_IF=${KYN_YAF_INTERFACE:-eth0}
DATAPATH=/var/data/yaf/
mkdir -p $DATAPATH
PREFIX=yaf
OUT=${DATAPATH}${PREFIX}

/usr/bin/yaf --mac --live pcap --in $SPAN_IF --flow-stats --applabel --p0fprint  --max-payload=500 --idle-timeout 600 --active-timeout 14400 -R 300 --out $OUT --lock 
