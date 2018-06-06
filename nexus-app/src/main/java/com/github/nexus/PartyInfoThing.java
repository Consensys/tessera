package com.github.nexus;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class PartyInfoThing {

    private final String url;

    private final Recipient recipient;

    private final List<Party> parties;
    
    private PartyInfoThing(String url, Recipient recipient,Party... party) {
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

        byte[] firstNum = Arrays.copyOfRange(data, 0, 8);

        long num = ByteBuffer.wrap(firstNum).getLong();

        int end = (8 + (int) num);

        byte[] firstBitOfData = Arrays.copyOfRange(data, 8, end);

        String url = new String(firstBitOfData);

        byte[] secondNum = Arrays.copyOfRange(data, end, end + 8);

        long numberOfRecipients = ByteBuffer.wrap(secondNum).getLong();

        System.out.println(numberOfRecipients);

        byte[] arrayElementCountData = Arrays.copyOfRange(data, end + 8, end + 16);
        long arrayElementCount = ByteBuffer.wrap(arrayElementCountData).getLong();

        byte[] recipientKeyLengthData = Arrays.copyOfRange(data, end + 16, end + 24);
        long recipientKeyLength = ByteBuffer.wrap(recipientKeyLengthData).getLong();
        System.out.println(recipientKeyLength);

        end = end + 7 + 24 + (int) recipientKeyLength;

        byte[] recipientKeyData = Arrays.copyOfRange(data, end, end + (int) recipientKeyLength);
        System.out.println(new String(recipientKeyData));

        return new PartyInfoThing(url, null);
    }

    public static class Recipient {

        private final String url;

        public Recipient(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
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
