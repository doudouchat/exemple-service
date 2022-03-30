Feature: api put account

  Background: 
    Given delete username 'jean.dupond@gmail.com'
    And create account
      """
      {
          "optin_mobile": true,
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "addresses": {
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
          "lastname": "Dupont"
      }
      """

  Scenario: put account
    When put account
      """
      {
         "addresses": {
             "home": {
                 "street": "rue de la poste",
                 "city": "Paris"
             },
             "job": {
                 "street": "5th avenue",
                 "city": "New-York"
             }
         },
         "birthday": "1967-06-15",
         "cgus": [
             {
                 "code": "code_1",
                 "version": "v0"
             },
             {
                 "code": "code_1",
                 "version": "v1"
             }
         ],
         "email": "jean.dupond@gmail.com",
         "firstname": "Jean",
         "lastname": "Dupond",
         "mobile": "0610203040",
         "optin_mobile": true
      }
      """
    Then account is
      """
      {
          "addresses": {
              "home": {
                  "street": "rue de la poste",
                  "city": "Paris"
              },
              "job": {
                  "street": "5th avenue",
                  "city": "New-York"
              }
          },
          "birthday": "1967-06-15",
          "cgus": [
              {
                  "code": "code_1",
                  "version": "v0"
              },
              {
                  "code": "code_1",
                  "version": "v1"
              }
          ],
          "email": "jean.dupond@gmail.com",
          "firstname": "Jean",
          "lastname": "Dupond",
          "mobile": "0610203040",
          "optin_mobile": true
      }
      """
    And account property 'creation_date' exists
    And account property 'update_date' exists
    
  Scenario: change email
    Given delete username 'jean.dupont@gmail.com'
    When put account
      """
      {
          "optin_mobile": true,
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "addresses": {
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
          "email": "jean.dupont@gmail.com",
          "lastname": "Dupont"
      }
      """
    Then account is
      """
      {
          "optin_mobile": true,
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "addresses": {
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
          "email": "jean.dupont@gmail.com",
          "lastname": "Dupont"
      }
      """
    And account 'jean.dupond@gmail.com' not exists

  Scenario: put account fails because birthday is incorrect
    When put account
      """
      {
          "birthday": "2019-02-30",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    Then account error only contains
      """
      {
          "path": "/birthday",
          "code": "format"
      }
      """

  Scenario: put account fails because creation_date is incorrect
    When put account
     """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont",
          "creation_date": "2019-02-30T19:16:40Z"
      }
      """
    Then account error contains 2 errors
    And account error contains
      """
      {"path":"/creation_date","code":"format"}
      """
    And account error contains
      """
      {"path":"/creation_date","code":"readOnly"}
      """

  Scenario: put account fails because creation_date is readonly
    When put account
     """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont",
          "creation_date": "2019-02-25T19:16:40Z"
      }
      """
    Then account error only contains
      """
      {
          "path": "/creation_date",
          "code": "readOnly"
      }
      """

  Scenario: put account fails because a property is unknown
    When put account
     """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont",
          "unknown": "nc"
      }
      """
    Then account error only contains
      """
      {
          "path": "/unknown",
          "code": "additionalProperties"
      }
      """

  Scenario: patch account fails because an address is incomplete
    When put account
     """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont",
          "addresses": {
               "job": {
                  "city": "Paris"
                }
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

  Scenario: put account fails because two many addresses
    When put account
     """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont",
          "addresses": {
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
      }
      """
    Then account error only contains
      """
      {
          "path": "/addresses",
          "code": "maxProperties"
      }
      """

  Scenario: put account fails because username already exists
    Given delete username 'jean.dupont@gmail.com'
    And create account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupont@gmail.com",
          "lastname": "Dupont"
      }
      """
    When put account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    Then account error only contains
      """
      {
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }
      """
