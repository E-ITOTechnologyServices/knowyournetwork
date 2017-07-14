#!/bin/sh -e

####################################################################################################
#
# Build nfelastic in docker container
#
####################################################################################################


####################################################################################################
# parameter check
#

SRC_DIR=$1
BUILD_DIR=$2
DEPLOY_DIR=$3
USER_ID=$4


if [ -z ${SRC_DIR} ]; then
    echo SRC_DIR is not set - aborting.
    exit 1
fi

if [ -z ${BUILD_DIR} ]; then
    echo BUILD_DIR is not set - aborting.
    exit 2
fi

if [ -z ${DEPLOY_DIR} ]; then
    echo DEPLOY_DIR is not set - aborting.
    exit 3
fi

if [ -z ${USER_ID} ]; then
    echo USER_ID is not set - aborting.
    exit 4
fi

PREFIX=${DEPLOY_DIR}/usr/

####################################################################################################
# preparing build

NUM_CPU=$(nproc)

####################################################################################################
# build nfelastic
#

echo "=== building nfelastic ==="

mkdir -p ${BUILD_DIR}
cd ${BUILD_DIR}

cmake ${SRC_DIR}/nfelastic -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=${DEPLOY_DIR}/usr
make -j ${NUM_PROC}
make install

chown -R ${USER_ID} ${DEPLOY_DIR}
