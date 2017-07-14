#
# Docker file to build the front end environment.
#
#######################################################################
#
FROM node:7.7-alpine
#
# Add the application
#

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Copy Node.js config and install dependencies
COPY ./package.json /usr/src/app
RUN npm install

# Copy all source files and run app
COPY ./src /usr/src/app/src
CMD [ "npm", "start" ]

#
EXPOSE 80
#
#######################################################################
