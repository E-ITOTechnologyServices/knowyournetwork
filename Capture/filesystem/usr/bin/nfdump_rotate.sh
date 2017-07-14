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




ESHOST="$(docker inspect --format '{{ .NetworkSettings.Networks.kyn_default.IPAddress }}' kyn_elasticsearch_1)"
if [ "$?" -ne "0" ]; then
  echo "Can't find Elasticsearch IP."
  exit 1
fi

PIDFILE=/var/run/`basename $0`.pid
BACKUPPATH=/var/data/nfdump_backup/
DATAPATH=/var/data/nfdump

mkdir -p $BACKUPPATH

if [ -f $PIDFILE ]; then
  echo "$0 is already running or has been interrupted."
  echo -n "$PIDFILE -> "
  cat $PIDFILE
  exit 255
fi

echo $$ > $PIDFILE


nfdump2es () {
  /usr/bin/nfdump -q -B -N -S -o elastic_db -r $file -g kyn-netflow -G http://${ESHOST}:9200/ -P kyn > /dev/null;
}


unset http_proxy
unset https_proxy
shopt -s nullglob

FILES=$DATAPATH/nfcapd.20*

for file in $FILES
do
    echo Working on $file
    # import nfdump to es
    nfdump2es

    while [ $? -ne 0 ]; do
        echo "Failed import (std)"
        sleep 30
        nfdump2es
    done
    
    mv $file $BACKUPPATH
done

rm $PIDFILE

