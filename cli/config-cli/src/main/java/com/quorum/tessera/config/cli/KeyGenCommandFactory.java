package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.util.ConfigFileUpdaterWriter;
import com.quorum.tessera.config.util.PasswordFileUpdaterWriter;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.key.generation.KeyGeneratorFactory;
import picocli.CommandLine;

public class KeyGenCommandFactory implements CommandLine.IFactory {

  @Override
  public <K> K create(Class<K> cls) throws Exception {
    try {
      if (cls != KeyGenCommand.class) {
        throw new RuntimeException(
            this.getClass().getSimpleName()
                + " cannot create instance of type "
                + cls.getSimpleName());
      }
      KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.create();
      ConfigFileUpdaterWriter configFileUpdaterWriter =
          new ConfigFileUpdaterWriter(FilesDelegate.create());
      PasswordFileUpdaterWriter passwordFileUpdaterWriter =
          new PasswordFileUpdaterWriter(FilesDelegate.create());
      KeyDataMarshaller keyDataMarshaller = KeyDataMarshaller.create();
      return (K)
          new KeyGenCommand(
              keyGeneratorFactory,
              configFileUpdaterWriter,
              passwordFileUpdaterWriter,
              keyDataMarshaller);
    } catch (Exception e) {
      return CommandLine.defaultFactory().create(cls); // fallback if missing
    }
  }
}
