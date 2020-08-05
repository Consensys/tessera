package com.quorum.tessera.context;

import org.junit.After;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class ContextTestCase {

    @After
    public void clearFields() throws Exception {

        List<Field> fields =
                Arrays.stream(DefaultContextHolder.class.getDeclaredFields())
                        .filter(not(Field::isEnumConstant))
                        .filter(not(Field::isSynthetic))
                        .collect(Collectors.toList());

        for (Field field : fields) {
            field.setAccessible(true);
            field.set(DefaultContextHolder.INSTANCE, null);
        }
    }
}
