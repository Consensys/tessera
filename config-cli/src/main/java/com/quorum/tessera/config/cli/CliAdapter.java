
package com.quorum.tessera.config.cli;



public interface CliAdapter {
     CliResult execute(String... args) throws Exception;
     
     
     static CliAdapter create() {
         if(Boolean.valueOf(System.getProperty("tessera.config.legacy","false"))) {
             return new LegacyCliAdapter();
         }
         return new DefaultCliAdapter();
     }
     
}
