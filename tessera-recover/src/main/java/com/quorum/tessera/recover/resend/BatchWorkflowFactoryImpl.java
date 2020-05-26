package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class BatchWorkflowFactoryImpl implements BatchWorkflowFactory {

    private Enclave enclave;

    private PayloadEncoder payloadEncoder;

    private PartyInfoService partyInfoService;

    private ResendBatchPublisher resendBatchPublisher;

    private long transactionCount;

    public void setEnclave(Enclave enclave) {
        this.enclave = enclave;
    }

    public void setPayloadEncoder(PayloadEncoder payloadEncoder) {
        this.payloadEncoder = payloadEncoder;
    }

    public void setPartyInfoService(PartyInfoService partyInfoService) {
        this.partyInfoService = partyInfoService;
    }

    public void setResendBatchPublisher(ResendBatchPublisher resendBatchPublisher) {
        this.resendBatchPublisher = resendBatchPublisher;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    @Override
    public BatchWorkflow create() {

        ValidateEnclaveStatus validateEnclaveStatus = new ValidateEnclaveStatus(enclave);
        DecodePayloadHandler decodePayloadHandler = new DecodePayloadHandler(payloadEncoder);
        PreparePayloadForRecipient preparePayloadForRecipient = new PreparePayloadForRecipient(payloadEncoder);
        FindRecipientFromPartyInfo findRecipientFromPartyInfo = new FindRecipientFromPartyInfo(partyInfoService);
        FilterPayload filterPayload = new FilterPayload(enclave);
        SearchRecipentKeyForPayload searchRecipentKeyForPayload = new SearchRecipentKeyForPayload(enclave);
        SenderIsNotRecipient senderIsNotRecipient = new SenderIsNotRecipient(enclave);
        EncodedPayloadPublisher encodedPayloadPublisher = new EncodedPayloadPublisher(resendBatchPublisher);

        List<BatchWorkflowAction> handlers = List.of(validateEnclaveStatus, decodePayloadHandler,filterPayload, preparePayloadForRecipient, searchRecipentKeyForPayload, findRecipientFromPartyInfo,senderIsNotRecipient, encodedPayloadPublisher);

        return new BatchWorkflow() {

            private final AtomicLong filteredMessageCount = new AtomicLong(transactionCount);

            @Override
            public boolean execute(BatchWorkflowContext context) {
                boolean outcome = handlers.stream()
                    .filter(Predicate.not(h -> h.execute(context)))
                    .findFirst()
                    .isEmpty();

                if(outcome) {
                    context.setExpectedTotal(filteredMessageCount.decrementAndGet());
                }
                return outcome;
            }

            @Override
            public long getPublishedMessageCount() {
                return encodedPayloadPublisher.getPublishedCount();
            }
        };
    }
}
