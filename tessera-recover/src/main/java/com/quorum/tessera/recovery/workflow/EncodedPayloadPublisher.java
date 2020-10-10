package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

import java.util.ArrayList;
import java.util.List;

public class EncodedPayloadPublisher implements BatchWorkflowAction {

    private String targetUrl;

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

        targetUrl = event.getRecipient().getUrl();

        payloads.add(event.getEncodedPayload());

        long total = event.getExpectedTotal();

        if (payloads.size() == batchSize || payloads.size() >= total || messageCounter + payloads.size() >= total) {
            resendBatchPublisher.publishBatch(payloads, targetUrl);
            messageCounter += payloads.size();
            payloads.clear();
        }

        return true;
    }

    public long getPublishedCount() {
        return messageCounter;
    }

    public void checkOutstandingPayloads(BatchWorkflowContext event) {

        final long total = event.getExpectedTotal();

        final int noOfPayloads = payloads.size();

        if (noOfPayloads > 0 && (noOfPayloads >= total || messageCounter + noOfPayloads >= total)) {
            resendBatchPublisher.publishBatch(payloads, targetUrl);
            messageCounter += payloads.size();
            payloads.clear();
        }
    }
}
