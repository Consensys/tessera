package com.quorum.tessera.test.vault;

import com.quorum.tessera.test.vault.aws.RunAwsIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    RunAwsIT.class,
 //   RunAzureIT.class, // disabled as Azure tests do not currently work due to challenge mocking the authentication protocol used by new Azure SDK libs
})
public class VaultTestSuite {

}
