Feature: Retries and Errors

  Scenario: Process invalid avro message
    Given the application is running
    When an invalid avro message is sent
    Then the message should be moved to topic officer-delta-invalid

  Scenario: Process message with invalid data
    Given the application is running
    When a message with invalid data is sent
    Then the message should be moved to topic officer-delta-invalid

  Scenario: Process message when the data api returns 400
    Given the application is running
    When the consumer receives a message but the data api returns a 400
    Then the message should be moved to topic officer-delta-invalid

  Scenario: Process message when the data api returns 503
    Given the application is running
    When the consumer receives a message but the data api returns a 503
    Then the message should retry 4 times and then error