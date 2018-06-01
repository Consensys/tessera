package com.github.nexus.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "SOME_ENTITY")
public class SomeEntity implements Serializable {

    @Id
    @Column(name="ID")
    @SequenceGenerator(name = "SOME_SEQ",sequenceName = "SOME_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "SOME_SEQ")
    private Long id;

    @Basic
    @Column(name = "VALUE")
    private String value;

    public SomeEntity(String value) {
        this.value = value;
    }

    public SomeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SomeEntity other = (SomeEntity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
