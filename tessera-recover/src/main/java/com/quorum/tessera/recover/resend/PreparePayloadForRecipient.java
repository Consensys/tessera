package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Objects;

public class PreparePayloadForRecipient implements BatchWorkflowAction {

    private PayloadEncoder payloadEncoder;

    public PreparePayloadForRecipient(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = payloadEncoder;
    }

    @Override
    public boolean execute(BatchWorkflowContext event) {

        EncodedPayload encodedPayload = event.getEncodedPayload();
        PublicKey recipientPublicKey = event.getRecipientKey();

        if(Objects.equals(encodedPayload.getSenderKey(), recipientPublicKey)) {
            return true;
        }
        EncodedPayload adjustedPayload = payloadEncoder.forRecipient(encodedPayload,recipientPublicKey);

        event.setEncodedPayload(adjustedPayload);

        return true;

    }
}
