package com.quorum.tessera.server.jersey;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.internal.ConstantActiveDescriptor;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServiceLoaderInjectionResolver implements JustInTimeInjectionResolver {

  private final ServiceLocator serviceLocator;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ServiceLoaderInjectionResolver.class);

  @Inject
  public ServiceLoaderInjectionResolver(ServiceLocator serviceLocator) {
    this.serviceLocator = Objects.requireNonNull(serviceLocator);
  }

  @Override
  public boolean justInTimeResolution(Injectee injectee) {
    LOGGER.debug("Injectee: {}", injectee);
    Type requiredType = injectee.getRequiredType();
    Class<?> type = (Class<?>) requiredType;

    if (type.isInterface()) {
      getClass().getModule().addUses(type);
      ServiceLoader serviceLoader = ServiceLoader.load(type);
      Object service = serviceLoader.findFirst().get();
      LOGGER.debug("Found {} for injection", service);

      Class impl = service.getClass();

      Set qualifiers =
          Arrays.stream(impl.getAnnotations())
              .map(Annotation::annotationType)
              .filter(a -> a.isAnnotationPresent(Qualifier.class))
              .collect(Collectors.toSet());

      String name =
          Optional.of(type)
              .filter(c -> c.isAnnotationPresent(Named.class))
              .map(c -> c.getAnnotation(Named.class))
              .map(Named::value)
              .orElse(type.getName());

      // TODO: We should be able to support directly
      Class<? extends Annotation> scope = Singleton.class;

      ConstantActiveDescriptor constantActiveDescriptor =
          new ConstantActiveDescriptor(
              service,
              Set.of(type),
              scope,
              name,
              qualifiers,
              DescriptorVisibility.LOCAL,
              false,
              false,
              null,
              Map.of(),
              1);

      ServiceLocatorUtilities.addOneDescriptor(serviceLocator, constantActiveDescriptor);
      LOGGER.info("Created Descriptor {} for injection", constantActiveDescriptor);

      return true;
    }

    return false;
  }
}
