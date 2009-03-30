package org.exigencecorp.bindgen.processor;

import java.lang.reflect.Field;

public class FieldWalker {

    public static Object walk(Object root, String... fieldNames) throws Exception {
        Object value = root;
        for (String fieldName : fieldNames) {
            Field field = null;
            for (Class<?> currentClass = value.getClass(); currentClass != null && field == null; currentClass = currentClass.getSuperclass()) {
                try {
                    field = currentClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException nsfe) {
                }
            }
            if (field == null) {
                break;
            }
            field.setAccessible(true);
            value = field.get(value);
        }
        return value;
    }

}
