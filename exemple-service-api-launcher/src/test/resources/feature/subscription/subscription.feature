Feature: api subscription

  Background:
    Given get authorization to read & update subscription for client 'test'

  Scenario: create subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is
      """
      {
      }
      """
    And subscription contains 'subscription_date'

  Scenario: update subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com'
    And create subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is
      """
      {
      }
      """
    And subscription contains 'subscription_date'

  Scenario: get subscription fails because none subscription exists
    When delete subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is unknown

  Scenario: update subscription fails because email is incorrect
    When create subscription 'jean.dupond'
    Then subscription error only contains
      """
      {
          "path": "/email",
          "code": "format"
      }
      """
