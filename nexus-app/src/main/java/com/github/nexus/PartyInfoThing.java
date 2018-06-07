package com.github.nexus;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoThing {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoThing.class);
    
    private final String url;

    private final List<Recipient> recipient;

    private final List<Party> parties;

    private PartyInfoThing(String url, Recipient[] recipient, Party[] party) {
        this.url = url;
        this.recipient = Arrays.asList(recipient);
        this.parties = Arrays.asList(party);
    }

    public String getUrl() {
        return url;
    }

    public List<Recipient> getRecipients() {
        return recipient;
    }

    public List<Party> getParties() {
        return parties;
    }

    public static PartyInfoThing from(byte[] data) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        final int urlLength = (int) byteBuffer.getLong();

        final byte[] urlBytes = new byte[urlLength];
        byteBuffer.get(urlBytes);
        final String url = new String(urlBytes);

        final int numberOfRecipients = (int) byteBuffer.getLong();
        final int recipientElementCount = (int) byteBuffer.getLong();
        LOGGER.trace("recipientElementCount {}",recipientElementCount);
        
        final Recipient[] recipients = new Recipient[numberOfRecipients];
        
        for (int i = 0; i < numberOfRecipients; i++) {
            final int recipientKeyLength = (int) byteBuffer.getLong();
            final byte[] recipientKeyBytes = new byte[recipientKeyLength];
            byteBuffer.get(recipientKeyBytes);

            final int recipientUrlValueLength = (int) byteBuffer.getLong();
            final byte[] urlValueData = new byte[recipientUrlValueLength];
            byteBuffer.get(urlValueData);
            final String recipientUrl = new String(urlValueData);

            recipients[i] = new Recipient(recipientKeyBytes, recipientUrl);

        }


        final int partyCount = (int) byteBuffer.getLong();

        final Party[] parties = new Party[partyCount];
        for (int i = 0; i < partyCount; i++) {
            long partyElementLength = byteBuffer.getLong();
            byte[] ptyData = new byte[(int) partyElementLength];
            byteBuffer.get(ptyData);
            String ptyURL = new String(ptyData);
            parties[i] = new Party(ptyURL);
        }

        return new PartyInfoThing(url,recipients, parties);
    }

    public static class Recipient {

        private final byte[] key;

        private final String url;

        public Recipient(byte[] key, String url) {
            this.url = url;
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public byte[] getKey() {
            return key;
        }
    }

    public static class Party {

        private final String url;

        public Party(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

    }

}
