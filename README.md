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

### Building a docker image locally
This will allow you to build local changes and test them using `chs-dev`.

```bash
mvn compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-2.amazonaws.com/local/officer-delta-processor:latest
```

## Building and Running Locally

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary (see below).
1. Ensure dependent Companies House services are running within the Companies House developer environment
1. Start the service in the CHS developer environment
1. Send a GET request using your REST client to /officer-delta-processor/healthCheck. The response should be 200 OK with status=UP.

## Makefile Changes
The jacoco exec file that SonarQube uses on GitHub is incomplete and, therefore, produces incorrect test coverage
reporting when code is pushed up to the repo. This is because the `analyse-pull-request` job runs when we push code to an open PR and this job runs `make test-unit`.
Therefore, the jacoco exec reporting only covers unit test coverage, not integration test coverage. 

To remedy this, in the
short-term, we have decided to change the `make test-unit` command in the Makefile to run `mvn clean verify -Dskip.unit.tests=false -Dskip.integration.tests=false` instead as this
will ensure unit AND integration tests are run and that coverage is added to the jacoco reporting and, therefore, produce accurate SonarQube reporting on GitHub.

For a more in-depth explanation, please see: https://companieshouse.atlassian.net/wiki/spaces/TEAM4/pages/4357128294/DSND-1990+Tech+Debt+Spike+-+Fix+SonarQube+within+Pom+of+Projects

