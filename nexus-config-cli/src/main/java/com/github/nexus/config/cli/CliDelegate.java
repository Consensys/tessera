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
import javax.validation.Validation;
import javax.validation.Validator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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
        Options options = new Options();

        options.addOption(Option.builder()
                .longOpt("configfile")
                .desc("Confguration file path")
                .hasArg(true)
                .numberOfArgs(1)
                .required()
                .build());

        OptionGroup apiCommands = new OptionGroup();
        apiCommands.setRequired(true);
        apiCommands.addOption(Option.builder()
                .longOpt("upcheck")
                .desc("Check that Nexus is running")
                .numberOfArgs(0)
                .build());

        apiCommands.addOption(Option.builder()
                .longOpt("version")
                .desc("Get Nexus version")
                .numberOfArgs(0)
                .build());

        apiCommands.addOption(Option.builder()
                .longOpt("send")
                .desc("Send transaction")
                .numberOfArgs(1)
                .build());

        options.addOptionGroup(apiCommands);
        
        CommandLineParser parser = new DefaultParser();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        try {
            CommandLine line = parser.parse(options, args);

            Path path = Paths.get(line.getOptionValue("configfile"));

            if(!Files.exists(path)) {
                throw new FileNotFoundException(String.format("%s not found.", path));
            }
            
            try (InputStream in = Files.newInputStream(path)) {
                this.config = ConfigFactory.create().create(in);
            }

            Set<ConstraintViolation<Config>> violations = validator.validate(config);

            if (!violations.isEmpty()) {
                violations.stream().forEach(System.err::println);
                System.exit(1);
            }

            return config;

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

}
