package com.quorum.tessera.config.constraints;

import com.quorum.tessera.io.FilesDelegate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidContentValidator implements ConstraintValidator<ValidContent, Path> {

  private ValidContent config;

  @Override
  public void initialize(ValidContent constraintAnnotation) {
    this.config = constraintAnnotation;
  }

  @Override
  public boolean isValid(Path path, ConstraintValidatorContext context) {

    if (Objects.isNull(path)) {
      return true;
    }

    if (!Files.exists(path)) {
      return true;
    }

    List<String> lines =
        FilesDelegate.create()
            .lines(path)
            .filter(line -> !Objects.equals("", line))
            .collect(Collectors.toList());

    return lines.size() >= config.minLines() && lines.size() <= config.maxLines();
  }
}
