package net.consensys.tessera.migration;

import picocli.CommandLine;

public class Main {

    public static void main(String... args) throws Exception {

        MigrateCommand migrateCommand = new MigrateCommand();

        CommandLine commandLine = new CommandLine(migrateCommand)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setStopAtUnmatched(false)
            .setCommandName("orion-to-tessera/bin/migrate")
            .setParameterExceptionHandler((ex, strings) -> {
                System.out.println(ex.getMessage());
                return 1;
            }).setUnmatchedArgumentsAllowed(true);

        commandLine.registerConverter(OrionKeyHelper.class, new OrionKeyHelperConvertor());

        int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
