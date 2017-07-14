FROM tmaier/docker-compose

### Administration container image
#
# can be used to
# * start kyn system
# * stop kyn system
#
# needed to perform
# * elastic search license init
# * user database model init
# * check/set vm memory setting for elastic search
#
RUN apk update \
    && apk add curl \
    && apk add mysql-client

# Add all init scripts
ADD filesystem  /

ENTRYPOINT [ "/usr/bin/admin.sh" ]
