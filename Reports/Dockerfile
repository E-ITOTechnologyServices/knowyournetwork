#
# Docker file to build the Backend environment.
#
####################################################################################################
#
FROM debian:jessie-slim
#
RUN apt-get update
RUN apt-get install -y  texlive-xetex\
                        php5-cli\
                        php5-curl\
                        curl\
                        && rm -rf /usr/share/doc/*

RUN cd /tmp/ \
        && curl -O http://de.archive.ubuntu.com/ubuntu/pool/main/u/ubuntu-font-family-sources/ttf-ubuntu-font-family_0.83-0ubuntu2_all.deb \
        && dpkg -i /tmp/ttf-ubuntu-font-family_0.83-0ubuntu2_all.deb \
        && rm -rf /tmp/* /usr/share/doc/*


WORKDIR /var/kyn/scripts
RUN echo '{"require":{"elasticsearch/elasticsearch": "~2.0"}}' > /var/kyn/scripts/composer.json

RUN curl -s http://getcomposer.org/installer | php \
        && php /var/kyn/scripts/composer.phar install -d /var/kyn/scripts/ --no-dev \
        && rm /var/kyn/scripts/composer.*

COPY ./filesystem/Reports /Reports

WORKDIR /Reports

ENTRYPOINT ["/Reports/run.sh"]

####################################################################################################

