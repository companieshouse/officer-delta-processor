Feature: Officer Delta

Scenario: Can transform and send a natural officer
  Given the application is running
  When the consumer receives a natural officer delta
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate officer
  Given the application is running
  When the consumer receives a corporate officer delta
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a pre-1992-corporate officer
  Given the application is running
  When the consumer receives a pre_1992_corporate officer delta
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a pre-1992-natural officer
  Given the application is running
  When the consumer receives a pre_1992_natural officer delta
  Then a PUT request is sent to the appointments api with the transformed data