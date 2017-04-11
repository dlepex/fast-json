package github.fastjson;


/**
 * @see FastJsonSerGenerator - use this class  to "manually" generate serializeFieldsOnly body.
 */
public interface FastJsonSerializable {

    // override this method only if you need something custom
    default void serialize(JsonWriter jw, boolean flag) { // flag param is never used.
        jw.writeByte(JsonWriter.OBJECT_START);
        serializeUnwrapped(jw);
        jw.backIfSeparator();
        jw.writeByte(JsonWriter.OBJECT_END);
    }

    // prefer to implement this method
    // never call this method directly, it may leave trailing commas
    default void serializeUnwrapped(JsonWriter jw) {}
}
