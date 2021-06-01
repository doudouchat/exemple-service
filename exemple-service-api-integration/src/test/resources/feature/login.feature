Feature: api login

  Scenario: create login
    Given delete username 'jean.dupond@gmail.com'
    When create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """
    Then login status is 201
    And login 'jean.dupond@gmail.com' exists
    And login is
      """
      {
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """

  Scenario: delete login
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "bcc4e98f-c9ae-4a33-b2c3-7cc49d9b1b71"
      }
      """
    When delete login 'jean.dupond@gmail.com'
    Then login status is 204
    But login 'jean.dupond@gmail.com' not exists

  Scenario: create login fails because username already exists
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "607a1829-8972-463e-9a91-bc55688edc13"
      }
      """
    When create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "179d5fa8-dabd-4363-9668-cd295fc90a51"
      }
      """
    Then login status is 400
    And login error is
      """
      [{
          "path": "/username",
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }]
      """
