#!/bin/sh

HOST="localhost"
PORT="3306"
#
while getopts h:p: OPT; do
	[ $OPT == "h" ] && HOST=$OPTARG
	[ $OPT == "p" ] && PORT=$OPTARG
done
#
shift $((OPTIND-1))

# Set the MySQL databases user access
echo -n "Setting the MySQL databases user access..."
mysql -h $HOST -P $PORT -w < /mysql_data/sql_scripts/import_access.sql
echo "done."
