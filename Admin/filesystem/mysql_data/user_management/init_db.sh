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

# Initialize the database
echo -n "Initializing user management database..."
mysql -h $HOST -P $PORT -w --password=$PASSWORD < /mysql_data/user_management/sql_scripts/import_database.sql
echo "done."
