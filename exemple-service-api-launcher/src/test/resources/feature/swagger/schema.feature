Feature: schema

  Scenario: get swagger
    When get swagger of application 'test'
    Then schema status is 200

  Scenario: get patch
    When get schema patch
    Then schema status is 200

  Scenario Outline: get schema <application>
    When get schema of application <application>
    Then schema status is 200
    And schema is cached in keyspace <keyspace>

    Examples: 
      | application | keyspace         |
      | 'test'      | 'test_keyspace'  |
      | 'other'     | 'other_keyspace' |
