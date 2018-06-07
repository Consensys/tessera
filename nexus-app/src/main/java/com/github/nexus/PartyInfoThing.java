package com.github.nexus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartyInfoThing {

    private final String url;

    private final Recipient recipient;

    private final List<Party> parties;

    private PartyInfoThing(String url, Recipient recipient, Party... party) {
        this.url = url;
        this.recipient = recipient;
        this.parties = Arrays.asList(party);
    }

    public String getUrl() {
        return url;
    }

    public Recipient getRecipient() {
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

        assert (numberOfRecipients == 1);
        assert (recipientElementCount == 2);

        final int recipientKeyLength = (int) byteBuffer.getLong();
        final byte[] recipientKeyBytes = new byte[recipientKeyLength];
        byteBuffer.get(recipientKeyBytes);

        final int length = (int) byteBuffer.getLong();
        final byte[] valueData = new byte[length];
        byteBuffer.get(valueData);
        final String recipientUrl = new String(valueData);

        final long partyCount = byteBuffer.getLong();

        final List<Party> parties = new ArrayList<>();
        for (long i = 0; i < partyCount; i++) {
            long partyElementLength = byteBuffer.getLong();
            byte[] ptyData = new byte[(int) partyElementLength];
            byteBuffer.get(ptyData);
            String ptyURL = new String(ptyData);
            parties.add(new Party(ptyURL));
        }

        return new PartyInfoThing(url, new Recipient(recipientKeyBytes, recipientUrl), parties.toArray(new Party[0]));
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
