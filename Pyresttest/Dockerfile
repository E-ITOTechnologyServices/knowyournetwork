#
# Docker file to build the Pyresttest environment
#
####################################################################################################
FROM debian:jessie
#
RUN apt-get update && \
        apt-get install -y python-pycurl \
                            python-pip 
#
RUN pip install pyresttest
#
COPY ./filesystem/tests /tests
#
WORKDIR /tests
#
ENTRYPOINT ["pyresttest", "http://middleware:8080"]
#
####################################################################################################
