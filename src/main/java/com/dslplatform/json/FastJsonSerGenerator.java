package com.dslplatform.json;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FastJsonSerGenerator {

    public boolean publicOnly = true;
    public boolean writeNulls = false;
    public boolean excludeTransient = true;

    public StringBuilder buffer = new StringBuilder(512);

    private void genField(StringBuilder sb, Field f, boolean isLast) {
        JsonProperty p = f.getAnnotation(JsonProperty.class);
        String jsonName = p != null && !p.value().isEmpty() ? p.value() : f.getName();
        boolean unwrapped = f.getAnnotation(JsonUnwrapped.class) != null;
        boolean nullGuard = !writeNulls && !f.getType().isPrimitive();
        sb.append("\n");
        if (nullGuard) {
            sb.append(String.format("if (this.%s != null) { ", f.getName()));
        }
        if (!unwrapped) {
            sb.append(String.format("jw.writeField(\"%s\"); jw.serialize(this.%s);", jsonName, f.getName()));
        } else {
            sb.append(String.format("jw.serializeFieldsOnly(this.%s);", f.getName()));
        }

        if (!isLast) {
            sb.append(" jw.writeSep();");
        }

        if (nullGuard) {
            sb.append("}");
        }
    }


    public String generate(Class<?> cls) {
        StringBuilder sb =  this.buffer;
        sb.setLength(0);
        boolean first = true;
        boolean nullGuard = false;

        List<Field> fields = new ArrayList<>();

        for(Field f: cls.getDeclaredFields()) {
            if (f.getAnnotation(JsonIgnore.class) != null || Modifier.isStatic(f.getModifiers()) ||
                    (f.getAnnotation(JsonProperty.class) == null && (
                        (excludeTransient && Modifier.isTransient(f.getModifiers())) ||
                        (publicOnly && !Modifier.isPublic(f.getModifiers()))))) {
                continue;
            }
            fields.add(f);
        }
        int lastIndex = fields.size() - 1;
        for(int i = 0; i <= lastIndex; i++) {
            genField(sb, fields.get(i),  i == lastIndex);
        }
        return  sb.toString();
    }

}
