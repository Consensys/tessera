package com.quorum.tessera.eclipselink;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class SomeEntity {

  @Id
  @GeneratedValue(generator = AtomicLongSequence.SEQUENCE_NAME, strategy = GenerationType.AUTO)
  private Long id;

  @Basic private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeEntity that = (SomeEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
