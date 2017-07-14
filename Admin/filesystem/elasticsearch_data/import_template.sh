#!/bin/sh
#
# Name: import.sh
#
# Author: Tobias Nieberg
#
# Purpose: Load some functional data into an ElasticSearch
# instance.
#
# Usage: import_template.sh [-h host] [-p port] template_file [template_file ...]
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
	echo "Usage:	$(basename $0) [-h host] [-p port] template_file [template_file ...]"
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
# Load template.
#
for TEMPLATE_FILE in $*
do	
	if [ -r $TEMPLATE_FILE ]; then
		TEMPLATE=$(basename $TEMPLATE_FILE .json)
		METHOD=_template
		echo "$TEMPLATE_FILE => $HOST:$PORT/$METHOD/$TEMPLATE "
		curl -s -o /dev/null -XPUT $HOST:$PORT/$METHOD/$TEMPLATE \
			 --data-binary @$TEMPLATE_FILE
		check_error $? "Failed to load template for $TEMPLATE"
	fi
done
#
################################################################
#
exit 0
