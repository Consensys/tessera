package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultType;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

@CommandLine.Command(
    name = "keygen",
    aliases = {"-keygen"},
    headerHeading = "Usage:%n%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    header = "Generate Tessera encryption keys",
    abbreviateSynopsis = true,
    subcommands = {CommandLine.HelpCommand.class}
)
public class KeyGenCommand {
    // TODO(cjh) raise CLI usage wording changes as separate change

    @CommandLine.Option(
        names = {"--output", "-filename"},
        description = "Comma-separated list of paths to save generated key files. Can also be used with keyvault. Number of args determines number of key-pairs generated."
    )
    public List<String> output;

    @CommandLine.Option(
        names = {"--encryptionconfig", "-keygenconfig"},
        description = "File containing Argon2 encryption config used to secure the new private key"
    )
    public Path encryptionConfig;

    @CommandLine.Option(
        names = {"--vault.type", "-keygenvaulttype"},
        description = "Specify the key vault provider the generated key is to be saved in.  If not set, the key will be encrypted and stored on the local filesystem"
    )
    //TODO(cjh) get possible enum values to show in the usage
    public KeyVaultType vaultType;

    @CommandLine.Option(
        names = {"--vault.url", "-keygenvaulturl"},
        description = "Base url for key vault"
    )
    public String vaultUrl;

    @CommandLine.Option(
        names = {"--vault.hashicorp.approlepath", "-keygenvaultapprole"},
        description = "AppRole path for Hashicorp Vault authentication (defaults to 'approle')"
    )
    public String hashicorpApprolePath;

    @CommandLine.Option(
        names = {"--vault.hashicorp.secretenginepath", "-keygenvaultsecretengine"},
        description = "Name of already enabled Hashicorp v2 kv secret engine"
    )
    public String hashicorpSecretEnginePath;

    @CommandLine.Option(
        names = {"--vault.hashicorp.tlskeystore", "-keygenvaultkeystore"},
        description = "Path to JKS keystore for TLS Hashicorp Vault communication"
    )
    public Path hashicorpTlsKeystore;

    @CommandLine.Option(
        names = {"--vault.hashicorp.tlstruststore", "-keygenvaulttruststore"},
        description = "Path to JKS truststore for TLS Hashicorp Vault communication"
    )
    public Path hashicorpTlsTruststore;

}
