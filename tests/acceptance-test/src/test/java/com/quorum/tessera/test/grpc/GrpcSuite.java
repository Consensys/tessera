package com.quorum.tessera.test.grpc;

import com.quorum.tessera.test.CucumberGprcIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import suite.EnclaveType;
import suite.ParameterizedTestSuiteRunnerFactory;
import suite.ProcessConfiguration;
import suite.TestSuite;

import java.util.ArrayList;
import java.util.List;

import static com.quorum.tessera.config.CommunicationType.GRPC;
import java.util.stream.Stream;
import static suite.SocketType.HTTP;

@TestSuite.SuiteClasses({SendGrpcIT.class, PartyInfoGrpcIT.class, TesseraGrpcIT.class, CucumberGprcIT.class})
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(ParameterizedTestSuiteRunnerFactory.class)
public class GrpcSuite {

    @Parameterized.Parameters
    public static List<ProcessConfiguration> configurations() {
        final List<ProcessConfiguration> configurations = new ArrayList<>();

        Stream.of(DBType.values())
                .forEach(
                        database -> {
                            configurations.add(
                                    new ProcessConfiguration(database, GRPC, HTTP, EnclaveType.LOCAL, false, ""));
                        });

        return configurations;
    }
}
