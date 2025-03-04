Feature: api stock

  Background:
    Given get authorization to read & update stock for client 'back'

  Scenario: increase stock
    When increase of 5 for product 'product1' from store 'store'
    And stock event is
      """
      {
          "quantity": 5,
          "product": "product1"
      }
      """
    And increase of 20 for product 'product1' from store 'store'
    And stock event is
      """
      {
          "quantity": 20,
          "product": "product1"
      }
      """
    And increase of -12 for product 'product1' from store 'store'
    And stock event is
      """
      {
          "quantity": -12,
          "product": "product1"
      }
      """
    Then stock of product 'product1' from store 'store' is 13

  Scenario: descrease stock fails because stock is insufficient
    When increase of 5 for product 'product1' from store 'store'
    And increase of -10 for product 'product1' from store 'store'
    Then stock of product 'product1' from store 'store' is 5, is insufficient for -10

  Scenario: get stock fails because none stock exists
    When get stock of product 'product1' from store 'store'
    Then stock of product 'product1' from store 'store' is unknown
