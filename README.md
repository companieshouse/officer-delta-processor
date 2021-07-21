# officer-delta-processor

The Officer Delta Processor is responsible for reading from the officer-delta kafka topic.

Requirements
------------

To build officer-delta-processor, you will need:
* [Git](https://git-scm.com/downloads)
* [Java 11](https://www.oracle.com/uk/java/technologies/javase-jdk11-downloads.html)
* [Maven](https://maven.apache.org/download.cgi)
* [Apache Kafka](https://kafka.apache.org/)
* Internal Companies House core services

You will also need a REST client (e.g. Postman or cURL) if you want to interact with any officer-delta-processor service endpoints.

## Building and Running Locally

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary (see below).
1. Ensure dependent Companies House services are running within the Companies House developer environment
1. Start the service in the CHS developer environment
1. Send a GET request using your REST client to /officer-delta-processor/healthCheck. The response should be 200 OK with status=UP.

