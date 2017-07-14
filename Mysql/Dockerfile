## MySQL 5.7
#
# Pull the mysql:5.7 image
#
FROM mysql:5.7
#
# Install necessary tools
#
RUN apt-get update && \
    apt-get install -y cron \
                       supervisor \
			           vim
#
# Add the application
#
ADD filesystem /
#
# Activate the crontab
#
RUN crontab /etc/crontab
#
# Create folder for backup
#
RUN mkdir /backup
#
# Set the default command.
#
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
#
####################################################################################################