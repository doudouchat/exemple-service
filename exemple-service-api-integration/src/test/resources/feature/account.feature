Feature: api account

  Scenario: create account
    When create account for application 'test' and version 'v1'
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
    Then account status is 201
    And account exists
    And account 'creation_date' exists
    And account 'id' exists
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

  Scenario: create account fails because lastname is not an integer
    When create account with 'lastname' and value 10
    Then account status is 400
    And account error is
      """
      [{
          "path": "/lastname",
          "code": "type"
      }]
      """

  Scenario: create account fails because birthday is incorrect
    When create account with 'birthday' and value '2019-02-30'
    Then account status is 400
    And account error is
      """
      [{
          "path": "/birthday",
          "code": "format"
      }]
      """

  Scenario: create account fails because optin_mobile is not an integer
    When create account with 'optin_mobile' and value 10
    Then account status is 400
    And account error is
      """
      [{
          "path": "/optin_mobile",
          "code": "type"
      }]
      """

  Scenario: create account fails because cgus is not an integer
    When create account with 'cgus' and value 10
    Then account status is 400
    And account error is
      """
      [{
          "path": "/cgus",
          "code": "type"
      }]
      """

  Scenario: create account fails because email is empty
    When create account with 'email' and value ''
    Then account status is 400
    And account error is
      """
      [{
          "path": "/email",
          "code": "format"
      }]
      """

  Scenario: create account fails because a property is unknown
    When create account with 'unknown' and value 'nc'
    Then account status is 400
    And account error is
      """
      [{
          "path": "/unknown",
          "code": "additionalProperties"
      }]
      """

  Scenario: create account fails because an address is incomplete
    When create account with 'addresses'
      """
      {
         "job": {
            "city": "Paris"
         }
      }
      """
    Then account status is 400
    And account error is
      """
      [{
          "path": "/addresses/job/street",
          "code": "required"
      }]
      """

  Scenario: create account fails because two many addresses
    When create account with 'addresses'
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
    Then account status is 400
    And account error is
      """
      [{
          "path": "/addresses",
          "code": "maxProperties"
      }]
      """

  Scenario: create account fails because application not exists
    When create account for application 'default' and version 'v1'
      """
      {
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupond"
      }
      """
    Then account status is 403

  Scenario: get account fails because application not exists
    Given create account
    When get account for application 'default' and version 'v1'
    Then account status is 403

  Scenario: get account fails because account not exists
    When get account d6233a2e-64f9-4e92-b894-01244515a18e for application 'test' and version 'v1'
    Then account status is 404
