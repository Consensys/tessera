package com.github.nexus.test.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.el.ELContext;
import javax.el.ELProcessor;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

public class ElUtil {

    public static InputStream process(InputStream inputStream, Map<String, ?> parameters) {

        String data = Stream.of(inputStream)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines).collect(Collectors.joining());
        String result = process(data, parameters);

        return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));

    }

    public static String process(String data, Map<String, ?> parameters) {
        ELProcessor eLProcessor = new ELProcessor();

        parameters.entrySet().forEach(e -> eLProcessor.defineBean(e.getKey(), e.getValue()));

        ELContext eLContext = eLProcessor.getELManager().getELContext();
        ValueExpression valueExpression = ExpressionFactory.newInstance()
                .createValueExpression(eLContext, data, String.class);
        return (String) valueExpression.getValue(eLContext);
    }

}
