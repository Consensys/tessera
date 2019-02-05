package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class EnclaveRequestCodecTest {
    
    private EnclaveRequestCodec enclaveRequestCodec = new EnclaveRequestCodec();
    
    
    @Test
    public void doEncodeEncyrptPayloadInvocation() throws Exception {
        
        PublicKey publicKey = PublicKey.from("PublicKey".getBytes());
        String expectedPublicKeyString = Base64.getEncoder().encodeToString(publicKey.getKeyBytes());
        
        EnclaveRequest enclaveRequest = EnclaveRequest.Builder.create()
                .withType(EnclaveRequestType.ENCRYPT_PAYLOAD)
                .withArg("ENYCYTPT_THIS".getBytes())
                .withArg(publicKey)
                .withArg(Arrays.asList(publicKey))
                .build();        
        
        
        String result = enclaveRequestCodec.doEncode(enclaveRequest);
        
        JsonObject json = Json.createReader(new StringReader(result)).readObject();
        
        
        assertThat(json.getString("type")).isEqualTo(EnclaveRequestType.ENCRYPT_PAYLOAD.name());
        
        JsonArray argsList = json.getJsonArray("args");
        
        assertThat(argsList).hasSize(3);
        assertThat(argsList.getString(0)).isEqualTo(Base64.getEncoder().encodeToString("ENYCYTPT_THIS".getBytes()));
        assertThat(argsList.getString(1)).isEqualTo(expectedPublicKeyString);
        assertThat(argsList.getJsonArray(2).getString(0)).isEqualTo(expectedPublicKeyString);
        

    }
    
    
}
