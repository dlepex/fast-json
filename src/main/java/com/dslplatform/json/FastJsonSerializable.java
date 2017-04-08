package com.dslplatform.json;


/**
 * @see FastJsonSerGenerator - use this class manyally to generate serializeFieldsOnly body.
 */
public interface FastJsonSerializable {

    default void serialize(JsonWriter jw, boolean minimal) {
        jw.writeByte(JsonWriter.OBJECT_START);
        serializeFieldsOnly(jw);
        jw.writeByte(JsonWriter.OBJECT_END);
    }

    default void serializeFieldsOnly(JsonWriter jw) {}
}
