package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EncodedPayloadPublisher implements BatchWorkflowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadPublisher.class);

    private List<EncodedPayload> payloads;

    private ResendBatchPublisher resendBatchPublisher;

    private long messageCounter = 0L;

    public EncodedPayloadPublisher(ResendBatchPublisher resendBatchPublisher) {
        this.resendBatchPublisher = resendBatchPublisher;
        this.payloads = new ArrayList<>();
    }

    @Override
    public boolean execute(BatchWorkflowContext event) {

        final int batchSize = event.getBatchSize();

        payloads.add(event.getEncodedPayload());

        long total = event.getExpectedTotal();

        if((payloads.size() == batchSize || total <= payloads.size()) || messageCounter >= total) {
            resendBatchPublisher.publishBatch(payloads, event.getRecipient().getUrl());
            messageCounter += payloads.size();
            payloads.clear();
        }

        return true;
    }

    public long getPublishedCount() {
        return messageCounter;
    }
}
