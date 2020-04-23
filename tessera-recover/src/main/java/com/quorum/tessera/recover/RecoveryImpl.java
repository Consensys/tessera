package com.quorum.tessera.recover;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;

import javax.persistence.EntityManagerFactory;
import java.util.Objects;

public class RecoveryImpl implements Recovery {

    private final EntityManagerFactory entityManagerFactory;

    private final PartyInfoService partyInfoService;

    public RecoveryImpl(EntityManagerFactory entityManagerFactory,PartyInfoService partyInfoService) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @Override
    public void recover() {





    }
}
