package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.*;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.config.util.JaxbUtil;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyConfigurationAdapter extends XmlAdapter<KeyConfiguration, KeyConfiguration> {

    private final KeyDataAdapter keyDataAdapter = new KeyDataAdapter();

    private FilesDelegate filesDelegate = FilesDelegate.create();

    @Override
    public KeyConfiguration unmarshal(final KeyConfiguration input) {

        final List<String> allPasswords = new ArrayList<>();
        if (input.getPasswords() != null) {
            allPasswords.addAll(input.getPasswords());
        } else if (input.getPasswordFile() != null) {
            try {
                allPasswords.addAll(Files.readAllLines(input.getPasswordFile(), UTF_8));
            } catch (final IOException ex) {
                //dont do anything, if any keys are locked validation will complain that
                //locked keys were provided without passwords
                System.err.println("Could not read the password file");
            }
        }

        final List<KeyData> keyDataWithPasswords;
        if (allPasswords.isEmpty()) {
            keyDataWithPasswords = input.getKeyData();
        } else {
            keyDataWithPasswords = IntStream
                .range(0, input.getKeyData().size())
                .mapToObj(i -> {

                    final KeyData kd = input.getKeyData().get(i);

                    final KeyDataConfig keyDataConfig;

                    if(kd.getConfig() == null && filesDelegate.exists(kd.getPrivateKeyPath())) {
                        final InputStream is = filesDelegate.newInputStream(kd.getPrivateKeyPath());
                        keyDataConfig = JaxbUtil.unmarshal(is, KeyDataConfig.class);
                    } else {
                        keyDataConfig = kd.getConfig();
                    }

                    if(Objects.isNull(keyDataConfig)) {
                        return kd;
                    }

                    return new KeyData(
                        new KeyDataConfig(
                            new PrivateKeyData(
                                keyDataConfig.getValue(),
                                keyDataConfig.getSnonce(),
                                keyDataConfig.getAsalt(),
                                keyDataConfig.getSbox(),
                                keyDataConfig.getArgonOptions(),
                                allPasswords.get(i)
                            ),
                            kd.getConfig()==null ? keyDataConfig.getType() : kd.getConfig().getType()
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

    protected void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }



}
