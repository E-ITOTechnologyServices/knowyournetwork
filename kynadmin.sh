#!/bin/sh

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

### grep all environment variables with prefix KYN_*
#
get_environment()
{
    env | grep KYN_ | while read line
    do
        [ -n "${line}" ] && echo "--env $line "
    done
}

### kyn docker network autodetection
#
get_network()
{
    KYN_NETWORK=$(docker network list --filter name=kyn* --format {{.Name}})

    if [ ! -z "$KYN_NETWORK" ]; then
        NUM_NETWORKS=$(echo $KYN_NETWORK | wc -w)
        if [ $NUM_NETWORKS -gt 1 ]; then
            echo "WARNING: more than 1 kyn* network found! [" $KYN_NETWORK "]" 1>&2
            KYN_NETWORK=$(echo $KYN_NETWORK|cut -f1 -d" ")
            echo "using $KYN_NETWORK" 1>&2
        fi
        echo "--network=$KYN_NETWORK"
    fi
}

#######################################################################
#
### kyn docker parameters
#
VOLUME="--volume=/var/run/docker.sock:/var/run/docker.sock"
#
IMAGE="kyn/admin"
#
ENV=$(get_environment)
#
# Special case for "down" command
if [ "$1" != "down" ]; then
    NETWORK=$(get_network)
fi

# Special case for "eslicense" command
if [ "$1" = "eslicense" ]; then
    if [ -f "$2" ]; then
        ES_FILE_PATH=`realpath $2`
        VOLUME="$VOLUME --volume=$ES_FILE_PATH:/tmp/es_license.json"
    else
        echo "Installing an ES license requires a license file as second argument!"
        exit 1
    fi
fi

#######################################################################
#
### run command in docker admin container
docker run --rm ${ENV} ${VOLUME} ${NETWORK} ${IMAGE} $@
