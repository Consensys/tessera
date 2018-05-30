
package com.github.nexus.api.example;

import javax.validation.constraints.NotNull;



public class SomeOtherObject {
 
    @NotNull
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
