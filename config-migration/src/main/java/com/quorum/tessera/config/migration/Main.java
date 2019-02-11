package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.cli.CliResult;

public class Main {

    public static void main(String... args) {
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        LegacyCliAdapter adapter = new LegacyCliAdapter();
        try {
            CliResult result = adapter.execute(args);
            System.exit(result.getStatus());
        } catch (final Exception ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }
    }
}
