#!/bin/sh
#
# Name: install_es_license.sh
#
# Author: Markus Jahnke
#
# Purpose: Load Elasticsearch license
#
# Usage: install_es_license.sh [-h host] [-p port] license_file
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
check_es() {
    RESPONSE=$(curl --max-time 2 --write-out %{http_code} --silent --output /dev/null $ESUSER:$ESPASSWORD@$HOST:$PORT)
}
#
################################################################
# Usage
if [ $# -eq 0 ]; then
	echo "Usage:	$(basename $0) [-h host] [-p port] license_file"
        exit 1
fi
#
################################################################
#
# retry timeout
RETRY=60
#
TIMEOUT=10
#
# Elasticsearch X-Pack default Passwords
ESUSER="elastic"
ESPASSWORD="changeme"
#
HOST="elasticsearch"
PORT="9200"
#
while getopts h:p: OPT; do
	[ $OPT == "h" ] && HOST=$OPTARG
	[ $OPT == "p" ] && PORT=$OPTARG
done
#
shift $((OPTIND-1))
#
LICENSE_FILE=$1
#
################################################################
# Load data.
#
if [ -r $LICENSE_FILE ]; then
        echo "$LICENSE_FILE => $HOST:$PORT"
#
# Wait for ES
#
        ENDTIME=$(($(date +%s) + $TIMEOUT))
        check_es
        while [ $? -ne 0 ] || [ "$RESPONSE" != "200" ]; do
            [ $ENDTIME -lt $(date +%s) ] && echo "ERROR: could not install es license" && exit 1
            echo "can't connect to $HOST:$PORT, will retry in $RETRY seconds"
            sleep $RETRY
            check_es
        done
#
# transfer license
#
        RESPONSE=$( curl -s -XPUT $ESUSER:$ESPASSWORD@$HOST:$PORT/_xpack/license?acknowledge=true -d @$LICENSE_FILE )
        check_error $? "Failed to import license from $LICENSE_FILE"
        # TODO: check RESPONSE
        echo "RESPONSE: $RESPONSE"
else
        echo "$LICENSE_FILE missing"
        exit 1
fi
#
################################################################
#
exit 0
