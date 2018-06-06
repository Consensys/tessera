package com.github.nexus;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PartyInfoThingTest {

    int[] nums = new int[] {
        
        0, 0, 0, 0, 0, 0, 0, 21, //URL index
        
        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 48, //URL data 8-21
        
        0, 0, 0, 0, 0, 0, 0, 1, //Number of recipients 29
        
        0, 0, 0, 0, 0, 0, 0, 2,//Number of elements per recipient
        
        0, 0, 0, 0, 0, 0, 0, 32, //recipient Key length
        
        216, 17, 154, 12, 190, 199, 22, 18, 28, 2, 208, 62, 196, 51, 102, 28, 204, 27, 44, 163, 139, 255, 186, 192, 111, 73, 209, 61, 101, 17, 101, 32, //Recipient key
        
        0, 0, 0, 0, 0, 0, 0, 21, //recipient value length/ URL 
        
        104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 49, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 21, 104, 116, 116, 112, 58, 47, 47, 108, 111, 99, 97, 108, 104, 111, 115, 116, 58, 56, 48, 48, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    byte[] data;

    public PartyInfoThingTest() {
    }

    @Before
    public void setUp() {
        List<Byte> bbdata = IntStream.of(nums).boxed()
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
    public void hello() {
        
      PartyInfoThing result =   PartyInfoThing.from(data);
      
      assertThat(result).isNotNull();
      
      assertThat(result.getUrl()).isEqualTo("http://localhost:8000");
      assertThat(result.getRecipient()).isNotNull();
      assertThat(result.getParties()).hasSize(1);

    }
}
