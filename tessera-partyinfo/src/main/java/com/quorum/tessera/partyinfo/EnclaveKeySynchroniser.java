package com.quorum.tessera.partyinfo;

import java.util.Objects;


public class EnclaveKeySynchroniser implements Runnable {

    private PartyInfoService partyInfoService;

    public EnclaveKeySynchroniser(final PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @Override
    public void run() {
        partyInfoService.syncKeys();
    }
}
