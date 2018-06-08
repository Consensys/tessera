package com.github.nexus.enclave;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.PayloadEncoder;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private final TransactionService transactionService;

    private final PayloadEncoder payloadEncoder;

    public EnclaveImpl(final TransactionService transactionService, PayloadEncoder payloadEncoder) {
        this.transactionService = requireNonNull(transactionService,"transactionService cannot be null");
        this.payloadEncoder = requireNonNull(payloadEncoder,"encoder cannot be null");
    }

    @Override
    public boolean delete(final MessageHash hash) {
        return transactionService.delete(hash);
    }

    @Override
    public byte[] receive(byte[] key, byte[] to) {
        return transactionService.retrieveUnencryptedTransaction(new MessageHash(key), new Key(to));
    }

    @Override
    public MessageHash store(byte[] sender, byte[][] recipients, byte[] message) {
        Key senderPublicKey = new Key(sender);
        List<Key> recipientList = Arrays.stream(recipients)
            .map(recipient -> new Key(recipient))
            .collect(Collectors.toList());

        EncodedPayloadWithRecipients encryptedPayload =
            transactionService.encryptPayload(message, senderPublicKey, recipientList);

        return transactionService.storeEncodedPayload(encryptedPayload);


    }
}
