package com.github.nexus.configuration;

import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELManager;
import javax.el.ELProcessor;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

public class Main {

   public static String file() {
        return "STUFF";
    }
    
    public static void main(String... args) throws Exception {

        ELProcessor eLProcessor = new ELProcessor();
       
        Map map = new HashMap();
        map.put("greeting", "HEKLLOW");
        ELContext eLContext = eLProcessor.getELManager().getELContext();
        eLProcessor.defineBean("foo", map);
        
        
        eLContext.getFunctionMapper().mapFunction("ping", "ping", Main.class.getDeclaredMethod("doStuff"));
        ExpressionFactory factory = ELManager.getExpressionFactory();
        ValueExpression e = factory.createValueExpression(eLContext, "Hello ${foo.greeting}! ${ping:ping()}", String.class);
        System.out.println(e.getValue(eLContext)); // --> Hello, bar!

    }

}
