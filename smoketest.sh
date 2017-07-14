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

#
# Executing live Middleware Rest API smoketests
#
####################################################################################################
#
# print command line option onto console

print_help()
{
    echo
    echo "Syntax:   smoketest.sh <test-configuration> [options]"
    echo
    echo "Test configurations:"
    echo "    all.yaml"
    echo "        run all tests below"
    echo "    reports-test.yaml"
    echo "        testing the reports module"
    echo "    aql-test.yaml"
    echo "        testing the AQL rules module"
    echo "    lara-test.yaml"
    echo "        testing the Lara rules module"
    echo "    newsticker-test.yaml"
    echo "        testing the Newsticker module"
    echo "    ntv-test.yaml"
    echo "        testing the Network traffic vizualization module"
    echo "    syslog-test.yaml"
    echo "        testing the Syslog module"
    echo "    kite-test.yaml"
    echo "        testing the Kite module - Host reports, Host details reports, CMDB reports"
    echo
    echo "For [options] see: https://github.com/svanoort/pyresttest"
    echo "    --log info"
    echo
}
#
if [ $# -lt 1 ]; then
    print_help
    exit 1
fi
#
IMAGE="kyn/pyresttest"
#
### kyn docker network autodetection
#
KYN_NETWORK=$(docker network list --filter name=kyn* --format {{.Name}})
#
if [ ! -z "$KYN_NETWORK" ]; then
    NETWORK="--network=$KYN_NETWORK"
fi
#
#
### run PyRestTest container and run sent tests
#
docker run --rm ${NETWORK} $IMAGE $@
