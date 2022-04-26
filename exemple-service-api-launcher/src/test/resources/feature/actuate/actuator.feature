Feature: actuator

  Scenario: info
    When actuator info
    Then actuator status is 200
    And actuator contains 'version'
    And actuator contains 'buildTime'

  Scenario: health
    When actuator health
    Then actuator status is 200
    And actuator is
      """
      {
          "status": "UP"
      }
      """

  Scenario: info page
    When actuator info html
    Then actuator status is 200
    And actuator html contains 'version'
