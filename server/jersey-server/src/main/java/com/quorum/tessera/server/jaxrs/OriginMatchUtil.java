package com.quorum.tessera.server.jaxrs;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OriginMatchUtil {

  private final List<String> tokens;

  private final Predicate<String> wildcardMatch = s -> s.equals("*");

  public OriginMatchUtil(List<String> tokens) {
    this.tokens =
        tokens.stream()
            .map(String::toLowerCase)
            .map(s -> ("\\Q" + s + "\\E"))
            .map(s -> s.replace("*", "\\E.*\\Q"))
            .collect(Collectors.toList());
  }

  public boolean matches(String origin) {

    if (Objects.isNull(origin) || Objects.equals("", origin)) {
      return false;
    }

    Predicate<String> subdomainMatch = s -> origin.toLowerCase().matches(s);
    Predicate<String> matchingCritera = wildcardMatch.or(subdomainMatch);

    return tokens.stream().anyMatch(matchingCritera);
  }
}
