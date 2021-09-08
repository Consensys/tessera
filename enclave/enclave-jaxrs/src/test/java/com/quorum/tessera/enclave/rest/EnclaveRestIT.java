package com.quorum.tessera.enclave.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.server.EnclaveCliAdapter;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import java.net.URL;
import java.util.Set;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

public class EnclaveRestIT {

  private Enclave enclave;

  private JerseyTest jersey;

  private RestfulEnclaveClient enclaveClient;

  static {
    System.setProperty(
        "jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    System.setProperty(
        "jakarta.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
  }

  @Before
  public void setUp() throws Exception {
    System.setProperty(CliType.CLI_TYPE_KEY, CliType.ENCLAVE.name());
    URL url = EnclaveRestIT.class.getResource("/sample-config.json");

    final CommandLine commandLine = new CommandLine(new EnclaveCliAdapter());
    commandLine
        .registerConverter(Config.class, new ConfigConverter())
        .setSeparator(" ")
        .setCaseInsensitiveEnumValuesAllowed(true);

    commandLine.execute("-configfile", url.getFile());
    CliResult cliResult = commandLine.getExecutionResult();

    Config config = cliResult.getConfig().get();
    ConfigFactory.create().store(config);
    this.enclave = Enclave.create();

    jersey = Util.create(enclave);
    jersey.setUp();

    enclaveClient = new RestfulEnclaveClient(jersey.client(), jersey.target().getUri());
  }

  @After
  public void tearDown() throws Exception {
    jersey.tearDown();
  }

  @Test
  public void defaultPublicKey() {
    PublicKey result = enclaveClient.defaultPublicKey();

    assertThat(result).isNotNull();
    assertThat(result.encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
  }

  @Test
  public void forwardingKeys() {
    Set<PublicKey> result = enclaveClient.getForwardingKeys();

    assertThat(result).isEmpty();
  }

  @Test
  public void getPublicKeys() {
    Set<PublicKey> result = enclaveClient.getPublicKeys();

    assertThat(result).hasSize(1);

    assertThat(result.iterator().next().encodeToBase64())
        .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
  }

  @Test
  public void status() {
    assertThat(enclaveClient.status()).isEqualTo(Service.Status.STARTED);
  }
}
