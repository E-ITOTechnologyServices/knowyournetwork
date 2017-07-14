#!/bin/sh
#
# Name: import.sh
#
# Author: Tobias Nieberg
#
# Purpose: Load some functional data into an ElasticSearch
# instance.
#
# Usage: import_data.sh [-h host] [-p port] data_file [data_file ...]
#
################################################################
#
check_error() {
	if [ "0" -ne "$1" ]; then
		echo $2
		exit 1
	fi
}
#
################################################################
# Usage
if [ $# -eq 0 ]; then
	echo "Usage:	$(basename $0) [-h host] [-p port] data_file [data_file ...]"
fi
#
################################################################
#
HOST="localhost"
PORT="9200"


while getopts "h:p:" OPT; do
	[ $OPT == "h" ] && HOST=$OPTARG
	[ $OPT == "p" ] && PORT=$OPTARG
done

#
################################################################
# Load data.
#
for DATA_FILE in $*
do	
	if [ -r $DATA_FILE ]; then
		METHOD=_bulk
		echo "$DATA_FILE => $HOST:$PORT/$METHOD/"
		curl -s -o /dev/null -XPUT $HOST:$PORT/$METHOD \
			 --data-binary @$DATA_FILE
		check_error $? "Failed to load data from $DATA_FILE"
	fi
done
#
################################################################
#
exit 0
