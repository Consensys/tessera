package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.EncryptorType;
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
                .flatMap(
                        value -> {
                            return Stream.of(EncryptorType.values())
                                    .map(
                                            et -> {
                                                return new ProcessConfiguration(
                                                        value,
                                                        CommunicationType.REST,
                                                        SocketType.HTTP,
                                                        EnclaveType.LOCAL,
                                                        false,
                                                        "p2p",
                                                        false,
                                                        et);
                                            });
                        })
                .collect(Collectors.toList());
    }
}
