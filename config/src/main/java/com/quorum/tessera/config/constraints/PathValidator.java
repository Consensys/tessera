package com.quorum.tessera.config.constraints;

import com.quorum.tessera.io.FilesDelegate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathValidator implements ConstraintValidator<ValidPath, Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathValidator.class);

  private ValidPath config;

  private FilesDelegate filesDelegate = FilesDelegate.create();

  @Override
  public void initialize(ValidPath a) {
    this.config = a;
  }

  @Override
  public boolean isValid(Path t, ConstraintValidatorContext constraintContext) {
    // Not null deals with this
    if (Objects.isNull(t)) {
      return true;
    }

    if (config.checkCanCreate() && filesDelegate.notExists(t)) {
      try {
        filesDelegate.createFile(t);
      } catch (UncheckedIOException ex) {
        LOGGER.debug(null, ex);
        constraintContext.disableDefaultConstraintViolation();

        String sanitised =
            Objects.toString(t)
                .replaceAll(Pattern.quote("$"), "")
                .replaceAll(Pattern.quote("#"), "");
        String message = String.format("Unable to create file %s", sanitised);
        constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
      } finally {
        try {
          filesDelegate.deleteIfExists(t);
        } catch (UncheckedIOException ex) {
          LOGGER.trace(null, ex);
          // Not much we can do
        }
      }
    }

    return !config.checkExists() || !filesDelegate.notExists(t);
  }

  /*
  Testing only needs factory
  */
  protected void setFilesDelegate(FilesDelegate filesDelegate) {
    this.filesDelegate = filesDelegate;
  }
}
