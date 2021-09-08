package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressSendIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(StressSendIT.class);

  private static final String SEND_PATH = "/send";

  private final Client client = ClientBuilder.newClient();

  private RestUtils utils = new RestUtils();

  private PartyHelper partyHelper = PartyHelper.create();

  private static final int MAX_COUNT = 20000;
  private static final int THREAD_COUNT = 10;

  /** Quorum sends transaction with single public recipient key */
  @Test
  public void sendToSingleRecipientUntilFailureOrMaxReached() {
    LOGGER.info("stress test starting");

    final Party firstParty = partyHelper.findByAlias("A");
    final Party secondParty = partyHelper.findByAlias("B");
    byte[] transactionData = utils.createTransactionData();

    final AtomicInteger sendCounter = new AtomicInteger(0);
    final AtomicInteger invalidResults = new AtomicInteger(0);

    final List<Thread> stressThreads = new ArrayList<>();
    for (int i = 0; i < THREAD_COUNT; i++) {
      final Thread stressThread =
          new Thread(
              () -> {
                int currentCount = sendCounter.incrementAndGet();
                while (currentCount < MAX_COUNT) {
                  final SendRequest sendRequest = new SendRequest();
                  sendRequest.setFrom(firstParty.getPublicKey());
                  sendRequest.setTo(secondParty.getPublicKey());
                  sendRequest.setPayload(transactionData);

                  try (Response response =
                      client
                          .target(firstParty.getQ2TUri())
                          .path(SEND_PATH)
                          .request()
                          .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON))) {

                    if (response.getStatus() != 201) {
                      LOGGER.info("Response is not 201. MessageCount=" + currentCount);
                      sendCounter.addAndGet(MAX_COUNT);
                      invalidResults.incrementAndGet();
                    }
                  }

                  currentCount = sendCounter.incrementAndGet();
                  if (currentCount % 1000 == 0) {
                    LOGGER.info("currentCount={}", currentCount);
                  }
                }
              });
      stressThread.start();
      stressThreads.add(stressThread);
    }

    // wait for stress threads to finish
    for (int i = 0; i < THREAD_COUNT; i++) {
      try {
        stressThreads.get(i).join();
      } catch (InterruptedException e) {
        LOGGER.error("Error while waiting for clients to stop.", e);
      }
    }
    LOGGER.info("stress test finished");
    assertThat(invalidResults.get()).isEqualTo(0);
  }
}
