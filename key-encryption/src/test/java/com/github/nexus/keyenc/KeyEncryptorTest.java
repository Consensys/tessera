package com.github.nexus.keyenc;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.argon2.ArgonOptions;
import com.github.nexus.argon2.ArgonResult;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.NaclFacade;
import com.github.nexus.nacl.Nonce;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyEncryptorTest {

    private Argon2 argon2;

    private NaclFacade nacl;

    private KeyEncryptor keyEncryptor;

    @Before
    public void init() {

        this.argon2 = mock(Argon2.class);
        this.nacl = mock(NaclFacade.class);

        this.keyEncryptor = new KeyEncryptorImpl(argon2, nacl);

    }

    @Test
    public void encryptingKeyReturnsCorrectJson() {

        final Key key = new Key(new byte[]{1, 2, 3, 4, 5});
        final String password = "pass";
        final ArgonResult result = new ArgonResult(new ArgonOptions("i", 1, 1, 1), new byte[]{}, new byte[]{});

        Mockito.doReturn(result).when(argon2).hash(eq(password), any(byte[].class));
        Mockito.doReturn(new Nonce(new byte[]{})).when(nacl).randomNonce();
        doReturn(new byte[]{}).when(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        final JsonObject returnJson = keyEncryptor.encryptPrivateKey(key, password);

        final JsonObject aopts = returnJson.getJsonObject("aopts");

        Assertions.assertThat(returnJson.getString("sbox")).isNotNull();
        Assertions.assertThat(returnJson.getString("asalt")).isNotNull();
        Assertions.assertThat(returnJson.getString("snonce")).isNotNull();
        Assertions.assertThat(aopts).isNotNull();
        Assertions.assertThat(aopts.getInt("memory")).isNotNull();
        Assertions.assertThat(aopts.getInt("parallelism")).isNotNull();
        Assertions.assertThat(aopts.getInt("iterations")).isNotNull();
        Assertions.assertThat(aopts.getString("variant")).isNotNull();

        verify(argon2).hash(eq(password), any(byte[].class));
        verify(nacl).randomNonce();
        verify(nacl).sealAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

    }

    @Test
    public void nullKeyGivesError() {

        final Throwable throwable = catchThrowable(() -> keyEncryptor.encryptPrivateKey(null, ""));

        assertThat(throwable).isInstanceOf(NullPointerException.class);

    }

    @Test
    public void correntJsonGivesDecryptedKey() {

        final String password = "pass";
        final String json = "{\"snonce\":\"\",\"sbox\":\"\",\"asalt\":\"uZAfjmMwEepP8kzZCnmH6g==\",\"aopts\":{\"variant\":\"i\",\"iterations\":1,\"memory\":1,\"parallelism\":1}}";

        final JsonReader reader = Json.createReader(new StringReader(json));
        final JsonObject parsed = reader.readObject();

        doReturn(new byte[]{1, 2, 3})
            .when(nacl)
            .openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));

        Mockito.doReturn(new ArgonResult(null, new byte[]{}, new byte[]{4, 5, 6}))
            .when(argon2)
            .hash(any(ArgonOptions.class), eq(password), any(byte[].class));

        final Key key = keyEncryptor.decryptPrivateKey(parsed, password);

        Assertions.assertThat(key.getKeyBytes()).isEqualTo(new byte[]{1, 2, 3});

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(Key.class));
        verify(argon2).hash(any(ArgonOptions.class), eq(password), any(byte[].class));

    }

}
