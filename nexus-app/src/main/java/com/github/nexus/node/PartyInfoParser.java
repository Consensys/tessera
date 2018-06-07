package com.github.nexus.node;

import com.github.nexus.enclave.keys.model.Key;

import java.nio.ByteBuffer;

public interface PartyInfoParser {

    default PartyInfo from(byte[] encoded){

        final ByteBuffer byteBuffer = ByteBuffer.wrap(encoded);

        final int urlLength = (int) byteBuffer.getLong();

        final byte[] urlBytes = new byte[urlLength];
        byteBuffer.get(urlBytes);
        final String url = new String(urlBytes);

        final int numberOfRecipients = (int) byteBuffer.getLong();
        final int recipientElementCount = (int) byteBuffer.getLong();

        final Recipient[] recipients = new Recipient[numberOfRecipients];

        for (int i = 0; i < numberOfRecipients; i++) {
            final int recipientKeyLength = (int) byteBuffer.getLong();
            final byte[] recipientKeyBytes = new byte[recipientKeyLength];
            byteBuffer.get(recipientKeyBytes);

            final int recipientUrlValueLength = (int) byteBuffer.getLong();
            final byte[] urlValueData = new byte[recipientUrlValueLength];
            byteBuffer.get(urlValueData);
            final String recipientUrl = new String(urlValueData);

            recipients[i] = new Recipient(new Key(recipientKeyBytes), recipientUrl);

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

        return new PartyInfo(url,recipients, parties);
    };




    byte[] to(PartyInfo partyInfoThing);

    static PartyInfoParser create(){
        return new PartyInfoParser() {
            @Override
            public byte[] to(PartyInfo partyInfoThing) {
                return new byte[0];
            }
        };
    }


}
