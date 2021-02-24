package net.consensys.tessera.migration;

import picocli.CommandLine;

public class Main {

    public static void main(String... args) throws Exception {

        MigrateCommand migrateCommand = new MigrateCommand();

        CommandLine commandLine = new CommandLine(migrateCommand)
            .setCaseInsensitiveEnumValuesAllowed(true);

        commandLine.registerConverter(OrionKeyHelper.class, new OrionKeyHelperConvertor());

        int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
