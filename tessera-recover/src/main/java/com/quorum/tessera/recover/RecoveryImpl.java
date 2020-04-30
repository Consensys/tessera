package com.quorum.tessera.recover;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.sync.TransactionRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Objects;
import java.util.Set;

public class RecoveryImpl implements Recovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryImpl.class);

    private final StagingEntityDAO stagingEntityDAO;

    private final PartyInfoService partyInfoService;

    private final TransactionRequester transactionRequester;

    public RecoveryImpl(StagingEntityDAO stagingEntityDAO,
                        PartyInfoService partyInfoService,
                        TransactionRequester transactionRequester) {
        this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
    }

    @Override
    public void recover() {

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();
        final Set<Party> parties = partyInfo.getParties();

        parties.stream()
            .filter(p -> !transactionRequester.requestAllTransactionsFromNode(p.getUrl()))
            .forEach(p -> {
                LOGGER.warn("Unable to request batch resend for party: {}" + p.getUrl());
            });

        stagingEntityDAO.countAll();


    }


}
