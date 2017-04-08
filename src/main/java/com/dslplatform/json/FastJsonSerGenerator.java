package com.dslplatform.json;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FastJsonSerGenerator {


    public static String generateFieldsSer(Class<?> cls, boolean publicOnly, boolean writeNulls) {
        StringBuilder sb = new StringBuilder("\n serializeFieldsOnly() for class: " + cls.getSimpleName()).append("\n");
        boolean first = true;
        boolean nullGuard = false;
        for(Field f: cls.getDeclaredFields()) {
            if (f.getAnnotation(JsonIgnore.class) != null || Modifier.isStatic(f.getModifiers()) ||
                    ( publicOnly && ! Modifier.isPublic(f.getModifiers()))) {
                continue;
            }

            if(!first) {
                sb.append(" jw.writeSep();");
                if (nullGuard) {
                    sb.append("}");
                }
            }
            first = false;

            JsonProperty p = f.getAnnotation(JsonProperty.class);
            String jsonName = p != null ? p.value() : f.getName();
            boolean unwrapped = f.getAnnotation(JsonUnwrapped.class) != null;
            nullGuard = !writeNulls && !f.getType().isPrimitive();
            sb.append("\n");
            if (nullGuard) {
                sb.append(String.format("if (this.%s != null) { ", f.getName()));
            }
            if (!unwrapped) {
                sb.append(String.format("jw.writeField(\"%s\"); jw.serialize(this.%s);", jsonName, f.getName()));
            } else {
                sb.append(String.format("this.%s.serializeFieldsOnly(jw);", f.getName()));
            }
        }
        if (nullGuard) {
            sb.append("}");
        }
        return  sb.toString();
    }

}
