package admin.cmd;

import static org.assertj.core.api.Assertions.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.util.ElUtil;
import io.cucumber.java8.En;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StartupSteps implements En {

  public StartupSteps() {

    List<Party> partyHolder = new ArrayList<>();

    Given(
        "configuration file with empty public and private key values",
        () -> {
          URL url = getClass().getResource("/empty-keys-config.json");

          Party party = new Party("", url, "X");
          Config config = party.getConfig();
          assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEmpty();
          assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEmpty();
          partyHolder.add(party);
        });

    Given(
        "configuration file with with key paths containing empty values",
        () -> {
          URL url = getClass().getResource("/empty-keyspath-config.json");
          Path emptyKeyFile = Files.createTempFile(UUID.randomUUID().toString(), "");
          Path ipcFile = Files.createTempFile(UUID.randomUUID().toString(), "");
          Map<String, String> params = new HashMap<>();
          params.put("emptyKeyFilePath", emptyKeyFile.toAbsolutePath().toString());
          params.put("unixSocketPath", ipcFile.toAbsolutePath().toString());
          Path configFile = ElUtil.createTempFileFromTemplate(url, params);
          Party party = new Party("", configFile.toUri().toURL(), "X");
          Config config = party.getConfig();
          JaxbUtil.marshalWithNoValidation(config, System.out);
          assertThat(emptyKeyFile).exists();

          partyHolder.add(party);
        });

    List<ExecutionResult> results = new ArrayList<>();
    When(
        "admin user executes start",
        () -> {
          assertThat(partyHolder).hasSize(1);
          Party party = partyHolder.iterator().next();
          ExecutionResult result = Utils.start(party);
          results.add(result);
        });

    Then(
        "node returns error message and exits",
        () -> {
          assertThat(results).hasSize(1);
          ExecutionResult result = results.get(0);
          assertThat(result.getExitCode()).isNotEqualTo(0);

          assertThat(result.getOutput()).hasSize(2);

          assertThat(result.getOutput())
              .anyMatch(m -> m.startsWith("Config validation issue: keys.keyData[0].privateKey"));

          assertThat(result.getOutput())
              .anyMatch(m -> m.startsWith("Config validation issue: keys.keyData[0].publicKey"));
        });
  }
}
