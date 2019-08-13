package com.quorum.tessera.transaction.batch;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManagerWrapper;
import com.quorum.tessera.transaction.TransactionRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BatchResendSync implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResendSync.class);

    private final PartyInfoService partyInfoService;

    private final BatchResendManager batchResendManager;

    private final long startupDelay;

    private final ProcessControl processControl;

    private final TransactionRequester transactionRequester;
    
    private final TransactionManagerWrapper transactionManagerWrapper;

    private final Set<String> unsuccessfulResendBatchParties = new HashSet<>();
    private final Set<String> resendParties = new HashSet<>();

    private volatile boolean mustStop = false;

    public BatchResendSync(
            PartyInfoService partyInfoService,
            BatchResendManager batchResendManager,
            long startupDelay,
            ProcessControl processControl,
            TransactionRequester transactionRequester,
            TransactionManagerWrapper transactionManagerWrapper) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.batchResendManager = Objects.requireNonNull(batchResendManager);
        this.startupDelay = startupDelay;
        this.processControl = Objects.requireNonNull(processControl);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
        this.transactionManagerWrapper = Objects.requireNonNull(transactionManagerWrapper);
    }

    @PostConstruct
    public void start() {
        processControl.start(this, () -> this.setMustStop(true));
    }

    @Override
    public void run() {

        LOGGER.info("Starting Batch Resend Syncrhonization");
        try {
            transactionManagerWrapper.setResendMode(true);
            checkAndStop();
            // clean the staging area
            LOGGER.info("Cleaning up staging area");
            batchResendManager.cleanupStagingArea();
            LOGGER.info("Sleeping - startupDelay=" + startupDelay);
            Thread.sleep(startupDelay);
            checkAndStop();
            buildResendParties();
            // request batch resends from every known party (allow for retries) - record unsuccessful attempts
            final BatchResendManager.Result requestResendFromParties = requestBatchResendsFromKnownParties();
            logUnsuccessfulParties();

            // stage data from the staging area
            checkAndStop();
            final BatchResendManager.Result staging = batchResendManager.performStaging();
            // sync with ENCRYPTED_TRANSACTION - via push API??
            checkAndStop();
            final BatchResendManager.Result sync = batchResendManager.performSync();

            // TODO maybe do this as an extra feature later on
            // enable the push API
            // send resendAll requests to the hosts where resend batch was unsuccessful

            LOGGER.info(
                    "Batch resend results: RequestResendFromParties={} Staging={} Sync={}.",
                    requestResendFromParties,
                    staging,
                    sync);
            LOGGER.info("Please analyze the logs and start tessera in the appropriate mode.");
            // shutdown at the end
            processControl.exit(ProcessControl.SUCCESS);
        } catch (InterruptedException | ProcessStoppedException e) {
            LOGGER.info("The process was deliberately stopped.", e);
            processControl.exit(ProcessControl.STOPPED);
        } catch (Exception e) {
            LOGGER.info("A fatal exception has occurred which caused batch resend to stop.", e);
            processControl.exit(ProcessControl.FAILURE);
        }
    }

    private void logUnsuccessfulParties() {
        for (String url : unsuccessfulResendBatchParties) {
            LOGGER.info("Unable to request batch resend for party: " + url);
        }
    }

    private void buildResendParties() {
        PartyInfo partyInfo = partyInfoService.getPartyInfo();
        resendParties.addAll(
                partyInfo.getParties().stream()
                        .map(Party::getUrl)
                        .map(url -> url.endsWith("/") ? url : url + "/")
                        .collect(Collectors.toList()));
        resendParties.remove(partyInfo.getUrl().endsWith("/") ? partyInfo.getUrl() : partyInfo.getUrl() + "/");
    }

    public void setMustStop(boolean mustStop) {
        this.mustStop = mustStop;
    }

    private BatchResendManager.Result requestBatchResendsFromKnownParties() {
        int successfulParties = 0;
        resendParties.forEach(rp -> LOGGER.info("ResendParty: {}" + rp));
        for (String party : resendParties) {
            checkAndStop();
            if (transactionRequester.requestAllTransactionsFromNode(party)) {
                successfulParties++;
            } else {
                unsuccessfulResendBatchParties.add(party);
            }
        }
        if (successfulParties == resendParties.size()) {
            return BatchResendManager.Result.SUCCESS;
        }
        if (successfulParties > 0) {
            return BatchResendManager.Result.PARTIAL_SUCCESS;
        }
        return BatchResendManager.Result.FAILURE;
    }

    private void checkAndStop() throws ProcessStoppedException {
        if (mustStop) {
            throw new ProcessStoppedException();
        }
    }
}
