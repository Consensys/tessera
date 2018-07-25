package com.github.tessera.config.builder;

import com.github.tessera.config.KeyData;
import com.github.tessera.config.KeyDataConfig;
import com.github.tessera.config.util.JaxbUtil;
import com.github.tessera.io.FilesDelegate;
import com.github.tessera.io.IOCallback;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

public class KeyDataBuilder {

 
    
    private KeyDataBuilder() {
    }

    public static KeyDataBuilder create() {
        return new KeyDataBuilder();
    }

    private List<String> publicKeys = Collections.emptyList();

    private List<String> privateKeys = Collections.emptyList();

    private List<String> privateKeyPasswords = Collections.emptyList();

    private Path privateKeyPasswordFile;
    
    public KeyDataBuilder withPublicKeys(List<String> publicKeys) {
        this.publicKeys = publicKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeys(List<String> privateKeys) {
        this.privateKeys = privateKeys;
        return this;
    }

    public KeyDataBuilder withPrivateKeyPasswords(List<String> privateKeyPasswords) {
        this.privateKeyPasswords = privateKeyPasswords;
        return this;
    }

    public KeyDataBuilder withPrivateKeyPasswordFile(Path privateKeyPasswordFile) {
        this.privateKeyPasswordFile = privateKeyPasswordFile;
        return this;
    }
    
    public List<KeyData> build() {

        List<JsonObject> priavteKeyJson = privateKeys.stream()
                .map(s  -> Paths.get(s))
                .map(path -> IOCallback.execute(() -> Files.newInputStream(path)))
                .map(is -> Json.createReader(is))
                .map(JsonReader::readObject)
                .collect(Collectors.toList());
        
        final List<String> passwords;
        if(Objects.nonNull(privateKeyPasswordFile)) {
            passwords = FilesDelegate.create().lines(privateKeyPasswordFile).collect(Collectors.toList());
        } else {
            passwords = privateKeyPasswords;
        }
        
        
        List<KeyDataConfig> keyDataConfigs = IntStream.range(0, priavteKeyJson.size())
         
                .mapToObj(i -> {

                    final String password = passwords.get(i);
                    final JsonObject keyDatC = Json.createObjectBuilder(priavteKeyJson.get(i)).build();

                    boolean isLocked = Objects.equals(keyDatC.getString("type"), "argon2sbox");

                    final JsonObject dataNode = keyDatC.getJsonObject("data");
                    final JsonObjectBuilder ammendedDataNode = Json.createObjectBuilder(dataNode);

                    if (isLocked) {
                        ammendedDataNode.add("password", Objects.requireNonNull(password, "Password is required."));
                    }

                    return Json.createObjectBuilder(keyDatC)
                            .remove("data")
                            .add("data", ammendedDataNode)
                            .build();
                })
                .map(JsonObject::toString)
                .map(String::getBytes)
                .map(ByteArrayInputStream::new)
                .map(inputStream -> JaxbUtil.unmarshal(inputStream, KeyDataConfig.class))
                .collect(Collectors.toList());

        return IntStream.range(0, keyDataConfigs.size())
                .mapToObj(i -> new KeyData(keyDataConfigs.get(i), privateKeys.get(i), publicKeys.get(i)))
                .collect(Collectors.toList());
    }



}
