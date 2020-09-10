//package com.quorum.tessera.p2p.resend;
//
//import com.quorum.tessera.config.Config;
//import com.quorum.tessera.enclave.Enclave;
//import com.quorum.tessera.enclave.EnclaveFactory;
//
//import java.util.ServiceLoader;
//
//public interface TransactionRequesterFactory {
//
//    TransactionRequester createTransactionRequester(Config config);
//
//    static TransactionRequesterFactory newFactory() {
//        return ServiceLoader.load(TransactionRequesterFactory.class).findFirst().get();
//    }
//}
