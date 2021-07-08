package com.quorum.tessera.test.rest;

import io.cucumber.java8.En;
import io.cucumber.java8.PendingException;

public class ReceiveSteps implements En {

  public ReceiveSteps() {
    Given(
        "Party as been sent transaction data",
        () -> {
          // Write code here that turns the phrase above into concrete actions
          throw new PendingException();
        });

    When(
        "Party receives request for transaction from Quorum peer",
        () -> {
          // Write code here that turns the phrase above into concrete actions
          throw new PendingException();
        });

    Then(
        "Party returns transaction data",
        () -> {
          // Write code here that turns the phrase above into concrete actions
          throw new PendingException();
        });

    Then(
        "Party data not found error",
        () -> {
          // Write code here that turns the phrase above into concrete actions
          throw new PendingException();
        });
  }
}
