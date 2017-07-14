#
# Name: Dockerfile
#
# Date: 12-07-2016
#
# Docker file to deploy the middleware. 
#
# Use of this file to generate an image assumes that the middleware
# has already been built.
#
#######################################################################
#
FROM jetty
#
# Add packages
#
RUN apt-get update \
    && apt-get install -y socat \
                          supervisor \
                          vim
#
# Add the application
#
COPY KynKite/KynKiteWebApp/target/KynKiteWebApp /var/lib/jetty/webapps/KynKiteWebApp
#
ADD filesystem /
#
EXPOSE 8080
#
# Set the default command.
#
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
#
#######################################################################
