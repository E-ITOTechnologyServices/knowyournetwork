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

BACKUPPATH=/var/data/yaf_backup/
DATAPATH=/var/data/yaf/
mkdir -p $BACKUPPATH

PIDFILE=/var/run/`basename $0`.pid
if [ -f $PIDFILE ]; then
  echo "$0 is already running or has been interrupted."
  echo -n "$PIDFILE -> "
  cat $PIDFILE
  exit 1
fi
echo $$ > $PIDFILE
if [ "$?" -ne "0" ]; then echo "could not create $PIDFILE"; exit 1; fi

yaf2es () {
  super_mediator --in $file --output-mode json --no-stats --out - | yaf2es.rb --eshost $ESHOST
}

shopt -s nullglob
FILES=${DATAPATH}*.yaf
for file in $FILES; do
  if [ -f $file.lock ]; then
    echo "Skipping $file"
    continue
  fi
  echo Working on $file
  yaf2es
  while [ "$?" -ne "0" ]; do
    echo "Failed import"
    sleep 30
    yaf2es
  done

  mv $file $BACKUPPATH

done

rm $PIDFILE
