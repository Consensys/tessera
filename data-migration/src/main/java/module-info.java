module tessera.data.migration.main {
    requires java.sql;
 //   requires commons.codec;
    requires info.picocli;
    requires org.apache.commons.io;
    requires org.bouncycastle.provider;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.shared.main;

    provides com.quorum.tessera.cli.CliAdapter with
        com.quorum.tessera.data.migration.CmdLineExecutor;
}
