Feature: api patch login

  Scenario: patch login
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """
    When patch login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/password",
           "value": "mdp123"
         },
         {
           "op": "add",
           "path": "/disabled",
           "value": true
         }
      ]
      """
    Then login status is 204
    And login 'jean.dupond@gmail.com' exists
    And login is
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b",
          "disabled": true
      }
      """
    And login password is 'mdp123'

  Scenario: patch login fails because id is readonly
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "password": "mdp",
          "username": "jean.dupond@gmail.com",
          "id": "c75bf422-87da-4bba-a124-6fc4f9c52eaf"
      }
      """
    When patch login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/id",
           "value": "00f6fcbf-86d4-426f-9661-006d19163b3d"
         }
      ]
      """
    Then login status is 400
    And login error is expect 'message'
      """
      [{
          "path": "/id",
          "code": "readOnly"
      }]
      """

  Scenario: patch login fails because username is readonly
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "password": "mdp",
          "username": "jean.dupond@gmail.com",
          "id": "c75bf422-87da-4bba-a124-6fc4f9c52eaf"
      }
      """
    When patch login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/username",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    Then login status is 400
    And login error is expect 'message'
      """
      [{
          "path": "/username",
          "code": "readOnly"
      }]
      """
