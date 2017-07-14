#!/bin/sh
#
# Name: import_prod.sh
#
# Author: Tobias Nieberg
#
# Purpose: Load some functional data into an ElasticSearch
# instance.
#
# Usage: import_prod.sh [-h host] [-p port]
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
#
HOST="localhost"
PORT="9200"


while getopts "h:p:" OPT; do
	[ $OPT == "h" ] && HOST=$OPTARG
	[ $OPT == "p" ] && PORT=$OPTARG
done

#
################################################################
#
# Load templates.
#
./import_template.sh -h $HOST -p $PORT template/*
check_error $? "Import of templates failed."

#
# Load indices.
#
./import_data.sh -h $HOST -p $PORT index/*
check_error $? "Import of index data failed."
#
################################################################
exit 0
