//package com.quorum.tessera.config.migration;
//
//import com.quorum.tessera.config.cli.CliResult;
//import com.quorum.tessera.config.util.JaxbUtil;
////import javax.validation.ConstraintViolationException;
//
//public class Main {
//    
//    public static void main(String... args) throws Exception {        
//        LegacyCliAdapter adapter = new LegacyCliAdapter();
//        try {
//            CliResult result = adapter.execute(args);
//            result.getConfig().ifPresent(c -> JaxbUtil.marshal(c,  System.out));
//            System.exit(result.getStatus());
//// 
////        } catch(ConstraintViolationException ex) {
////            ex.getConstraintViolations().forEach(v -> System.err.println(v));
////            System.exit(2);
//        } catch(Exception ex) {
//            System.err.println(ex.getMessage());
//            System.exit(1);
//        }
//
//    }
//}
