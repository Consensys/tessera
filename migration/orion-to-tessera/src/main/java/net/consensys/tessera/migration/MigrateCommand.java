package net.consensys.tessera.migration;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.config.MigrateConfigCommand;
import net.consensys.tessera.migration.data.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

public class MigrateCommand implements Callable<Config> {

    @CommandLine.Option(
            names = {"-f", "orionfile", "orionconfig"},
            required = true,
            description = "Orion config file")
    private OrionKeyHelper orionKeyHelper;

    @CommandLine.Option(
            names = {"-o", "outputfile"},
            required = true,
            description = "Output Tessera config file")
    private Path outputFile;

    @CommandLine.Option(names = {"-sv", "skipValidation"})
    private boolean skipValidation;

    @CommandLine.Option(names = {"-v", "--verbose"})
    private boolean verbose;

    @CommandLine.Mixin
    private TesseraJdbcOptions tesseraJdbcOptions = new TesseraJdbcOptions();

    @Override
    public Config call() throws Exception {

        MigrateConfigCommand migrateConfigCommand =
                new MigrateConfigCommand(
                        orionKeyHelper.getFilePath(), outputFile, skipValidation, verbose, tesseraJdbcOptions);
        Config config = migrateConfigCommand.call();

        System.out.println("Generated tessera config");
        JaxbUtil.marshalWithNoValidation(config,System.out);

        net.consensys.orion.config.Config orionConfig = orionKeyHelper.getConfig();
        //TODO: add any other orion config validations
        Objects.requireNonNull(orionConfig.storage(),"Storage config is required. Not found in toml or env");

        InboundDbHelper inboundDbHelper = InboundDbHelper.from(orionConfig);

        MigrationInfo migrationInfo = MigrationInfoFactory.create(inboundDbHelper);
        System.out.println("Found "+ migrationInfo + " to migrate.");

        MigrateDataCommand migrateDataCommand =
                new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

        boolean outcome = migrateDataCommand.call();
        if(outcome) {
            System.out.println("Success");
        } else {
            System.err.println("ERROR");
        }

        return config;
    }
}
