//package com.quorum.tessera.keyenc;
//
//import Argon2;
//import Key;
//import NaclFacade;
//import NaclFacadeFactory;
//import org.assertj.core.api.Assertions;
//import org.junit.Before;
//import org.junit.Test;
//
//import javax.json.JsonObject;
//import java.util.Base64;
//
//public class KeyEncryptorIT {
//
//    private static final String pkeyBase64 = "kwMSmHpIhCFFoOCyGkHkJyfLVxsa9Mj3Y23sFYRLwLM=";
//
//    private static final String password = "PASS_WORD";
//
//    private KeyEncryptor keyEncryptor;
//
//    private Key privateKey;
//
//    @Before
//    public void init() {
//
//        final NaclFacade nacl = NaclFacadeFactory.newFactory().create();
//        final Argon2 argon2 = Argon2.create();
//
//        this.privateKey = new Key(Base64.getDecoder().decode(pkeyBase64));
//
//        this.keyEncryptor = new KeyEncryptorImpl(argon2, nacl);
//    }
//
//    @Test
//    public void encryptAndDecryptOnKeyIsSuccessful() {
//
//        final JsonObject jsonObject = keyEncryptor.encryptPrivateKey(privateKey, password);
//
//        final Key decryptedKey = keyEncryptor.decryptPrivateKey(jsonObject, password);
//
//        Assertions.assertThat(decryptedKey).isEqualTo(privateKey);
//
//    }
//
//}
