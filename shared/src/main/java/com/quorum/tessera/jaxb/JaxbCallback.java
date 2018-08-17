
package com.quorum.tessera.jaxb;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBException;

@FunctionalInterface
public interface JaxbCallback<T> {
    
    
    T doExecute() throws JAXBException,DataBindingException;
    
    
    static <T> T execute(JaxbCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (JAXBException ex) {
            throw new DataBindingException(ex);
        }
    }
    
}
