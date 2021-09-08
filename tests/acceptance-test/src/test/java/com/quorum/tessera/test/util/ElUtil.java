package com.quorum.tessera.test.util;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElUtil {

  public static InputStream process(InputStream inputStream, Map<String, ?> parameters) {

    String data =
        Stream.of(inputStream)
            .map(InputStreamReader::new)
            .map(BufferedReader::new)
            .flatMap(BufferedReader::lines)
            .collect(Collectors.joining(System.lineSeparator()));
    String result = process(data, parameters);

    return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
  }

  public static String process(String data, Map<String, ?> parameters) {
    ELProcessor eLProcessor = new ELProcessor();

    parameters.entrySet().forEach(e -> eLProcessor.defineBean(e.getKey(), e.getValue()));

    ELContext eLContext = eLProcessor.getELManager().getELContext();
    ValueExpression valueExpression =
        ExpressionFactory.newInstance().createValueExpression(eLContext, data, String.class);
    return (String) valueExpression.getValue(eLContext);
  }

  public static Path createTempFileFromTemplate(URL template, Map<String, ?> parameters) {

    try (InputStream in = process(template.openStream(), parameters)) {

      String data =
          Stream.of(in)
              .map(InputStreamReader::new)
              .map(BufferedReader::new)
              .flatMap(BufferedReader::lines)
              .collect(Collectors.joining(System.lineSeparator()));

      Path file = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
      file.toFile().deleteOnExit();
      Files.write(file, data.getBytes());

      return file;

    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static Path createAndPopulatePaths(URL template) throws IOException {

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    return ElUtil.createTempFileFromTemplate(template, params);
  }

  public static Path createAndPopulatePaths(Path template) throws IOException {

    Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

    Map<String, Object> params = new HashMap<>();
    params.put("unixSocketPath", unixSocketPath.toString());

    return ElUtil.createTempFileFromTemplate(template.toUri().toURL(), params);
  }
}
