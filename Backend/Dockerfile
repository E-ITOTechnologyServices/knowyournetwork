#
# Docker file to build the Backend environment.
#
####################################################################################################
#
FROM debian:jessie
#
#
RUN apt-get update && \
    apt-get install -y cron \
                       php5-cli \
                       php5-common \
                       ruby \
                       supervisor \
                       tree \
                       vim \
			ruby-mysql2
#
# install Ruby libraries
#
RUN gem install elasticsearch hashie ruby-graphviz 
#
#
####################################################################################################
#
# Add the application
#
ADD filesystem /
#
RUN crontab /etc/crontab
#
# Set the default command.
#
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
#
####################################################################################################
