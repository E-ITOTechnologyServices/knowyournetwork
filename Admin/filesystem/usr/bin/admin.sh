#!/bin/sh
#
# Name: init.sh
#
# Author: Tobias Nieberg
#
# Purpose: Initialise kyn system
#
# Usage: init.sh <actions>
#
################################################################
ELASTICHOST="elasticsearch"

MYSQLHOST="mysql"

DOCKER_SOCKET="/var/run/docker.sock"

# init elastic license in service container "elastisearch"
install_eslicense()
{
    echo
    /usr/bin/install_es_license.sh -h $ELASTICHOST /tmp/es_license.json
}


# init kyn user database in service container "mysql"
init_userdb()
{
    echo
    /mysql_data/user_management/init_db.sh -h $MYSQLHOST -b ${1-""}
}

# init kyn aql database in service container "mysql"
init_aqldb()
{
    echo
    /mysql_data/aql/init_db.sh -h $MYSQLHOST -b ${1-""}
}


# import mysql user management demo data
import_userdb_demodata()
{
    echo
    /mysql_data/user_management/import_demodata.sh -h $MYSQLHOST -b ${1-""}
}


# import mysql aql demo data
import_aqldb_demodata()
{
    echo
    /mysql_data/aql/import_demodata.sh -h $MYSQLHOST -b ${1-""}
}


# set mysql access rights
set_mysql_access()
{
    echo
    /mysql_data/set_access.sh -h $MYSQLHOST
}


import_es_templates()
{
    echo
    cd /elasticsearch_data/
    ./import_template.sh -h $ELASTICHOST template/*
    cd /
}


import_es_indices()
{
    echo
    cd /elasticsearch_data/
    ./import_data.sh -h $ELASTICHOST index/*
    cd /
}


import_es_demodata()
{
    echo
    cd /elasticsearch_data/
    ./import_data.sh -h $ELASTICHOST demo/*
    cd /
}


check_docker_socket()
{
    [ ! -S "$DOCKER_SOCKET" ] && echo "ERROR: Docker socket at ${DOCKER_SOCKET} not mounted." && exit 1
}


dump_docker_status()
{
    echo
    echo "=== kyn docker containers ==="
    docker ps -a --filter name=kyn*
    echo
    echo "=== kyn docker network ======"
    docker network list --filter name=kyn*
    echo
    echo "============================="
    echo
}


start_kyn()
{
    check_docker_socket
    cd /opt/kyn
    docker-compose up -d
    cd /
    dump_docker_status
}


stop_kyn()
{
    check_docker_socket
    cd /opt/kyn
    docker-compose down
    cd /
    dump_docker_status
}


# print command line option onto console
print_help()
{
    echo
    echo "Syntax:   admin.sh [-p some_password] <command> [<command> ...]"
    echo
    echo "Commands:"
    echo "    eslicense"
    echo "        install elastic search license"
    echo "    inituserdb"
    echo "        reset and initialise kyn user database"
    echo "    initaqldb"
    echo "        reset and initialise kyn aql database"
    echo "    importestemplates"
    echo "        import elasticsearch templates"
    echo "    importesindices"
    echo "        import elasticsearch indices"
    echo "    importesdemodata"
    echo "        import elasticsearch demo data"
    echo "    importuserdbdemodata"
    echo "        import mysql user management demo data"
    echo "    importaqldbdemodata"
    echo "        import mysql aql demo data"
    echo "    setmysqlaccess"
    echo "        set mysql access rights"
    echo "    up"
    echo "        start all kyn containers"
    echo "    down"
    echo "        stop and remove all kyn containers"
    echo "    init"
    echo "        perform all init actions at once"
    echo "    status"
    echo "        overview about running containers and networks"
    echo
}



##################################################
# main script
#

if [ $# -lt 1 ]; then
    print_help
    exit 1
fi

PASSWORD=""
while getopts p: OPT; do
	[ $OPT == "p" ] && PASSWORD=$OPTARG
done
#
shift $((OPTIND-1))


while [ $# -ge 1 ]
do
    case "$1" in

        eslicense)
            install_eslicense
            shift
            ;;

        inituserdb)
            init_userdb $PASSWORD
            ;;

        initaqldb)
            init_aqldb $PASSWORD
            ;;

        importestemplates)
            import_es_templates
            ;;

        importesindices)
            import_es_indices
            ;;

        importesdemodata)
            import_es_demodata
            ;;

        importuserdbdemodata)
            import_userdb_demodata $PASSWORD
            ;;

        importaqldbdemodata)
            import_aqldb_demodata $PASSWORD
            ;;

        setmysqlaccess)
            set_mysql_access
            ;;

        up)
            start_kyn
            ;;

        down)
            stop_kyn
            ;;

        init)
            install_eslicense
            init_userdb
            init_aqldb
            import_es_templates
            import_es_indices
            ;;

        status)
            dump_docker_status
            ;;

        *)
            echo "ERROR: command $1 is unknown"
            exit 1
            ;;
    esac

    shift
done
