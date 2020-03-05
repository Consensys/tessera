package com.quorum.tessera.service.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A Spring implementation of the Service Locator that accepts xml bean definition files */
public class SpringServiceLocator implements ServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringServiceLocator.class);

    private static ApplicationContext context;

    /**
     * If the Spring context is already established, then returns the previously generated set of beans
     *
     * <p>{@inheritDoc}
     */
    @Override
    public Set<Object> getServices() {

        if (context == null) {
            LOGGER.trace("Creating spring application context");
            context = new ClassPathXmlApplicationContext("tessera-spring.xml");
            LOGGER.trace("Created spring application context {}",context);
        }
        LOGGER.trace("Loading services");
        Set<Object> services = Stream.of(context.getBeanDefinitionNames())
            .peek(n -> LOGGER.trace("Spring bean def {}",n))
            .map(context::getBean)
            .collect(Collectors.toSet());
        LOGGER.trace("Loaded services");
        return services;
    }
}
