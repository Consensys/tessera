package net.consensys.tessera.migration;

import com.quorum.tessera.config.Config;
import net.consensys.tessera.migration.config.MigrateConfigCommand;
import net.consensys.tessera.migration.data.InboundDbHelper;
import net.consensys.tessera.migration.data.MigrateDataCommand;
import net.consensys.tessera.migration.data.TesseraJdbcOptions;
import picocli.CommandLine;

import java.nio.file.Path;
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

    @CommandLine.Mixin private TesseraJdbcOptions tesseraJdbcOptions = new TesseraJdbcOptions();

    @Override
    public Config call() throws Exception {

        MigrateConfigCommand migrateConfigCommand =
                new MigrateConfigCommand(
                        orionKeyHelper.getFilePath(), outputFile, skipValidation, verbose, tesseraJdbcOptions);
        Config config = migrateConfigCommand.call();

        InboundDbHelper inboundDbHelper = InboundDbHelper.from(orionKeyHelper.getConfig());

        MigrateDataCommand migrateDataCommand =
                new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

        boolean outcome = migrateDataCommand.call();

        return config;
    }
}
