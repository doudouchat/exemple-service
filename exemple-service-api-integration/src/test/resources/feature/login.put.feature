Feature: api put login

  Scenario: put login
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """
    When put login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp123",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b",
          "disabled": true
      }
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

  Scenario: put login fails because id is required
    Given delete username 'jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "password": "mdp",
          "username": "jean.dupond@gmail.com",
          "id": "c75bf422-87da-4bba-a124-6fc4f9c52eaf"
      }
      """
    When put login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      {
        "username": "jean.dupond@gmail.com",
        "password": "mdp123",
        "disabled": true
      }
      """
    Then login status is 400
    And login error is expect 'message'
      """
      [{
          "path": "/id",
          "code": "required"
      }]
      """
