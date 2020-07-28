package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class RecipientExclusionCache implements ExclusionCache<Recipient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientExclusionCache.class);

    private final ConcurrentSkipListSet<Recipient> excluded
        = new ConcurrentSkipListSet<>(Comparator.comparing(Recipient::getUrl));

    @Override
    public boolean isExcluded(Recipient recipient) {
        return excluded.contains(recipient);
    }

    @Override
    public ExclusionCache<Recipient> exclude(Recipient recipient) {
        excluded.add(recipient);
        return this;
    }

    @Override
    public Optional<Recipient> include(String recipientUrl) {

        Optional<Recipient> recipient = excluded.stream()
            .filter(r -> r.getUrl().equalsIgnoreCase(recipientUrl))
            .findFirst();

        recipient.ifPresent(excluded::remove);
        return recipient;
    }


}
