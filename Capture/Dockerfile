#
# Docker file to build the Capture environment.
#
####################################################################################################
#
FROM debian:jessie
#
RUN apt-get update && \
    apt-get install -y cron \
                       curl \
                       libglib2.0-0 \
                       libjansson4 \
                       libpcap0.8 \
                       ruby \
                       supervisor \
                       tree \
                       vim && \
    gem install elasticsearch
#
# Installing Docker
#
RUN apt-get install -y apt-transport-https \
                       ca-certificates \
                       gnupg2 \
                       software-properties-common
#
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
#
RUN add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
#
RUN apt-get update && \
    apt-get install -y docker-ce
#
#
####################################################################################################
#
# Add the application
#
ADD filesystem /
#
# Activate the crontab
#
RUN crontab /etc/crontab
#
# Set the default command.
#
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
#
####################################################################################################
