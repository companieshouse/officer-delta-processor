# officer-delta-processor

The Officer Delta Processor is responsible for reading from the officer-delta kafka topic.

TODO: add more detail.

Requirements
------------

To build efs-submission-api, you will need:
* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/download.cgi)
* [Apache Kafka](https://kafka.apache.org/)
* Internal Companies House core services

You will also need a REST client (e.g. Postman or cURL) if you want to interact with any officer-delta-processor service endpoints.

Certain endpoints (e.g. POST /efs-submission-api/events/submit-files-to-fes) will not work correctly unless the relevant environment variables are configured. 

## Building and Running Locally

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary (see below).
1. Ensure dependent Companies House services are running within the Companies House developer environment
1. Start the service in the CHS developer environment
1. Send a GET request using your REST client to /officer-delta-processor/healthcheck. The response should be 200 OK with status=UP.

Configuration
-------------
System properties for officer-delta-processor are defined in `application.properties`. These are normally configured per environment.

Variable| Description|Example|Mandatory (always, email, FES)|
--------------------|--------------|------|--------|


## Building the docker image 
***--Add correct image here--***

     mvn -s settings.xml compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/efs-submission-api

## Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.

1. Enable the `insertCorrectDeltaModuleName` module

1. Run `tilt up` and wait for all services to start

### To make local changes

Development mode is available for this service in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable officer-delta-processor

This will clone the officer-delta-processor into the repositories' folder inside of docker-chs-dev folder. Any changes to the code, or resources will automatically trigger a rebuild and relaunch.