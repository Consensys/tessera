package com.quorum.tessera.config.migration;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;

public class Main {

    public static void main(String... args) {
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty(CliType.CLI_TYPE_KEY, CliType.CONFIG_MIGRATION.name());
        try {
            final CliResult result = CliDelegate.instance().execute(args);
            System.exit(result.getStatus());
        } catch (final Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }
    }
}
