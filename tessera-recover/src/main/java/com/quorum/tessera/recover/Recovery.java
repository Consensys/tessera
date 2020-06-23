package com.quorum.tessera.recover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public interface Recovery {

    Logger LOGGER = LoggerFactory.getLogger(Recovery.class);

    default int recover() {

        final long startTime = System.nanoTime();

        LOGGER.debug("Requesting transactions from other nodes");
        final RecoveryResult resendResult = request();

        final long resendFinished = System.nanoTime();

        LOGGER.debug("Perform staging of transactions");
        final RecoveryResult stageResult = stage();

        final long stagingFinished = System.nanoTime();

        LOGGER.debug("Perform synchronisation of transactions");
        final RecoveryResult syncResult = sync();

        final long syncFinished = System.nanoTime();

        LOGGER.info(
                "Resend Stage: {} (duration = {} ms). Staging Stage: {} (duration = {} ms). Sync Stage: {} (duration = {} ms)",
                resendResult,
                (resendFinished - startTime) / 1000000,
                stageResult,
                (stagingFinished - resendFinished) / 1000000,
                syncResult,
                (syncFinished - stagingFinished) / 1000000);

        final long endTime = System.nanoTime();
        LOGGER.info("Recovery process took {} ms", (endTime - startTime) / 1000000);

        return Stream.of(resendResult, stageResult, syncResult).map(RecoveryResult::getCode).reduce(Integer::max).get();
    }

    RecoveryResult request();

    RecoveryResult stage();

    RecoveryResult sync();
}
