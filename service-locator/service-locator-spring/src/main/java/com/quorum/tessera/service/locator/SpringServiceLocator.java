package com.quorum.tessera.service.locator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Spring implementation of the Service Locator
 * that accepts xml bean definition files
 */
public class SpringServiceLocator implements ServiceLocator {

    private static ApplicationContext context;

    /**
     * If the Spring context is already established, then returns the
     * previously generated set of beans
     *
     * {@inheritDoc}
     */
    @Override
    public Set<Object> getServices(final String filename) {
        if(context == null) {
            context = new ClassPathXmlApplicationContext(filename);
        }

        return Stream
            .of(context.getBeanDefinitionNames())
            .map(context::getBean)
            .collect(Collectors.toSet());
    }

}
