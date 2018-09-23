package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeyConfigurationAdapter extends XmlAdapter<KeyConfiguration, KeyConfiguration> {

    @Override
    public KeyConfiguration unmarshal(final KeyConfiguration input) {

        final List<String> allPasswords = new ArrayList<>();
        if (input.getPasswords() != null) {
            allPasswords.addAll(input.getPasswords());
        } else if (input.getPasswordFile() != null) {
            try {
                allPasswords.addAll(Files.readAllLines(input.getPasswordFile(), StandardCharsets.UTF_8));
            } catch (final IOException ex) {
                //dont do anything, if any keys are locked validation will complain that
                //locked keys were provided without passwords
                System.err.println("Could not read the password file");
            }
        }

        final List<ConfigKeyPair> keyDataWithPasswords;
        if (allPasswords.isEmpty()) {
            keyDataWithPasswords = input.getKeyData();
        } else {
            keyDataWithPasswords = IntStream
                .range(0, input.getKeyData().size())
                .mapToObj(i -> {

                    final ConfigKeyPair kd = input.getKeyData().get(i);
                    kd.withPassword(allPasswords.get(i));

                    return kd;
                }).collect(Collectors.toList());
        }

        return new KeyConfiguration(input.getPasswordFile(), input.getPasswords(), keyDataWithPasswords);
    }

    @Override
    public KeyConfiguration marshal(final KeyConfiguration input) {
        return input;
    }

}
