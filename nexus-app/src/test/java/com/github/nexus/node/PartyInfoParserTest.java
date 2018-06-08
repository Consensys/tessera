package com.github.nexus.node;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyInfoParserTest {

    private final int[] sampleData = new int[] {
        
        0, 0, 0, 0, 0, 0, 0, 21, //URL index

        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 48, //URL data 8-21

        0, 0, 0, 0, 0, 0, 0, 1, //Number of recipients 

        0, 0, 0, 0, 0, 0, 0, 2,//Number of elements per recipient

        0, 0, 0, 0, 0, 0, 0, 32, //recipient Key length

        216, 17, 154, 12, 190, 199, 22, 18, 28, 2, 208, 62, 196, 51, 102, 28, 204, 27, 44, 163, 139, 255, 186, 192, 111, 73, 209, 61, 101, 17, 101, 32, //Recipient key

        0, 0, 0, 0, 0, 0, 0, 21, //recipient value length/ URL 

        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 49,
        
        0, 0, 0, 0, 0, 0, 0, 1, //Number of parties

        0, 0, 0, 0, 0, 0, 0, 21, //Length of party url

        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 49, //party URL data

        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private byte[] data;

    private PartyInfoParser partyInfoParser = PartyInfoParser.create();

    public PartyInfoParserTest() {
    }

    @Before
    public void setUp() {
        List<Byte> bbdata = IntStream.of(sampleData).boxed()
                .map(n -> n.byteValue())
                .collect(Collectors.toList());

        data = new byte[bbdata.size()];

        for (int i = 0; i < bbdata.size(); i++) {
            data[i] = bbdata.get(i);
        }

    }

    @After
    public void tearDown() {
    }

    @Test
    public void from() {

        PartyInfo result = partyInfoParser.from(data);

        assertThat(result).isNotNull();

        assertThat(result.getUrl()).isEqualTo("http://localhost:8000");
        assertThat(result.getRecipients()).hasSize(1);
        assertThat(result.getRecipients().get(0).getUrl()).isEqualTo("http://localhost:8001");
        assertThat(result.getRecipients().get(0).getKey()).isNotNull();
        assertThat(result.getParties()).hasSize(1);
        assertThat(result.getParties().get(0).getUrl()).isEqualTo("http://localhost:8001");

    }
    
    
    @Test
    public void toUsingSameInfoFromFixture() {
        
        final PartyInfo partyInfo = partyInfoParser.from(data);
        final byte[] result = partyInfoParser.to(partyInfo);
        
        final ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        
        assertThat(result).isNotEmpty();
        assertThat(byteBuffer.getLong()).isEqualTo(21L);

        byte[] urlData = new byte[21];
        byteBuffer.get(urlData,0,21);
        
        final String url = new String(urlData);
        assertThat(url).isEqualTo(partyInfo.getUrl());
        
        long numberOfRecipients = byteBuffer.getLong();
        assertThat(numberOfRecipients).isEqualTo(1L);
        
        long numberOfRecipentElements = byteBuffer.getLong();
        assertThat(numberOfRecipentElements).isEqualTo(2L);

        long keyByteLength = byteBuffer.getLong();
        assertThat(keyByteLength).isEqualTo(32L);

        final byte[] keyData = new byte[32];
        byteBuffer.get(keyData, 0, 32);

        assertThat(keyData)
                .hasSize(32)
                .isEqualTo(partyInfo.getRecipients().get(0).getKey().getKeyBytes());
        
       long recipientUrlLength = byteBuffer.getLong();
       
       assertThat(recipientUrlLength).isEqualTo(21L);
       
       byte[] recipientUrlData = new byte[21];
       
       byteBuffer.get(recipientUrlData);
       
       String recipientUrl = new String(recipientUrlData);
       
       assertThat(recipientUrl).isEqualTo(partyInfo.getRecipients().get(0).getUrl());

       
       long partyCount = byteBuffer.getLong();
       
       assertThat(partyCount).isEqualTo(1L);
       
       
       long partyUrlLength = byteBuffer.getLong();
       assertThat(partyUrlLength).isEqualTo(21L);
       
       byte[] partyUrlData = new byte[21];
       byteBuffer.get(partyUrlData);
       
       String partyUrl = new String(partyUrlData);
       
       assertThat(partyUrl).isEqualTo(partyInfo.getParties().get(0).getUrl());
               
               
       
       
       
    }


}
