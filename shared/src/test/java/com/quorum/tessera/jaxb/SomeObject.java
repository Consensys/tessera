
package com.quorum.tessera.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SomeObject {
    
    private String someValue;

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }
    
    
}
