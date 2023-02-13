Feature: Officer Delta

Scenario: Can transform and send a natural officer
  Given the application is running
  When the consumer receives a natural officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate officer
  Given the application is running
  When the consumer receives a corporate officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a pre-1992-corporate officer
  Given the application is running
  When the consumer receives a pre_1992_corporate officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a pre-1992-natural officer
  Given the application is running
  When the consumer receives a pre_1992_natural officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate-non-corp-kind officer
  Given the application is running
  When the consumer receives a corporate_non_corp_kind officer delta with id ODxi2KP6LfSvwes9xY0O86YliyM
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate-managing officer
  Given the application is running
  When the consumer receives a corporate_managing officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a managing officer
  Given the application is running
  When the consumer receives a managing officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data