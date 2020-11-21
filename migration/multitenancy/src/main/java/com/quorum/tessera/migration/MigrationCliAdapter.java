package com.quorum.tessera.migration;

import com.quorum.tessera.cli.CliAdapter;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        headerHeading = "Usage:%n%n",
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        header = "Migrate one database into another")
public class MigrationCliAdapter implements CliAdapter, Callable<CliResult> {

    @CommandLine.Option(names = "--primary", description = "path to primary node configuration file", required = true)
    public Config configPrimary;

    @CommandLine.Option(
            names = "--secondary",
            description = "path to secondary node configuration file",
            required = true)
    public Config configSecondary;

    @Override
    public CliType getType() {
        return CliType.MULTITENANCY_MIGRATION;
    }

    @Override
    public CliResult execute(String... args) {
        EntityManagerDAOFactory primaryFactory = EntityManagerDAOFactory.newFactory(configPrimary);
        EntityManagerDAOFactory secondaryFactory = EntityManagerDAOFactory.newFactory(configSecondary);

        new MigrationRunner(primaryFactory, secondaryFactory).run();

        return new CliResult(0, true, null);
    }

    @Override
    public CliResult call() {
        return this.execute();
    }
}
