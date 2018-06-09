package com.github.nexus.node;


import com.github.nexus.nacl.Key;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;

public interface PartyInfoParser {

    default PartyInfo from(byte[] encoded) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(encoded);

        final int urlLength = (int) byteBuffer.getLong();

        final byte[] urlBytes = new byte[urlLength];
        byteBuffer.get(urlBytes);
        final String url = new String(urlBytes);

        final int numberOfRecipients = (int) byteBuffer.getLong();
        final int recipientElementCount = (int) byteBuffer.getLong();

        final List<Recipient> recipients = new ArrayList<>();

        for (int i = 0; i < numberOfRecipients; i++) {
            
            final int recipientKeyLength = (int) byteBuffer.getLong();
            final byte[] recipientKeyBytes = new byte[recipientKeyLength];
            byteBuffer.get(recipientKeyBytes);

            final int recipientUrlValueLength = (int) byteBuffer.getLong();
            final byte[] urlValueData = new byte[recipientUrlValueLength];
            byteBuffer.get(urlValueData);
            final String recipientUrl = new String(urlValueData);

            recipients.add(new Recipient(new Key(recipientKeyBytes), recipientUrl));

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

        return new PartyInfo(url, recipients, Arrays.asList(parties));
    };


   default byte[] to(PartyInfo partyInfo) {

       int urlLength = partyInfo.getUrl().length();
       
       ByteBuffer byteBuffer = ByteBuffer.allocate(256);
       byteBuffer.putLong(urlLength);
       byteBuffer.put(partyInfo.getUrl().getBytes(StandardCharsets.UTF_8));
       byteBuffer.putLong(partyInfo.getRecipients().size());
       byteBuffer.putLong(2);//Recipient Element count

       partyInfo.getRecipients().forEach((r) -> {
           byteBuffer.putLong(32L);//recipient key length
           byteBuffer.put(r.getKey().getKeyBytes()); //Recipient Key
           byteBuffer.putLong(r.getUrl().length());//recipient url length.
           byteBuffer.put(r.getUrl().getBytes(StandardCharsets.UTF_8)); 
        });
       
       byteBuffer.putLong(partyInfo.getParties().size());
       partyInfo.getParties().forEach(p -> {
           byteBuffer.putLong(p.getUrl().length());
           byteBuffer.put(p.getUrl().getBytes(StandardCharsets.UTF_8));
       });
       
       
       
       return byteBuffer.array();

    }

    static PartyInfoParser create() {
        return new PartyInfoParser() {
    };
    }

}
