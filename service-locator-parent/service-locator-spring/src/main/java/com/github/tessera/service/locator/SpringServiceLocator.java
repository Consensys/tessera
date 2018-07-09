package com.github.tessera.service.locator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringServiceLocator implements ServiceLocator {

    private static ApplicationContext context;

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
