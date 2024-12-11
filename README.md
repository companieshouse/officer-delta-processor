# `officer-delta-processor`

## Summary

The ``officer-delta-processor`` handles the processing of officer deltas by:

* consuming them, in the forms of `ChsDelta` Kafka messages, from the `officer-delta` Kafka topic,
* deserialising them and transforming them into a structure suitable for a request to
  `company-appointments.api.ch.gov.uk`, and
* sending the request internally while performing any error handling.

## Error handling

The table below describes the topic a Kafka message is published to when an API error response is received, given the
number of attempts to process that message. The number of attempts is incremented when processed from the main or
retry topic. Any runtime exceptions thrown during the processing of a message are handled by publishing the message
immediately to the <br>`officer-delta-invalid` topic and are not retried.

| API Response | Attempt          | Topic published to    |
|--------------|------------------|-----------------------|
| 2xx          | any              | _does not republish_  |
| 400 or 409   | any              | officer-delta-invalid |
| 4xx or 5xx   | < max_attempts   | officer-delta-retry   |
| 4xx or 5xx   | \>= max_attempts | officer-delta-error   |

## System requirements

* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads)
* [Maven](https://maven.apache.org/download.cgi)
* [Apache Kafka](https://kafka.apache.org/)

## Building and Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the
   README.
2. Enable the following services using the command `./bin/chs-dev services enable <service>`.
    * `chs-delta-api`
    * `officer-delta-processor`
    * `company-appointments.api.ch.gov.uk`
    * `chs-kafka-api`
3. For a full end to end flow including sub deltas, also enable the `streaming` module and the following services:
    * `officers-search-consumer`
    * `search.api.ch.gov.uk`
    * `company-links-consumer`
    * `company-profile-api`
    * `company-metrics-consumer`
    * `company-metrics-api`
4. Boot up the services' containers on docker using `chs-dev up`.
5. Messages can be produced to the officer-delta topic using the instructions given
   in [CHS Delta API](https://github.com/companieshouse/chs-delta-api).

## Environment variables

| Variable                              | Description                                                                                                | Example                   |
|---------------------------------------|------------------------------------------------------------------------------------------------------------|---------------------------|
| KAFKA_BROKER_ADDR                     | The URL to the kafka broker                                                                                | kafka:9092                |
| KAFKA_POLLING_DURATION_MS             | The time between requests to the broker to check for new messages                                          | 5000                      |
| KAFKA_GROUP_NAME                      | The name of the consumer group on Kafka                                                                    | officer-delta-processor   |
| IS_ERROR_QUEUE_CONSUMER               | Whether or not to enable the error consumer (replaced by kafka-error-consumer)                             | false                     |
| MAXIMUM_RETRY_ATTEMPTS                | The number of times a message will be retried before being moved to the error topic                        | 5                         |
| KAFKA_TOPICS_LIST                     | The topic ID for officer delta kafka topic                                                                 | officer-delta             |
| OFFICER_DELTA_PROCESSOR_BACKOFF_DELAY | The incremental time delay between message retries                                                         | 100                       |
| OFFICER_DELTA_LISTENER_CONCURRENCY    | The number of listeners run in parallel for the consumer                                                   | 1                         |
| CHS_INTERNAL_API_KEY                  | The client ID of an API key, with internal app privileges, to call company-appointments.api.ch.gov.uk with | abc123def456ghi789        |
| API_URL                               | The host through which requests to the company-appointments.api.ch.gov.uk are sent                         | http://api.chs.local:4001 |

## Building the docker image

```bash
mvn compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-2.amazonaws.com/local/officer-delta-processor:latest
```

## To make local changes

Development mode is available for this service
in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

```bash
./bin/chs-dev development enable officer-delta-processor
```

This will clone the `officer-delta-processor` into the repositories folder. Any changes to the code, or resources
will automatically trigger a rebuild and relaunch.

## Makefile Changes

The jacoco exec file that SonarQube uses on GitHub is incomplete and, therefore, produces incorrect test coverage
reporting when code is pushed up to the repo. This is because the `analyse-pull-request` job runs when we push code to
an open PR and this job runs `make test-unit`.
Therefore, the jacoco exec reporting only covers unit test coverage, not integration test coverage.

To remedy this, in the
short-term, we have decided to change the `make test-unit` command in the Makefile to run
`mvn clean verify -Dskip.unit.tests=false -Dskip.integration.tests=false` instead as this
will ensure unit AND integration tests are run and that coverage is added to the jacoco reporting and, therefore,
produce accurate SonarQube reporting on GitHub.

For a more in-depth explanation, please
see: https://companieshouse.atlassian.net/wiki/spaces/TEAM4/pages/4357128294/DSND-1990+Tech+Debt+Spike+-+Fix+SonarQube+within+Pom+of+Projects

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        | data-sync                                      | ECS cluster (stack) the service belongs to
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/officer-delta-processor) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/officer-delta-processor)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
