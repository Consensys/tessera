package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.passwords.PasswordReader;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import picocli.CommandLine;

public class KeyUpdateCommandFactory implements CommandLine.IFactory {

  @Override
  public <K> K create(Class<K> cls) throws Exception {
    try {
      if (cls != KeyUpdateCommand.class) {
        throw new RuntimeException(
            this.getClass().getSimpleName()
                + " cannot create instance of type "
                + cls.getSimpleName());
      }

      KeyEncryptorFactory keyEncryptorFactory = KeyEncryptorFactory.newFactory();
      PasswordReader passwordReader = PasswordReaderFactory.create();

      return (K) new KeyUpdateCommand(keyEncryptorFactory, passwordReader);
    } catch (Exception e) {
      return CommandLine.defaultFactory().create(cls); // fallback if missing
    }
  }
}
