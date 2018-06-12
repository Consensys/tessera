package com.github.nexus.configuration;

import javax.el.*;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Uses Java EL to transform values by registering function transformers to an EL Context
 */
public class ConfigurationResolverImpl implements ConfigurationResolver {

    private final ELContext context;

    private final ExpressionFactory factory = ELManager.getExpressionFactory();

    public ConfigurationResolverImpl(final ConfigurationInterceptor... interceptors) {
        final ELProcessor eLProcessor = new ELProcessor();

        this.context = eLProcessor.getELManager().getELContext();

        Stream.of(interceptors).forEach(i -> i.register(context));
    }

    public Properties resolveProperties(final Properties properties) {

        final Properties newProps = new Properties();

        properties.forEach((key, value) -> {
            final ValueExpression ex = factory.createValueExpression(context, value.toString(), String.class);

            newProps.setProperty(key.toString(), ex.getValue(context).toString());
        });

        return newProps;
    }

}
