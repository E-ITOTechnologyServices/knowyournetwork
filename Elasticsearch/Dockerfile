#
# Docker file to build the Elasticsearch environment based on Image provided by Elastic
#
####################################################################################################
#
FROM docker.elastic.co/elasticsearch/elasticsearch:5.3.0
# 
RUN sed -i 's/^-Xmx2g/#-Xmx2g/g' /usr/share/elasticsearch/config/jvm.options; \
    sed -i 's/^-Xms2g/#-Xms2g/g' /usr/share/elasticsearch/config/jvm.options
#
#
#
####################################################################################################
#
# Set the default command.
#
# ENTRYPOINT inherited from original Image
#
####################################################################################################
