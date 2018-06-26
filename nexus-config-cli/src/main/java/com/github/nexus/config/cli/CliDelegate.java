package com.github.nexus.config.cli;

import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
//import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public enum CliDelegate {

    INSTANCE;

    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return config;
    }

    /*
    version
upcheck
send
sendraw
receive
receiveraw
delete
     */
    public Config execute(String... args) throws Exception {

        
        OptionGroup apiCommands = new OptionGroup();

        apiCommands.setRequired(true);

        apiCommands.addOption(Option.builder("upcheck")
                .desc("Check that Nexus is running")
                .numberOfArgs(0)
                .build());

        apiCommands.addOption(Option.builder("version")
                .desc("Get Nexus version")
                .numberOfArgs(0)
                .build());

        apiCommands.addOption(Option.builder("send")
                .desc("Send transaction")
                .numberOfArgs(1)
                .build());

        apiCommands.addOption(Option.builder("send")
                .desc("Send transaction")
                .numberOfArgs(1)
                .build());
                
        Options options = new Options();
       // options.addOptionGroup(helpOrConfigFile);
        

       
        options.addOption(
                Option.builder("configfile")
                        .desc("Confguration file path")
                        .hasArg(true)
                        .numberOfArgs(1)
                        .required()
                        .build());
        
        options.addOptionGroup(apiCommands);
        
        CommandLineParser parser = new DefaultParser();

//        if (parser.parse(new Options()
//                .addOption(Option.builder("help").build()), args)
//                .hasOption("help")) {
//            
//            HelpFormatter formatter = new HelpFormatter();
//            formatter.printHelp("nexus", options, true);
//            System.exit(0);
//        }

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        try {

            CommandLine line = parser.parse(options, args);

            
            
            Path path = Paths.get(line.getOptionValue("configfile"));

            if (!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }

            try (InputStream in = Files.newInputStream(path)) {
                this.config = ConfigFactory.create().create(in);
            }

            Set<ConstraintViolation<Config>> violations = validator.validate(config);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return config;

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

}
