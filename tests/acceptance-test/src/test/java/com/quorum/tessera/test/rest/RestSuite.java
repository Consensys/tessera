package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.CucumberRawIT;
import com.quorum.tessera.test.CucumberRestIT;
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
import java.util.Arrays;
import static suite.SocketType.HTTP;

@TestSuite.SuiteClasses({
    MultipleKeyNodeIT.class,
    DeleteIT.class,
    PushIT.class,
    ReceiveIT.class,
    ReceiveRawIT.class,
    ResendAllIT.class,
    ResendIndividualIT.class,
    SendIT.class,
    SendRawIT.class,
    P2PRestAppIT.class,
    TransactionForwardingIT.class,
    CucumberRestIT.class,
    CucumberRawIT.class
})
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(ParameterizedTestSuiteRunnerFactory.class)
public class RestSuite {

    @Parameterized.Parameters
    public static List<ProcessConfiguration> configurations() {
        final List<ProcessConfiguration> configurations = new ArrayList<>();

        for (final DBType database : Arrays.asList(DBType.H2)) {

            for (final EnclaveType enclaveType : EnclaveType.values()) {
                configurations.add(new ProcessConfiguration(database, REST, HTTP, enclaveType, false, ""));
            }
        }

        return configurations;
    }
}
