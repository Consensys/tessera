package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.CucumberRawIT;
import com.quorum.tessera.test.CucumberRestIT;
import com.quorum.tessera.test.DBType;
import org.junit.runner.RunWith;
import suite.ProcessConfig;
import suite.SocketType;
import suite.TestSuite;

@RunWith(TestSuite.class)
@ProcessConfig(communicationType = CommunicationType.REST, dbType = DBType.H2, socketType = SocketType.UNIX)
@TestSuite.SuiteClasses({CucumberRestIT.class, CucumberRawIT.class})
public class RestSuiteUnixH2 {}
