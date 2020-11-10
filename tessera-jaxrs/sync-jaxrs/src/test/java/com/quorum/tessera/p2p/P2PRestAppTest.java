//package com.quorum.tessera.p2p;
//
//import com.quorum.tessera.api.filter.IPWhitelistFilter;
//import com.quorum.tessera.config.AppType;
//import com.quorum.tessera.config.Config;
//import com.quorum.tessera.config.JdbcConfig;
//import com.quorum.tessera.config.ServerConfig;
//import com.quorum.tessera.context.RuntimeContext;
//import com.quorum.tessera.context.RuntimeContextFactory;
//import com.quorum.tessera.enclave.EnclaveFactory;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.jersey.test.JerseyTest;
//import org.glassfish.jersey.test.TestProperties;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import javax.ws.rs.client.Client;
//import javax.ws.rs.core.Application;
//import java.util.List;
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@Ignore
//public class P2PRestAppTest {
//
//    private P2PRestApp p2PRestApp;
//
//    private JerseyTest jersey;
//
//    static final RuntimeContext runtimeContext = RuntimeContextFactory.newFactory().create(mock(Config.class));
//
//    @Before
//    public void setUp() throws Exception {
//
//        EnclaveFactory enclaveFactory = EnclaveFactory.create();
//        assertThat(enclaveFactory).isExactlyInstanceOf(MockEnclaveFactory.class);
//        Config config = new Config();
//        ServerConfig serverConfig = new ServerConfig();
//        serverConfig.setApp(AppType.P2P);
//        serverConfig.setServerAddress("http://bogus.com");
//        config.setServerConfigs(List.of(serverConfig));
//        config.setJdbcConfig(new JdbcConfig());
//        config.getJdbcConfig().setUsername("JUNIT");
//        config.getJdbcConfig().setPassword("");
//        config.getJdbcConfig().setUrl("dummydburl");
//
//        Client client = mock(Client.class);
//        when(runtimeContext.getP2pClient()).thenReturn(client);
//        when(runtimeContext.isRemoteKeyValidation()).thenReturn(true);
//
//        p2PRestApp = new P2PRestApp();
//
//        jersey =
//                new JerseyTest() {
//                    @Override
//                    protected Application configure() {
//                        enable(TestProperties.LOG_TRAFFIC);
//                        enable(TestProperties.DUMP_ENTITY);
//                        ResourceConfig jerseyconfig = ResourceConfig.forApplication(p2PRestApp);
//                        return jerseyconfig;
//                    }
//                };
//
//        jersey.setUp();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        jersey.tearDown();
//    }
//
//    @Test
//    public void getSingletons() {
//        Set<Object> results = p2PRestApp.getSingletons();
//        assertThat(results).hasSize(3);
//        results.forEach(
//                o ->
//                        assertThat(o)
//                                .isInstanceOfAny(
//                                        PartyInfoResource.class, IPWhitelistFilter.class, TransactionResource.class));
//    }
//
//    @Test
//    public void recoverP2PApp() {
//        when(runtimeContext.isRecoveryMode()).thenReturn(true);
//        p2PRestApp = new P2PRestApp();
//        Set<Object> results = p2PRestApp.getSingletons();
//        assertThat(results).hasSize(3);
//        results.forEach(
//                o ->
//                        assertThat(o)
//                                .isInstanceOfAny(
//                                        PartyInfoResource.class, IPWhitelistFilter.class, RecoveryResource.class));
//    }
//
//
//
//    @Test
//    public void appType() {
//        assertThat(p2PRestApp.getAppType()).isEqualTo(AppType.P2P);
//    }
//}
