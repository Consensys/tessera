package com.quorum.tessera.partyinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KnownPeerChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(KnownPeerChecker.class);

    private static final URLNormalizer urlNormalizer = URLNormalizer.create();

    private static final List<String> LOCALHOST_ALIASES = Arrays.asList("localhost", "127.0.0.1");

    private Set<String> peers;

    public KnownPeerChecker(Set<String> peers) {
        this.peers = peers;
    }

    public boolean isKnown(String url) {
        LOGGER.debug("Checking if {} is a known peer", url);

        if (Objects.isNull(peers) || peers.isEmpty()) {
            LOGGER.debug("No peers");
            return false;
        }

        for (String peer : peers) {
            try {
                final String normalizedUrl = urlNormalizer.normalize(url);
                final String normalizedPeer = urlNormalizer.normalize(peer);

                if (normalizedUrl.equals(normalizedPeer)) {
                    LOGGER.debug("{} is known", url);
                    return true;
                }

                final URL u = new URL(normalizedUrl);
                final URL p = new URL(normalizedPeer);
                final List<String> hosts = Arrays.asList(u.getHost(), p.getHost());

                if (!u.getHost().equals(p.getHost()) && LOCALHOST_ALIASES.containsAll(hosts)) {
                    LOGGER.debug("localhost URLs: checking if IP or hostname match");
                    if (u.equals(p)) {
                        LOGGER.debug("{} is known", url);
                        return true;
                    }
                }
            } catch (MalformedURLException | RuntimeException e) {
                throw new RuntimeException(String.format("unable to check if %s is a known peer", url), e);
            }
        }
        LOGGER.debug("{} is not known", url);
        return false;
    }
}
