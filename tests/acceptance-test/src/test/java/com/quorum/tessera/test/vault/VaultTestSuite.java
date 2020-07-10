package com.quorum.tessera.test.vault;

import com.quorum.tessera.test.vault.aws.RunAwsIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    RunAwsIT.class,
 //   RunAzureIT.class,
})
public class VaultTestSuite {

}
