package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendResponse;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.api.model.*;

import java.util.List;

public interface TransactionManager {

    SendResponse send(SendRequest sendRequest);

    SendResponse sendSignedTransaction(SendSignedRequest sendRequest);

    void delete(DeleteRequest request);

    ResendResponse resend(ResendRequest request);

    MessageHash storePayload(EncodedPayload transactionPayload);

    ReceiveResponse receive(ReceiveRequest request);

    StoreRawResponse store(StoreRawRequest storeRequest);

    boolean isSender(String ptmHash);

    List<PublicKey> getParticipants(String ptmHash);
}
