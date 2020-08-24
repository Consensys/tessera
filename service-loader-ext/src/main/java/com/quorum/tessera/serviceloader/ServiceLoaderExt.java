package com.quorum.tessera.serviceloader;

import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ServiceLoaderExt<S> {

    private final java.util.ServiceLoader<S> serviceLoader;

    private ServiceLoaderExt(java.util.ServiceLoader<S> serviceLoader) {
        this.serviceLoader = serviceLoader;
    }

    public static <S> ServiceLoaderExt<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoaderExt<>(java.util.ServiceLoader.load(service, loader));
    }

    public static <S> ServiceLoaderExt<S> load(Class<S> service) {
        return new ServiceLoaderExt<>(java.util.ServiceLoader.load(service));
    }

    public static <S> ServiceLoaderExt loadInstalled(Class<S> service) {
        return new ServiceLoaderExt(java.util.ServiceLoader.loadInstalled(service));
    }

    public static <S> ServiceLoaderExt<S> load(ModuleLayer layer, Class<S> service) {
        return new ServiceLoaderExt<>(java.util.ServiceLoader.load(layer, service));
    }

    public Optional<S> findFirst() {
        return serviceLoader.stream()
            .map(java.util.ServiceLoader.Provider::get)
            .findFirst();
    }

    public void reload() {
        serviceLoader.reload();
    }

    public void forEach(Consumer<? super S> action) {
        stream()
            .map(java.util.ServiceLoader.Provider::get)
            .forEach(action);
    }

    public Spliterator<S> spliterator() {
        return stream()
            .map(java.util.ServiceLoader.Provider::get)
            .spliterator();
    }

    public Iterator<S> iterator() {
        return stream()
            .map(java.util.ServiceLoader.Provider::get)
            .iterator();
    }

    public Stream<java.util.ServiceLoader.Provider<S>> stream() {
        return serviceLoader.stream()
            .map(ProviderExt::new);
    }

    private static class ProviderExt<S> implements ServiceLoader.Provider<S> {

        private final ServiceLoader.Provider<S> provider;

        private static Object instance;

        private ProviderExt(ServiceLoader.Provider<S> provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        @Override
        public Class<? extends S> type() {
            return provider.type();
        }

        @Override
        public S get() {
            if (type().isAnnotationPresent(Singleton.class)) {
                if (Objects.isNull(instance)) {
                    instance = provider.get();
                }
                return (S) instance;
            }

            return provider.get();
        }
    }
}
