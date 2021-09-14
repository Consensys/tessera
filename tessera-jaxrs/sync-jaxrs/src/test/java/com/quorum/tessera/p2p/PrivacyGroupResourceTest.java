package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import jakarta.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrivacyGroupResourceTest {

  private PrivacyGroupManager privacyGroupManager;

  private PrivacyGroupResource privacyGroupResource;

  @Before
  public void beforeTest() throws Exception {

    privacyGroupManager = mock(PrivacyGroupManager.class);
    privacyGroupResource = new PrivacyGroupResource(privacyGroupManager);
  }

  @After
  public void afterTest() throws Exception {
    verifyNoMoreInteractions(privacyGroupManager);
  }

  @Test
  public void testStorePrivacyGroup() {
    doNothing().when(privacyGroupManager).storePrivacyGroup("encoded".getBytes());

    final Response response = privacyGroupResource.storePrivacyGroup("encoded".getBytes());

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    verify(privacyGroupManager).storePrivacyGroup("encoded".getBytes());
  }
}
