package github.fastjson;


/**
 * @see FastJsonSerGenerator - use this class  to "manually" generate serializeFieldsOnly body.
 */
public interface FastJsonSerializable {

    default void serialize(JsonWriter jw, boolean flag) { // flag param is never used.
        jw.writeByte(JsonWriter.OBJECT_START);
        serializeUnwrapped(jw);
        jw.writeByte(JsonWriter.OBJECT_END);
    }

    // prefer to implement this method
    default void serializeUnwrapped(JsonWriter jw) {}
}
