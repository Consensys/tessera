package com.quorum.tessera.server.jersey;

import org.glassfish.hk2.api.*;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

@Singleton
public class ServiceLoaderInjectionResolver implements JustInTimeInjectionResolver {

    private final ServiceLocator serviceLocator;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoaderInjectionResolver.class);

    @Inject
    public ServiceLoaderInjectionResolver(ServiceLocator serviceLocator) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
    }

    @Override
    public boolean justInTimeResolution(Injectee injectee) {
        LOGGER.debug("Injectee: {}", injectee);

        final Type type = injectee.getRequiredType();
        final Class<?> typeClass = (Class<?>) type;

        if(typeClass.isInterface()) {

            final ServiceLoader<?> serviceLoader = ServiceLoader.load(typeClass);
            serviceLoader.stream()
                .map(p -> p.type())
                .forEach(t -> {

                    final DescriptorImpl descriptor = new DescriptorImpl();
                    descriptor.setImplementation(t.getName());
                    descriptor.addAdvertisedContract(t.getName());

                    Arrays.stream(t.getInterfaces())
                        .map(Class::getName)
                        .forEach(descriptor::addAdvertisedContract);

                    Arrays.stream(t.getAnnotations())
                        .map(Annotation::annotationType)
                        .filter(a -> a.isAnnotationPresent(Scope.class))
                        .map(Class::getName)
                        .findFirst()
                        .ifPresent(descriptor::setScope);

                    Arrays.stream(t.getAnnotations())
                        .map(Annotation::annotationType)
                        .filter(a -> a.isAnnotationPresent(Qualifier.class))
                        .map(Class::getName)
                        .forEach(descriptor::addQualifier);

                    Optional.of(t)
                        .filter(c -> c.isAnnotationPresent(Named.class))
                        .map(c -> c.getAnnotation(Named.class))
                        .map(Named::value)
                        .ifPresent(descriptor::setName);


                    ActiveDescriptor outome = ServiceLocatorUtilities.addOneDescriptor(serviceLocator, descriptor);
                    LOGGER.debug("descriptor: {}", Objects.toString(outome));

                });
                ServiceLocatorUtilities.dumpAllDescriptors(serviceLocator);
            return true;
        }
        return false;

        //resolver.justInTimeResolution(injectee);
    }


}

