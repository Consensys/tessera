// package com.quorum.tessera.app;
//
//
// import com.quorum.tessera.service.locator.ServiceLocator;
//import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;
//
//
// import static org.mockito.Mockito.*;
//
// public class RestAppTest {
//
//    private ServiceLocator serviceLocator;
//
//    private SampleApp sampleApp;
//
//    @Before
//    public void setUp() {
//        sampleApp = new SampleApp();
//    }
//
//    @After
//    public void tearDown() {
//        verifyNoMoreInteractions(serviceLocator);
//    }
//
//    @Test
//    public void getClasses() {
//        assertThat(sampleApp.getClasses()).isNotEmpty();
//        assertThat(sampleApp.getAppType()).isNotNull();
//        
//    }
//
// }
