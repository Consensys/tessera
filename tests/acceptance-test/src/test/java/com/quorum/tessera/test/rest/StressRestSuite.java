package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import suite.EnclaveType;
import suite.ParameterizedTestSuiteRunnerFactory;
import suite.ProcessConfiguration;
import suite.TestSuite;

import java.util.ArrayList;
import java.util.List;

import static com.quorum.tessera.config.CommunicationType.REST;
import com.quorum.tessera.config.EncryptorType;
import static suite.SocketType.HTTP;

@TestSuite.SuiteClasses({StressSendIT.class})
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(ParameterizedTestSuiteRunnerFactory.class)
public class StressRestSuite {

    @Parameterized.Parameters
    public static List<ProcessConfiguration> configurations() {
        final List<ProcessConfiguration> configurations = new ArrayList<>();

        configurations.add(
                new ProcessConfiguration(
                        DBType.H2, REST, HTTP, EnclaveType.LOCAL, false, "", false, EncryptorType.NACL));

        return configurations;
    }
}
