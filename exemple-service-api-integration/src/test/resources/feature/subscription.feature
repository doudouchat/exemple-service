Feature: api subscription

  Scenario: create subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    Then subscription status is 201
    And subscription 'jean.dupond@gmail.com' is
      """
      {
      }
      """
    And subscription contains 'subscription_date'

  Scenario: update subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    And create subscription 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    Then subscription status is 204
    And subscription 'jean.dupond@gmail.com' is
      """
      {
      }
      """
    And subscription contains 'subscription_date'

  Scenario: get subscription fails because none subscription exists
    Given delete subscription 'jean.dupond@gmail.com'
    When get subscription 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    Then subscription status is 404

  Scenario: update subscription fails because email is incorrect
    When create subscription 'jean.dupond' for application 'test' and version 'v1'
    Then subscription status is 400
    And subscription error is
      """
      [{
          "path": "/email",
          "code": "format"
      }]
      """
