/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quorum.tessera.api.grpc;

import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Set;

/**
 *
 * @author mark
 */
public class GrpcServiceLocator implements ServiceLocator {

    @Override
    public Set<Object> getServices(String filename) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
