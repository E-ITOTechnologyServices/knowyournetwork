#!/bin/sh

HOST="localhost"
PORT="3306"
PASSWORD=""
#
while getopts h:p:b: OPT; do
	[ $OPT == "h" ] && HOST=$OPTARG
	[ $OPT == "p" ] && PORT=$OPTARG
    [ $OPT == "b" ] && PASSWORD=$OPTARG
done
#
shift $((OPTIND-1))

# Import AQL demo data
echo -n "Importing AQL demo data..."
mysql -h $HOST -P $PORT -w --password=$PASSWORD < /mysql_data/aql/sql_scripts/import_demodata.sql
echo "done."
