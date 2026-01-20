Feature: Officer Delta

Scenario: Can transform and send a natural officer
  Given the application is running
  When the consumer receives a natural officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a natural officer which is missing address fields
  Given the application is running
  When the consumer receives a natural_missing_address_fields officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate officer
  Given the application is running
  When the consumer receives a corporate officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate officer with identification type other corporate body or firm
  Given the application is running
  When the consumer receives a corporate_other_corporate_body officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a corporate officer with identification type registered overseas entity corporate managing officer
  Given the application is running
  When the consumer receives a corporate_roe_managing officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
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

Scenario: Can transform and send a corporate-managing officer which is missing address fields
  Given the application is running
  When the consumer receives a corporate_managing_missing_address_fields officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a managing officer
  Given the application is running
  When the consumer receives a managing officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a limited-partnership-general-partner-natural-person officer
  Given the application is running
  When the consumer receives a limited_partnership_general_partner_natural_person officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a limited-partnership-general-partner-legal-entity officer
  Given the application is running
  When the consumer receives a limited_partnership_general_partner_legal_entity officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a limited-partnership-limited-partner-natural-person officer
  Given the application is running
  When the consumer receives a limited_partnership_limited_partner_natural_person officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a limited-partnership-limited-partner-legal-entity officer
  Given the application is running
  When the consumer receives a limited_partnership_limited_partner_legal_entity officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data

Scenario: Can transform and send a limited-partnership-limited-partner-invalid-capital-contribution-subtype officer
  Given the application is running
  When the consumer receives a limited_partnership_limited_partner_invalid_capital_contribution_subtype officer delta with id EcEKO1YhIKexb0cSDZsn_OHsFw4
  Then a PUT request is sent to the appointments api with the transformed data
