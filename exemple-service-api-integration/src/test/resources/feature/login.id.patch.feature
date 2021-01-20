Feature: api patch login by id

  Scenario: patch username
    Given delete username 'jean.dupond@gmail.com'
    And delete username 'pierre.dupond@gmail.com'
    And delete username 'new_jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "fdf83e1b-7c43-4e5b-bd03-858d626014a0"
      }
      """
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "pierre.dupond@gmail.com",
          "password": "mdp",
          "id": "fdf83e1b-7c43-4e5b-bd03-858d626014a0"
      }
      """
    When patch login by id fdf83e1b-7c43-4e5b-bd03-858d626014a0 for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/0/username",
           "value": "new_jean.dupond@gmail.com"
         }
      ]
      """
    Then login status is 204
    And login by id fdf83e1b-7c43-4e5b-bd03-858d626014a0 exists
    And logins are
      """
      [
        {
          "username": "new_jean.dupond@gmail.com",
          "id": "fdf83e1b-7c43-4e5b-bd03-858d626014a0"
        },
        {
          "username": "pierre.dupond@gmail.com",
          "id": "fdf83e1b-7c43-4e5b-bd03-858d626014a0"
        }
      ]
      """
    And all passwords are 'mdp'

  Scenario: patch username fails because username already exists
    Given delete username 'jean.dupond@gmail.com'
    And delete username 'pierre.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "06f2006f-96c6-450d-b39e-f397edcba995"
      }
      """
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "pierre.dupond@gmail.com",
          "password": "mdp",
          "id": "096132d9-7a78-463f-9cd1-397d888bfcc9"
      }
      """
    When patch login by id 06f2006f-96c6-450d-b39e-f397edcba995 for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/0/username",
           "value": "pierre.dupond@gmail.com"
         }
      ]
      """
    Then login status is 400
    And login error is
      """
      [{
          "path": "/username",
          "code": "username",
          "message": "[pierre.dupond@gmail.com] already exists"
      }]
      """

  Scenario: patch password
    Given delete username 'jean.dupond@gmail.com'
    And delete username 'pierre.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "2a8aae11-ae15-41b2-904a-a5ae9a6390ae"
      }
      """
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "pierre.dupond@gmail.com",
          "password": "mdp",
          "id": "2a8aae11-ae15-41b2-904a-a5ae9a6390ae"
      }
      """
    When patch login by id 2a8aae11-ae15-41b2-904a-a5ae9a6390ae for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/0/password",
           "value": "mdp123"
         },
         {
           "op": "replace",
           "path": "/1/password",
           "value": "mdp123"
         }
      ]
      """
    Then login status is 204
    And login by id 2a8aae11-ae15-41b2-904a-a5ae9a6390ae exists
    And all passwords are 'mdp123'

  Scenario: add login
    Given delete username 'jean.dupond@gmail.com'
    And delete username 'pierre.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "cefda548-ff3e-4955-a8f4-73815d08ceee"
      }
      """
    When patch login by id cefda548-ff3e-4955-a8f4-73815d08ceee for application 'test' and version 'v1'
      """
      [
         {
           "op": "copy",
           "path": "/1",
           "from": "/0"
         },
         {
           "op": "replace",
           "path": "/1/username",
           "value": "pierre.dupond@gmail.com"
         }
      ]
      """
    Then login status is 204
    And login by id cefda548-ff3e-4955-a8f4-73815d08ceee exists
    And logins are
      """
      [
        {
          "username": "jean.dupond@gmail.com",
          "id": "cefda548-ff3e-4955-a8f4-73815d08ceee"
        },
        {
          "username": "pierre.dupond@gmail.com",
          "id": "cefda548-ff3e-4955-a8f4-73815d08ceee"
        }
      ]
      """
    And all passwords are 'mdp'
