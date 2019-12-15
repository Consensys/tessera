package com.quorum.tessera.picocli;

import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import picocli.CommandLine;

public class KeyGenCommandFactory implements CommandLine.IFactory {

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            if (cls != KeyGenCommand.class) {
                throw new RuntimeException(this.getClass().getSimpleName() + " cannot create instance of type " + cls.getSimpleName());
            }

             KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.newFactory();

            return (K) new KeyGenCommand(keyGeneratorFactory);
        } catch (Exception e) {
            return CommandLine.defaultFactory().create(cls); // fallback if missing
        }
    }
}
