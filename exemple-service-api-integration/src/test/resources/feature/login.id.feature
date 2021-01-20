Feature: api login by id

  Scenario: multi logins
    Given delete username 'jean.dupond@gmail.com'
    And delete username 'pierre.dupond@gmail.com'
    And delete username 'new_jean.dupond@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "535d03b3-e2b1-4e11-8ffe-9b5dba99bd8b"
      }
      """
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "pierre.dupond@gmail.com",
          "password": "mdp",
          "id": "535d03b3-e2b1-4e11-8ffe-9b5dba99bd8b"
      }
      """
    When get login by id 535d03b3-e2b1-4e11-8ffe-9b5dba99bd8b for application 'test' and version 'v1'
    Then login status is 200
    And logins are
      """
      [
        {
          "username": "jean.dupond@gmail.com",
          "id": "535d03b3-e2b1-4e11-8ffe-9b5dba99bd8b"
        },
        {
          "username": "pierre.dupond@gmail.com",
          "id": "535d03b3-e2b1-4e11-8ffe-9b5dba99bd8b"
        }
      ]
      """
    And all passwords are 'mdp'

  Scenario: get login fails because none login is found
    When get login by id 953d5ddb-92f2-4d79-b7c0-2828a34186ec for application 'test' and version 'v1'
    Then login status is 404
