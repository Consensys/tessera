package com.quorum.tessera.discovery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OnCreateHelperFactoryTest {

    private OnCreateHelperFactory onCreateHelperFactory;

    private OnCreateHelper onCreateHelper;

    @Before
    public void beforeTest() {
        onCreateHelper = mock(OnCreateHelper.class);
        onCreateHelperFactory = new OnCreateHelperFactory(onCreateHelper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(onCreateHelper);
    }

    @Test
    public void onCreate() {
        onCreateHelperFactory.onCreate();
        verify(onCreateHelper).onCreate();
    }

    @Test
    public void provider() {
        OnCreateHelper helper = OnCreateHelperFactory.provider();
        assertThat(helper).isNotNull()
            .isExactlyInstanceOf(OnCreateHelperImpl.class);

    }


    @Test
    public void defaultConstructor() {
        OnCreateHelper helper = new OnCreateHelperFactory();
        assertThat(helper).isNotNull()
            .isExactlyInstanceOf(OnCreateHelperFactory.class);

    }
}
