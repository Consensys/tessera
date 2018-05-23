package com.github.nexus.service.locator;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringServiceLocator implements ServiceLocator {

    //FIXME: Hard coded!!
    private final ApplicationContext applicationContext = new ClassPathXmlApplicationContext("nexus-spring.xml");
    
    @Override
    public Set<Object> getServices() {
        return Stream.of(applicationContext.getBeanDefinitionNames())
                .map(applicationContext::getBean).collect(Collectors.toSet());
    }


}
