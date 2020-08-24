package com.quorum.tessera.serviceloader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceLoaderExtTest {

    private ServiceLoaderExt<SampleService> sampleServiceServiceLoader;

    @Before
    public void beforeTest() throws Exception {
        sampleServiceServiceLoader = ServiceLoaderExt.load(SampleService.class);
    }

    @After
    public void afterTest() {

    }

    @Test
    public void findFirst() {
        assertThat(sampleServiceServiceLoader.findFirst())
            .containsInstanceOf(SampleServiceImpl.class);
    }

    @Test
    public void stream() {
        assertThat(sampleServiceServiceLoader.stream()).hasSize(2);
    }

    @Test
    public void iterator() {
        assertThat(sampleServiceServiceLoader.iterator())
            .hasSize(2);

    }

    @Test
    public void reload() {
        sampleServiceServiceLoader.reload();
    }

    @Test
    public void singletonAnnotatedClassIsAlwaysTheSame() {

        Predicate<java.util.ServiceLoader.Provider> findSingletonProviders = p -> p.type() == SingletonSampleService.class;

       SampleService firstResult = sampleServiceServiceLoader.stream()
            .filter(findSingletonProviders)
            .map(java.util.ServiceLoader.Provider::get)
            .findAny().get();

        SampleService secondResult = sampleServiceServiceLoader.stream()
            .filter(findSingletonProviders)
            .map(java.util.ServiceLoader.Provider::get)
            .findAny().get();

        assertThat(firstResult).isSameAs(secondResult);
    }

    @Test
    public void nonSingletonClasses() {

        Predicate<java.util.ServiceLoader.Provider> findSingletonProviders = p -> p.type() == SampleServiceImpl.class;

        SampleService firstResult = sampleServiceServiceLoader.stream()
            .filter(findSingletonProviders)
            .map(java.util.ServiceLoader.Provider::get)
            .findAny().get();

        SampleService secondResult = sampleServiceServiceLoader.stream()
            .filter(findSingletonProviders)
            .map(java.util.ServiceLoader.Provider::get)
            .findAny().get();

        assertThat(firstResult).isNotSameAs(secondResult);

    }



}
