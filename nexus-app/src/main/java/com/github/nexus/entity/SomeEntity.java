package com.github.nexus.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class SomeEntity {

    @Id
    private String id;
    private String value;

    public SomeEntity(String value) {
        this.value = value;
    }

    public SomeEntity() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
