package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import suite.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
@TestSuite.SuiteClasses(PeerToPeerIT.class)
@Parameterized.UseParametersRunnerFactory(ParameterizedTestSuiteRunnerFactory.class)
public class P2pTestSuite {

    @Parameterized.Parameters
    public static List<ProcessConfiguration> configurations() {
        return Stream.of(DBType.values())
                .map(
                        value ->
                                new ProcessConfiguration(
                                        value,
                                        CommunicationType.REST,
                                        SocketType.HTTP,
                                        EnclaveType.LOCAL,
                                        false,
                                        "p2p",
                                        false))
                .collect(Collectors.toList());
    }
}
