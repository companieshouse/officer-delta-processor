Feature: Delete

  Scenario: send DELETE request to appointments Api
    Given the application is running
    When the consumer receives a delete payload
    Then a DELETE request is sent to the appointments api with the encoded Id and company number