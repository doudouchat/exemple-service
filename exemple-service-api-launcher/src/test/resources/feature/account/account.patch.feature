Feature: api patch account

  Background: 
    Given delete username 'jean.dupond@gmail.com'
    And get authorization to create account for client 'test'
    And account
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
    And get authorization from account 'jean.dupond@gmail.com' and client 'test'

  Scenario: patch account
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/lastname",
           "value": "Dupond"
         },
         {
           "op": "replace",
           "path": "/addresses/job",
           "value": {"city" : "New-York", "street": "5th avenue"}
         },
         {
           "op": "replace",
           "path": "/addresses/home/city",
           "value": "Paris"
         },
         {
           "op": "remove",
           "path": "/civility"
         },
         {
           "op": "add",
           "path": "/cgus/1",
           "value": {"code" : "code_1", "version": "v1"}
         }
      ]
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
    And account event is
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