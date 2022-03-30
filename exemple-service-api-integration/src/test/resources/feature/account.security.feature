Feature: api account security

  Background: 
    Given delete username 'jean.dupond@gmail.com'

  Scenario: create account fails because application not exists
    When create account for application 'default' and version 'v1'
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    Then account is denied

  Scenario: get account fails because application not exists
    Given create account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    When get account for application 'default' and version 'v1'
    Then account is denied
