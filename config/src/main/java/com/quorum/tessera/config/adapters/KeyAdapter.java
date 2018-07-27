package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.IOCallback;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyAdapter extends XmlAdapter<KeyConfiguration, KeyConfiguration> {

    private final KeyDataAdapter keyDataAdapter = new KeyDataAdapter();

    @Override
    public KeyConfiguration unmarshal(final KeyConfiguration input) {

        if ((input.getPasswordFile() != null) && (input.getPasswords() != null)) {
            throw new ConfigException(new RuntimeException("Must specify passwords in file or in config, not both"));
        }

        final List<String> allPasswords;
        if (input.getPasswords() != null) {
            allPasswords = input.getPasswords();
        } else if (input.getPasswordFile() != null) {
            allPasswords = IOCallback.execute(() -> Files.readAllLines(input.getPasswordFile(), UTF_8));
        } else {
            allPasswords = Collections.emptyList();
        }


        final List<KeyData> keyDataWithPasswords;
        if (allPasswords.isEmpty()) {
            keyDataWithPasswords = input.getKeyData();
        } else {
            keyDataWithPasswords = IntStream
                .range(0, input.getKeyData().size())
                .mapToObj(i -> {

                    final KeyData kd = input.getKeyData().get(i);

                    return new KeyData(
                        new KeyDataConfig(
                            new PrivateKeyData(
                                kd.getConfig().getValue(),
                                kd.getConfig().getSnonce(),
                                kd.getConfig().getAsalt(),
                                kd.getConfig().getSbox(),
                                kd.getConfig().getArgonOptions(),
                                allPasswords.get(i)
                            ),
                            kd.getConfig().getType()
                        ),
                        kd.getPrivateKey(),
                        kd.getPublicKey(),
                        kd.getPrivateKeyPath(),
                        kd.getPublicKeyPath()
                    );
                }).collect(Collectors.toList());
        }

        return new KeyConfiguration(
            input.getPasswordFile(),
            input.getPasswords(),
            keyDataWithPasswords
                .stream()
                .map(keyDataAdapter::unmarshal)
                .collect(Collectors.toList())
        );

    }

    @Override
    public KeyConfiguration marshal(final KeyConfiguration input) {
        return new KeyConfiguration(
            input.getPasswordFile(),
            input.getPasswords(),
            input.getKeyData()
                .stream()
                .map(keyDataAdapter::marshal)
                .collect(Collectors.toList())
        );
    }

}
