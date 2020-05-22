package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EncodedPayloadPublisher implements BatchWorkflowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadPublisher.class);

    private Enclave enclave;

    private List<EncodedPayload> payloads;

    private ResendBatchPublisher resendBatchPublisher;

    private long messageCounter = 0L;

    public EncodedPayloadPublisher(Enclave enclave,
                                   ResendBatchPublisher resendBatchPublisher) {
        this.enclave = enclave;
        this.resendBatchPublisher = resendBatchPublisher;
        this.payloads = new ArrayList<>();
    }

    @Override
    public boolean execute(BatchWorkflowContext event) {

        final int batchSize = event.getBatchSize();

        final PublicKey recipientKey = event.getRecipientKey();

        if (enclave.getPublicKeys().contains(recipientKey)) {
            // we are trying to send something to ourselves - don't do it
            LOGGER.debug(
                "Trying to send message to ourselves with key {}, not publishing", recipientKey.encodeToBase64());
            return true;
        }

        payloads.add(event.getEncodedPayload());

        if((payloads.size() == batchSize)) {
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
