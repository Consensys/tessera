package com.quorum.tessera;

import com.acme.DefaultTestService;
import com.acme.TestService;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceLoaderUtilTest {

    @Test
    public void noServiceFound() {
        final Optional<ServiceLoaderUtilTest> result = ServiceLoaderUtil.load(ServiceLoaderUtilTest.class);

        assertThat(result).isNotPresent();
    }

    @Test
    public void serviceFound() {
        final Optional<TestService> result = ServiceLoaderUtil.load(TestService.class);

        assertThat(result).isPresent();
        assertThat(result).get().isInstanceOf(DefaultTestService.class);
    }

    @Test
    public void noServiceFoundWithStream() {
        final Stream<ServiceLoaderUtilTest> result = ServiceLoaderUtil.loadAll(ServiceLoaderUtilTest.class);

        assertThat(result).hasSize(0);
    }

    @Test
    public void serviceFoundWithStream() {
        final Stream<TestService> result = ServiceLoaderUtil.loadAll(TestService.class);

        assertThat(result).hasSize(1);
    }
}
