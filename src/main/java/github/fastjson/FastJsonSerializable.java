package github.fastjson;


/**
 * @see FastJsonSerGenerator - use this class  to "manually" generate serializeFieldsOnly body.
 */
public interface FastJsonSerializable {

    default void serialize(JsonWriter jw, boolean flag) { // flag param is never used.
        jw.serialize(this);
    }

    // prefer to implement this method
    default void serializeUnwrapped(JsonWriter jw) {}
}
