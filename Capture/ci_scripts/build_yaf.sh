#!/bin/bash -e

####################################################################################################
#
# Build yaf tool in docker container
#
####################################################################################################


####################################################################################################
# parameter check
#

DEST_DIR=$1
DEPLOY_DIR=$2
USER_ID=$3
NUM_CPU=$(nproc)


if [ -z ${DEST_DIR} ]; then
    echo DEST_DIR is not set - aborting.
    echo "Syntax: build_yaf.sh <DEST_DIR> <DEPLOY_DIR> <USER_ID>"
    exit 1
fi

if [ -z ${DEPLOY_DIR} ]; then
    echo DEPLOY_DIR is not set - aborting.
    echo "Syntax: build_yaf.sh <DEST_DIR> <DEPLOY_DIR> <USER_ID>"
    exit 2
fi

if [ -z ${USER_ID} ]; then
    echo USER_ID is not set - aborting.
    echo "Syntax: build_yaf.sh <DEST_DIR> <DEPLOY_DIR> <USER_ID>"
    exit 3
fi

BUILD_DIR=${DEST_DIR}/build
mkdir -p ${BUILD_DIR}

PREFIX=${DEPLOY_DIR}/usr/

####################################################################################################
# download yaf and tools
#

mkdir -p ${DEST_DIR}/download
cd ${DEST_DIR}/download

if [ ! -f libfixbuf-1.7.1.tar.gz ]; then
	curl -O https://tools.netsa.cert.org/releases/libfixbuf-1.7.1.tar.gz
	tar xzf libfixbuf-1.7.1.tar.gz -C ${BUILD_DIR}
	cd ${BUILD_DIR}/libfixbuf-*
	./configure --prefix=${PREFIX}
    make -j ${NUM_CPU}
    make install
fi

cd ${DEST_DIR}/download

if [ ! -f p0flib.tgz ]; then
	curl -O https://tools.netsa.cert.org/confluence/download/attachments/16547842/p0flib.tgz
	tar xzf p0flib.tgz -C ${BUILD_DIR}
	cd ${BUILD_DIR}/p0flib/libp0f
	./configure --prefix=${PREFIX}
    make -j ${NUM_CPU}
    make install
fi

cd ${DEST_DIR}/download

if [ ! -f yaf-2.8.4.tar.gz ]; then
	curl -O https://tools.netsa.cert.org/releases/yaf-2.8.4.tar.gz
	tar xzf yaf-2.8.4.tar.gz -C ${BUILD_DIR}
	cd ${BUILD_DIR}/yaf-*
	./configure --prefix=${PREFIX} --sysconfdir=/etc --enable-p0fprinter --enable-applabel --enable-plugins
fi

cd ${DEST_DIR}/download

if [ ! -f super_mediator-1.4.0.tar.gz ]; then
	curl -O https://tools.netsa.cert.org/releases/super_mediator-1.4.0.tar.gz
	tar xzf super_mediator-1.4.0.tar.gz -C ${BUILD_DIR}
	cd ${BUILD_DIR}/super_mediator-*
	./configure --prefix=${PREFIX} --sysconfdir=/etc 
fi

####################################################################################################


####################################################################################################
# build yaf and tools
#

echo "=== building yaf tools ==="

if [ ! -e ${PREFIX}/lib/libfixbuf.so ]; then
    cd ${BUILD_DIR}/libfixbuf-*
    make -j ${NUM_CPU}
    make install
fi


if [ ! -e ${PREFIX}/lib/libp0f.so ]; then
    cd ${BUILD_DIR}/p0flib/libp0f
    make -j ${NUM_CPU}
    make install
fi

if [ ! -e ${PREFIX}/bin/yaf ]; then
    cd ${BUILD_DIR}/yaf-2.8.4
    make -j ${NUM_CPU}
    make install
fi

if [ ! -e ${PREFIX}/bin/super_mediator ]; then
    cd ${BUILD_DIR}/super_mediator-1.4.0
    make -j ${NUM_CPU}
    make install
fi

### quick fix: adjust user rights of artefacts deployed to host system
echo setting user of ${DEPLOY_DIR} to uid ${USER_ID}
chown -R ${USER_ID} ${DEPLOY_DIR}
