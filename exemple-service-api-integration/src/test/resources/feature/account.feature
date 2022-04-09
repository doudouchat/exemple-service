Feature: api account

  Background: 
    Given delete username 'jean.dupond@gmail.com'

  Scenario: create account
    When create account
      """
      {
          "optin_mobile": true,
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "addresses": {
             "holiday2": null,
             "holiday1": null,
             "job": {
                 "city": "Paris",
                 "street": "rue de la paix"
             },
             "home": {
                 "city": "Lyon",
                 "street": "rue de la poste"
             }
          },
          "civility": "Mr",
          "mobile": "0610203040",
          "cgus": [
             {
               "code": "code_1",
               "version": "v0"
             }
          ],
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupond"
      }
      """
    Then account 'jean.dupond@gmail.com' exists
    And get id account 'jean.dupond@gmail.com'
    And account is
      """
      {
          "addresses": {
              "home": {
                  "street": "rue de la poste",
                  "city": "Lyon"
              },
              "job": {
                  "street": "rue de la paix",
                  "city": "Paris"
              }
          },
          "birthday": "1967-06-15",
          "cgus": [
              {
                  "code": "code_1",
                  "version": "v0"
              }
          ],
          "civility": "Mr",
          "email": "jean.dupond@gmail.com",
          "firstname": "Jean",
          "lastname": "Dupond",
          "mobile": "0610203040",
          "optin_mobile": true
      }
      """
    And account property 'creation_date' exists

  Scenario: create account fails because lastname is not an integer
    When create any account with 'lastname' and value 10
    Then account error only contains
      """
      {
          "path": "/lastname",
          "code": "type"
      }
      """

  Scenario: create account fails because birthday is incorrect
    When create any account with 'birthday' and value '2019-02-30'
    Then account error only contains
      """
      {
          "path": "/birthday",
          "code": "format"
      }
      """

  Scenario: create account fails because optin_mobile is not an integer
    When create any account with 'optin_mobile' and value 10
    Then account error only contains
      """
      {
          "path": "/optin_mobile",
          "code": "type"
      }
      """

  Scenario: create account fails because cgus is not an integer
    When create any account with 'cgus' and value 10
    Then account error only contains
      """
      {
          "path": "/cgus",
          "code": "type"
      }
      """

  Scenario: create account fails because email is empty
    When create any account with 'email' and value ''
    Then account error only contains
      """
      {
          "path": "/email",
          "code": "format"
      }
      """

  Scenario: create account fails because a property is unknown
    When create any account with 'unknown' and value 'nc'
    Then account error only contains
      """
      {
          "path": "/unknown",
          "code": "additionalProperties"
      }
      """

  Scenario: create account fails because an address is incomplete
    When create any account with 'addresses'
      """
      {
         "job": {
            "city": "Paris"
         }
      }
      """
    Then account error only contains
      """
      {
          "path": "/addresses/job/street",
          "code": "required"
      }
      """

  Scenario: create account fails because two many addresses
    When create any account with 'addresses'
      """
      {
         "holidays_1": {
            "city": "Paris",
            "street": "1 rue de la paix"
         },
         "holidays_2": {
            "city": "Paris",
            "street": "2 rue de la paix"
         },
         "holidays_3": {
            "city": "Paris",
            "street": "3 rue de la paix"
         }
      }
      """
    Then account error only contains
      """
      {
          "path": "/addresses",
          "code": "maxProperties"
      }
      """

  Scenario: create account fails because username already exists
    Given account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    When create any account with 'email' and value 'jean.dupond@gmail.com'
    Then account error only contains
      """
      {
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }
      """

  Scenario: get account fails because account not exists
    When get account by id d6233a2e-64f9-4e92-b894-01244515a18e
    Then account is unknown
