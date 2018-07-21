
package com.github.tessera.config.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class LegacyCliAdapter implements CliAdapter {

    @Override
    public CliResult execute(String... args) throws Exception {
        
        
        Options options = new Options();
        options.addOption(
                Option.builder()
                        .longOpt("url")
                        .desc("URL for this node (what's advertised to other nodes, e.g. https://constellation.mydomain.com/)")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("URL")
                        .build());
        
        options.addOption(
                Option.builder()
                        .longOpt("port")
                        .desc("Port to listen on for the public API")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("NUM")
                        .valueSeparator('=')
                        .build());
        
        options.addOption(
                Option.builder()
                        .longOpt("workdir")
                        .desc("Working directory to use (relative paths specified for other options are relative to the working directory)")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("DIR")
                        .build());
        
        options.addOption(
                Option.builder()
                        .longOpt("socket")
                        .desc("Path to IPC socket file to create for private API access")
                        .hasArg(true)
                        .optionalArg(false)
                        .numberOfArgs(1)
                        .argName("FILE")
                        .build());
        
        options.addOption(
                Option.builder()
                        .longOpt("othernodes")
                        .desc("Comma-separated list of other node URLs to connect to on startup (this list may be incomplete)")
                        .hasArg(true)
                        .optionalArg(false)
                        .hasArgs()
                        .argName("URL...")
                        .valueSeparator(',')
                        .build());
        
        options.addOption(
                Option.builder()
                        .longOpt("publickeys")
                        .desc("Comma-separated list of paths to public keys to advertise")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("privatekeys")
                        .desc("Comma-separated list of paths to corresponding private keys (these must be given in the same order as --publickeys)")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("alwayssendto")
                        .desc("Comma-separated list of paths to public keys that are always included as recipients (these must be advertised somewhere)")
                        .optionalArg(false)
                        .argName("FILE...")
                        .hasArgs()
                        .valueSeparator(',')
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("passwords")
                        .desc("A file containing the passwords for the specified --privatekeys, one per line, in the same order (if one key is not locked, add an empty line)")
                        .optionalArg(false)
                        .argName("FILE")
                        .hasArg()
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("storage")
                        .build()
        );

        options.addOption(
                Option.builder()
                        .longOpt("ipwhitelist")
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("tls")
                        .desc("TLS status (strict, off)")
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("tlsservercert")
                        .desc("TLS certificate file to use for the public API")
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("tlsserverchain")
                        .desc("Comma separated list of TLS chain certificates to use for the public API")
                        .hasArgs()
                        .build()
        );

        /*
                tlsservertrust
         */
        //tlsserverkey
        options.addOption(
                Option.builder()
                        .longOpt("tlsserverkey")
                        .desc("TLS key to use for the public API")
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("tlsservertrust")
                        .desc("TLS server trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
                        .build()
        );

        //           --tlsknownclients[=FILE]    TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownclients")
                        .desc("TLS server known clients file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .build()
        );

        //           --tlsclientcert[=FILE]      TLS client certificate file to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientcert")
                        .desc("TLS client certificate file to use for connections to other nodes")
                        .argName("FILE")
                        .build()
        );

        //           --tlsclientchain[=FILE...]  Comma separated list of TLS chain certificates to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientchain")
                        .desc("Comma separated list of TLS chain certificates to use for connections to other nodes")
                        .argName("FILE...")
                        .hasArgs()
                        .build()
        );

        //           --tlsclientkey[=FILE]       TLS key to use for connections to other nodes
        options.addOption(
                Option.builder()
                        .longOpt("tlsclientkey")
                        .desc("TLS key to use for connections to other nodes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //           --tlsclienttrust[=STRING]   TLS client trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)
        options.addOption(
                Option.builder()
                        .longOpt("tlsclienttrust")
                        .desc("TLS client trust mode (whitelist, ca-or-tofu, ca, tofu, insecure-no-validation)")
                        .argName("STRING")
                        .hasArg()
                        .build()
        );

//           --tlsknownservers[=FILE]    TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes
        options.addOption(
                Option.builder()
                        .longOpt("tlsknownservers")
                        .desc("TLS client known servers file for the ca-or-tofu, tofu and whitelist trust modes")
                        .argName("FILE")
                        .hasArg()
                        .build()
        );

        //  -v[NUM]  --verbosity[=NUM]           Print more detailed information (optionally specify a number or add v's to increase verbosity)
        options.addOption(
                Option.builder("v")
                        .longOpt("verbosity")
                        .desc("Print more detailed information (optionally specify a number or add v's to increase verbosity)")
                        .argName("NUM")
                        .hasArg()
                        .build()
        );

        //  -V, -?   --version                   Output current version information, then exit
        options.addOption(
                Option.builder("V")
                        .longOpt("version")
                        .desc("Output current version information, then exit")
                        .build()
        );
        options.addOption(
                Option.builder("?")
                        .desc("Output current version information, then exit")
                        .build()
        );

//           --generatekeys[=NAME...]    Comma-separated list of key pair names to generate, then exit
        options.addOption(
                Option.builder()
                        .longOpt("generatekeys")
                        .desc("Comma-separated list of key pair names to generate, then exit")
                        .hasArgs()
                        .argName("NAME...")
                        .build()
        );
        
        options.addOption(
                Option.builder()
                        .longOpt("help")
                        .build()
        );

        //privatekeys
        CommandLineParser parser = new DefaultParser();
        
        CommandLine line = parser.parse(options, args);
        
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            
            //(If a configuration file is specified, any command line options will take precedence.)
            formatter.printHelp("tessera [OPTION...] [config file containing options]", options);
            return new CliResult(0, true, null);
        }

        return new CliResult(1,false,null);
    }
    
}
