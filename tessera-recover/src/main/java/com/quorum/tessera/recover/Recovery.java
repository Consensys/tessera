package com.quorum.tessera.recover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public interface Recovery {

    Logger LOGGER = LoggerFactory.getLogger(Recovery.class);

    default int recover() {

        long startTime = System.nanoTime();

        LOGGER.debug("Requesting transactions from other nodes");

        RecoveryResult resendResult = request();

        LOGGER.info("Request resend result : {}", resendResult);

        LOGGER.debug("Perform staging of transactions");

        RecoveryResult stageResult = stage();

        LOGGER.info("Staging result : {}", stageResult);

        LOGGER.debug("Perform synchronisation of transactions");

        RecoveryResult syncResult = sync();

        LOGGER.info("Synchronisation result : {}", syncResult);

        long endTime = System.nanoTime();
        LOGGER.info("Recovery process took {} milliseconds", (endTime - startTime) / 1000000);

        return Stream.of(resendResult, stageResult, syncResult).map(RecoveryResult::getCode).reduce(Integer::max).get();
    }

    RecoveryResult request();

    RecoveryResult stage();

    RecoveryResult sync();
}
