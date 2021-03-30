package net.consensys.tessera.migration;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import net.consensys.tessera.migration.config.MigrateConfigCommand;
import net.consensys.tessera.migration.data.*;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class MigrateCommand implements Callable<Config> {

    @CommandLine.Option(
            names = {"-f", "orionfile", "orionconfig"},
            required = true,
            description = "Orion config file",paramLabel = "Orion config file")
    private OrionKeyHelper orionKeyHelper;

    @CommandLine.Option(
            names = {"-o", "outputfile"},
            required = true,
            description = "Output Tessera config file")
    private Path outputFile;

    @CommandLine.Option(names = {"-sv", "skipValidation"})
    private boolean skipValidation;

    @CommandLine.Mixin
    private TesseraJdbcOptions tesseraJdbcOptions = new TesseraJdbcOptions();

    @Override
    public Config call() throws Exception {

        MigrateConfigCommand migrateConfigCommand =
                new MigrateConfigCommand(
                        orionKeyHelper.getFilePath(), outputFile, skipValidation, tesseraJdbcOptions);
        Config config = migrateConfigCommand.call();

        System.out.println("Generated tessera config");
        JaxbUtil.marshalWithNoValidation(config,System.out);

        net.consensys.orion.config.Config orionConfig = orionKeyHelper.getOrionConfig();
        //TODO: add any other orion config validations
        Objects.requireNonNull(orionConfig.storage(),"Storage config is required. Not found in toml or env");

        InboundDbHelper inboundDbHelper = InboundDbHelper.from(orionConfig);

        MigrationInfo migrationInfo = MigrationInfoFactory.create(inboundDbHelper);
        System.out.println("Found "+ migrationInfo + " to migrate.");

        if(migrationInfo.getRowCount() == 0) {
            throw new IllegalStateException(String.format("No data found for %s. Check orion storage config string and/or storage env",inboundDbHelper.getStorageInfo()));
        }

        MigrateDataCommand migrateDataCommand =
                new MigrateDataCommand(inboundDbHelper, tesseraJdbcOptions, orionKeyHelper);

        Map<PayloadType,Long> outcome = migrateDataCommand.call();

        System.out.println("=== Migration report ===");
        System.out.printf("Migrated %s of %s transactions",outcome.get(PayloadType.ENCRYPTED_PAYLOAD),migrationInfo.getTransactionCount());
        System.out.println();
        System.out.printf("Migrated %s of %s privacy groups",outcome.get(PayloadType.PRIVACY_GROUP_PAYLOAD),migrationInfo.getPrivacyGroupCount());
        System.out.println();

        assert outcome.get(PayloadType.ENCRYPTED_PAYLOAD) == migrationInfo.getTransactionCount();
        assert outcome.get(PayloadType.PRIVACY_GROUP_PAYLOAD) == migrationInfo.getPrivacyGroupCount();


        return config;
    }
}
