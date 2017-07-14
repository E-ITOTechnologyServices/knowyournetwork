#
# Docker file to build the Spark environment
#
####################################################################################################
#
#
FROM ubuntu:xenial
#
RUN apt-get update && \
    apt-get install -y openjdk-8-jre \
                       cron \
                       curl \
                       supervisor \
                       ruby \
                       tree \
                       vim 
#
RUN apt-get install -y python-elasticsearch python-numpy python-scipy python-sklearn
#
RUN gem install configparser elasticsearch 
#
RUN curl http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz | tar -xzC /usr/local; \
    ln -s /usr/local/spark-2.1.0-bin-hadoop2.7/ /usr/local/spark
#
#
# Add user "hadoopuser"
RUN useradd -s /bin/bash -d /home/hadoopuser hadoopuser \
	&& mkdir -p /home/hadoopuser \
	&& echo "export SPARK_HOME=/usr/local/spark" >> /home/hadoopuser/.profile \
	&& chown -R hadoopuser:hadoopuser /home/hadoopuser
#
#
# Add the application
#
ADD filesystem /
#
RUN chown -R hadoopuser:hadoopuser /var/kyn/scripts \
	&& crontab -u hadoopuser /etc/crontab
#
####################################################################################################
#
# Set the default command.
#
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
#
####################################################################################################
