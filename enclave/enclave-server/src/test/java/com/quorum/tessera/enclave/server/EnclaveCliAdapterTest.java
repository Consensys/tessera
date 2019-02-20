package com.quorum.tessera.enclave.server;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.CliException;
import com.quorum.tessera.config.cli.CliResult;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class EnclaveCliAdapterTest {

    private EnclaveCliAdapter enclaveCliAdapter;

    @Before
    public void onSetUp() {

        enclaveCliAdapter = new EnclaveCliAdapter();
    }

    @Test
    public void noArgs() throws Exception {
        CliResult result = enclaveCliAdapter.execute();
        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);
    }

    @Test
    public void help() throws Exception {

        CliResult result = enclaveCliAdapter.execute("bogus", "help");

        assertThat(result).isNotNull();
        assertThat(result.getConfig()).isNotPresent();
        assertThat(result.getStatus()).isEqualTo(0);

    }

    @Test
    public void withFile() throws Exception {

        URI uri = getClass().getResource("/sample-config.json").toURI();

        Path path = Paths.get(uri);

        CliResult result = enclaveCliAdapter.execute("-configfile", path.toString());

        assertThat(result).isNotNull();

        assertThat(result.getStatus()).isEqualTo(0);

        Config config = result.getConfig().get();

        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey())
                .isEqualTo("yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");

        assertThat(config.getKeys().getKeyData().get(0).getPublicKey())
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

    @Test(expected = CliException.class)
    public void parserThrowsException() throws Exception {

        CommandLineParser parser = mock(CommandLineParser.class);

        doThrow(ParseException.class)
                .when(parser)
                .parse(any(), any());

        EnclaveCliAdapter otherEnclaveCliAdapter = new EnclaveCliAdapter(parser);

        otherEnclaveCliAdapter.execute("-configfile", "somename");

    }
}
