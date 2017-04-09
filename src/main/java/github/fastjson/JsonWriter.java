package github.fastjson;

import com.dslplatform.json.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

/**
 * This class is used for growing such byte[] buffer (grow factor = 1.5)
 * <p>
 * After the processing is done, JSON can be copied to target OutputStream or resulting byte[] can be used directly.
 * <p>
 * They should not be shared across threads (concurrently) so for Thread reuse it's best to use patterns such as ThreadLocal.
 */
public final class JsonWriter extends Writer {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public byte[] ensureCapacity(final int free) {
        if (position + free >= result.length) {
            result = Arrays.copyOf(result, result.length + (result.length << 1) + free);
        }
        return result;
    }

    public void advance(int size) {
        position += size;
    }

    private int position;
    private byte[] result;

    public final UnknownSerializer unknownSerializer;

    // prefer this constructor.
    // buf instances may be put in thread-local to reduce gc pressure
    public static JsonWriter create(final byte[] buf) {
        return new JsonWriter(buf, (w, o) -> {
            throw new IOException("serializing unknown obj: " + o.getClass());
        });
    }



    //// pre-allocated lambdas :

    public static final Serializer<String> serOfString = JsonWriter::serialize;
    public static final Serializer<String> serOfAscii = JsonWriter::writeAsciiOrNull;
    public static final Serializer<Integer> serOfInt = JsonWriter::serialize;
    public static final Serializer<Long> serOfLong = JsonWriter::serialize;
    public static final Serializer<Double> serOfDouble = JsonWriter::serialize;
    public static final Serializer<Boolean> serOfBool = JsonWriter::serialize;

    @Deprecated
    public JsonWriter() {
        this(512, null);
    }
    @Deprecated
    public JsonWriter(final UnknownSerializer unknownSerializer) {
        this(512, unknownSerializer);
    }
    @Deprecated
    public JsonWriter(final int size, final UnknownSerializer unknownSerializer) {
        this(new byte[size], unknownSerializer);
    }
    @Deprecated
    public JsonWriter(final byte[] result, final UnknownSerializer unknownSerializer) {
        this.result = result;
        this.unknownSerializer = unknownSerializer;
    }


    public static final byte OBJECT_START = '{';
    public static final byte OBJECT_END = '}';
    public static final byte ARRAY_START = '[';
    public static final byte ARRAY_END = ']';
    public static final byte COMMA = ',';
    public static final byte SEMI = ':';
    public static final byte QUOTE = '"';
    public static final byte ESCAPE = '\\';

    public final void writeNull() {
        final int s = position;
        position += 4;
        if (position >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2);
        }
        final byte[] _result = result;
        _result[s] = 'n';
        _result[s + 1] = 'u';
        _result[s + 2] = 'l';
        _result[s + 3] = 'l';
    }

    public final void writeByte(final byte c) {
        if (position == result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2);
        }
        result[position++] = c;
    }

    public final void writeString(final String str) {
        final int len = str.length();
        if (position + (len << 2) + (len << 1) + 2 >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + (len << 2) + (len << 1) + 2);
        }
        final byte[] _result = result;
        _result[position] = QUOTE;
        int cur = position + 1;
        for (int i = 0; i < len; i++) {
            final char c = str.charAt(i);
            if (c > 31 && c != '"' && c != '\\' && c < 126) {
                _result[cur++] = (byte) c;
            } else {
                writeQuotedString(str, i, cur, len);
                return;
            }
        }
        _result[cur] = QUOTE;
        position = cur + 1;
    }

    private void writeQuotedString(final String str, int i, int cur, final int len) {
        final byte[] _result = this.result;
        for (; i < len; i++) {
            final char c = str.charAt(i);
            if (c == '"') {
                _result[cur++] = ESCAPE;
                _result[cur++] = QUOTE;
            } else if (c == '\\') {
                _result[cur++] = ESCAPE;
                _result[cur++] = ESCAPE;
            } else if (c < 32) {
                if (c == 8) {
                    _result[cur++] = ESCAPE;
                    _result[cur++] = 'b';
                } else if (c == 9) {
                    _result[cur++] = ESCAPE;
                    _result[cur++] = 't';
                } else if (c == 10) {
                    _result[cur++] = ESCAPE;
                    _result[cur++] = 'n';
                } else if (c == 12) {
                    _result[cur++] = ESCAPE;
                    _result[cur++] = 'f';
                } else if (c == 13) {
                    _result[cur++] = ESCAPE;
                    _result[cur++] = 'r';
                } else {
                    _result[cur] = ESCAPE;
                    _result[cur + 1] = 'u';
                    _result[cur + 2] = '0';
                    _result[cur + 3] = '0';
                    switch (c) {
                    case 0:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '0';
                        break;
                    case 1:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '1';
                        break;
                    case 2:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '2';
                        break;
                    case 3:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '3';
                        break;
                    case 4:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '4';
                        break;
                    case 5:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '5';
                        break;
                    case 6:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '6';
                        break;
                    case 7:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = '7';
                        break;
                    case 11:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = 'B';
                        break;
                    case 14:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = 'E';
                        break;
                    case 15:
                        _result[cur + 4] = '0';
                        _result[cur + 5] = 'F';
                        break;
                    case 16:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '0';
                        break;
                    case 17:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '1';
                        break;
                    case 18:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '2';
                        break;
                    case 19:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '3';
                        break;
                    case 20:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '4';
                        break;
                    case 21:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '5';
                        break;
                    case 22:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '6';
                        break;
                    case 23:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '7';
                        break;
                    case 24:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '8';
                        break;
                    case 25:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = '9';
                        break;
                    case 26:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'A';
                        break;
                    case 27:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'B';
                        break;
                    case 28:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'C';
                        break;
                    case 29:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'D';
                        break;
                    case 30:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'E';
                        break;
                    default:
                        _result[cur + 4] = '1';
                        _result[cur + 5] = 'F';
                        break;
                    }
                    cur += 6;
                }
            } else if (c < 0x007F) {
                _result[cur++] = (byte) c;
            } else {
                final int cp = str.codePointAt(i);
                if (Character.isSupplementaryCodePoint(cp)) {
                    i++;
                }
                if (cp == 0x007F) {
                    _result[cur++] = (byte) cp;
                } else if (cp <= 0x7FF) {
                    _result[cur++] = (byte) (0xC0 | ((cp >> 6) & 0x1F));
                    _result[cur++] = (byte) (0x80 | (cp & 0x3F));
                } else if ((cp < 0xD800) || (cp > 0xDFFF && cp <= 0xFFFD)) {
                    _result[cur++] = (byte) (0xE0 | ((cp >> 12) & 0x0F));
                    _result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                    _result[cur++] = (byte) (0x80 | (cp & 0x3F));
                } else if (cp >= 0x10000 && cp <= 0x10FFFF) {
                    _result[cur++] = (byte) (0xF0 | ((cp >> 18) & 0x07));
                    _result[cur++] = (byte) (0x80 | ((cp >> 12) & 0x3F));
                    _result[cur++] = (byte) (0x80 | ((cp >> 6) & 0x3F));
                    _result[cur++] = (byte) (0x80 | (cp & 0x3F));
                } else {
                    throw new SerializationException("Unknown unicode codepoint in string! " + Integer.toHexString(cp));
                }
            }
        }
        _result[cur] = QUOTE;
        position = cur + 1;
    }

    @SuppressWarnings("deprecation")
    public final void writeAscii(final String str) {
        final int len = str.length();
        if (position + len >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + len);
        }
        str.getBytes(0, len, result, position);
        position += len;
    }

    public final void writeAsciiOrNull(final String str) {
        if (str == null) {
            writeNull();
            return;
        }
        writeAscii(str);
    }

    @SuppressWarnings("deprecation")
    public final void writeAscii(final String str, final int len) {
        if (position + len >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + len);
        }
        str.getBytes(0, len, result, position);
        position += len;
    }

    public final void writeAscii(final byte[] buf) {
        final int len = buf.length;
        if (position + len >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + len);
        }
        final int p = position;
        final byte[] _result = result;
        for (int i = 0; i < buf.length; i++) {
            _result[p + i] = buf[i];
        }
        position += len;
    }

    public final void writeAscii(final byte[] buf, final int len) {
        if (position + len >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + len);
        }
        final int p = position;
        final byte[] _result = result;
        for (int i = 0; i < len; i++) {
            _result[p + i] = buf[i];
        }
        position += len;
    }

    public final void writeBinary(final byte[] buf) {
        if (position + (buf.length << 1) + 2 >= result.length) {
            result = Arrays.copyOf(result, result.length + result.length / 2 + (buf.length << 1) + 2);
        }
        result[position++] = '"';
        position += JsonBase64.encodeToBytes(buf, result, position);
        result[position++] = '"';
    }

    @Override
    public String toString() {
        return new String(result, 0, position, UTF_8);
    }

    public final byte[] toByteArray() {
        return Arrays.copyOf(result, position);
    }

    public final void toStream(final OutputStream stream) throws IOException {
        stream.write(result, 0, position);
    }

    public final byte[] getByteBuffer() {
        return result;
    }

    public final int size() {
        return position;
    }

    public final void reset() {
        position = 0;
    }

    @Override
    public void write(int c) throws IOException {
        if (c < 127) {
            writeByte((byte) c);
        } else {
            write(new char[]{(char) c}, 0, 1);
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        String append = new String(cbuf, off, len);
        writeAscii(append.getBytes(UTF_8));
    }

    @Override
    public void write(String str, int off, int len) {
        String append = str.substring(off, off + len);
        writeAscii(append.getBytes(UTF_8));
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        position = 0;
    }

    /**
     * Custom objects can be serialized based on the implementation specified through this interface.
     * Annotation processor creates custom deserializers at compile time and registers them into DeprecatedDslJson.
     * @param <T> type
     */
    @FunctionalInterface
    public interface Serializer<T> {
        void write(JsonWriter ser, T value);
    }

    public <T extends FastJsonSerializable> void serialize(final T[] array) {
        writeByte(ARRAY_START);
        if (array.length != 0) {
            array[0].serialize(this, false);
            for (int i = 1; i < array.length; i++) {
                writeByte(COMMA);
                array[i].serialize(this, false);
            }
        }
        writeByte(ARRAY_END);
    }

    public <T extends FastJsonSerializable> void serialize(final T[] array, final int len) {
        writeByte(ARRAY_START);
        if (array.length != 0 && len != 0) {
            array[0].serialize(this, false);
            for (int i = 1; i < len; i++) {
                writeByte(COMMA);
                array[i].serialize(this, false);
            }
        }
        writeByte(ARRAY_END);
    }



    public <T> void serialize(final T[] array, final Serializer<T> ser) {
        if (array == null) {
            writeNull();
            return;
        }
        writeByte(ARRAY_START);
        if (array.length != 0) {
            T item = array[0];
            serializeOrNull(item, ser);
            for (int i = 1; i < array.length; i++) {
                writeByte(COMMA);
                item = array[i];
                serializeOrNull(item, ser);
            }
        }
        writeByte(ARRAY_END);
    }

    public <T> void serialize(final List<T> list, final Serializer<T> ser) {
        if (list == null) {
            writeNull();
            return;
        }
        writeByte(ARRAY_START);
        if (list.size() != 0) {
            T item = list.get(0);
            serializeOrNull(item, ser);
            for (int i = 1; i < list.size(); i++) {
                writeByte(COMMA);
                item = list.get(i);
                serializeOrNull(item, ser);
            }
        }
        writeByte(ARRAY_END);
    }

    public <T> void serialize(final Collection<T> collection, final Serializer<T> ser) {
        if (collection == null) {
            writeNull();
            return;
        }
        writeByte(ARRAY_START);
        if (!collection.isEmpty()) {
            final Iterator<T> it = collection.iterator();
            T item = it.next();
            serializeOrNull(item, ser);
            while (it.hasNext()) {
                writeByte(COMMA);
                item = it.next();
                serializeOrNull(item, ser);
            }
        }
        writeByte(ARRAY_END);
    }

    public void serializeObject(final Object value) {
        // todo dynamic ser
        if (value == null) {
            writeNull();
        } else if (unknownSerializer != null) {
            try {
                unknownSerializer.serialize(this, value);
            } catch (IOException ex) { //serializing unknown stuff can fail in various ways ;(
                throw new SerializationException(ex);
            }
        } else {
            throw new SerializationException("Unable to serialize: " + value.getClass() + ".\n" +
                    "Check that JsonWriter was created through DeprecatedDslJson#newWriter.");
        }
    }


    // New methods:

    public void writeField(String name) {
        writeAscii(name);
        writeByte(SEMI);
    }

    public void writeSep() {
       writeByte(COMMA);
    }

    @SuppressWarnings("all")
    public  void serialize(final Iterable<? extends FastJsonSerializable> iter) {
        if (iter == null) {
            writeNull();
            return;
        }
        if (iter instanceof RandomAccess) {
            writeByte(ARRAY_START);
            List<? extends FastJsonSerializable> list = (List) iter;
            int size = list.size();
            if (size != 0) {
                serialize(list.get(0));
                for (int i = 1; i < size; i++) {
                    writeByte(COMMA);
                    serialize(list.get(i));
                }
            }
            writeByte(ARRAY_END);
        } else {
            serialize(iter.iterator());
        }
    }

    public <T> void serialize(final Iterable<T> iter, Serializer<T> ser) {
        if (iter == null) {
            writeNull();
            return;
        }
        if (iter instanceof RandomAccess) {
            writeByte(ARRAY_START);
            List<T> list = (List<T>) iter;
            int size = list.size();
            if (size != 0) {
                serializeOrNull(list.get(0), ser);
                for (int i = 1; i < size; i++) {
                    writeByte(COMMA);
                    serializeOrNull(list.get(i), ser);
                }
            }
            writeByte(ARRAY_END);
        } else {
            serialize(iter.iterator(), ser);
        }
    }

    public void serialize(final Iterator<? extends FastJsonSerializable> iter) {
        if (iter == null) {
            writeNull();
            return;
        }
        writeByte(ARRAY_START);
        if (iter.hasNext()) {
            serialize(iter.next());
            iter.forEachRemaining(v -> {
                writeByte(COMMA);
                serialize(v);
            });
        }
        writeByte(ARRAY_END);
    }

    public <T> void serialize(final Iterator<T> iter, Serializer<T> w) {
        if (iter == null) {
            writeNull();
            return;
        }
        writeByte(ARRAY_START);
        serializeUnwrapped(iter, w);
        writeByte(ARRAY_END);
    }


    public <T> void serializeAsObj(final Iterator<T> iter, Serializer<T> w) {
        if (iter == null) {
            writeNull();
            return;
        }
        writeByte(OBJECT_START);
        serializeUnwrapped(iter, w);
        writeByte(OBJECT_END);
    }

    public <T> void serializeUnwrapped(final Iterator<T> iter, Serializer<T> w) {
        if (iter == null) {
            return;
        }
        if (iter.hasNext()) {
            serializeOrNull(iter.next(), w);
            iter.forEachRemaining(v -> {
                writeByte(COMMA);
                serializeOrNull(v, w);
            });
        }
    }


    public void serialize(FastJsonSerializable o) {
        if (o != null) {
            o.serialize(this, false);
        } else {
            writeNull();
        }
    }

    public void serializeUnwrapped(FastJsonSerializable o) {
        if (o != null) {
            o.serializeUnwrapped(this);
        }
    }


    public <T>  void serializeOrNull(T o, Serializer<T> w) {
        if (o != null) {
            w.write(this, o);
        } else {
            writeNull();
        }
    }
    public void serialize(String value) {
       StringConverter.serializeNullable(value, this);
    }

    public void serialize(Integer value) {
        NumberConverter.serializeNullable(value, this);
    }

    public void serialize(int value) {
        NumberConverter.serialize(value, this);
    }

    public void serialize(Long value) {
        NumberConverter.serializeNullable(value, this);
    }


    public void serialize(long value) {
        NumberConverter.serialize(value, this);
    }

    public void serialize(Double value) {
        NumberConverter.serializeNullable(value, this);
    }


    public void serialize(double value) {
        NumberConverter.serialize(value, this);
    }

    public void serialize(Boolean value) {
        BoolConverter.serializeNullable(value, this);
    }

    public void serialize(boolean value) {
        BoolConverter.serialize(value, this);
    }

    public void serialize(Map<String, ? extends FastJsonSerializable> map) {
        if (map == null) {
            writeNull();
            return;
        }
        serializeAsObj(map.entrySet().iterator(), (w, e) -> {w.writeField(e.getKey()); w.serialize(e.getValue());});
    }

    public void serializeUnwrapped(Map<String, ? extends FastJsonSerializable> map) {
        serializeUnwrapped(map.entrySet().iterator(), (w, e) -> {w.writeField(e.getKey()); w.serialize(e.getValue());});
    }

    public <T> void serialize(Map<String, T> map, Serializer<T> ser) {
        if (map == null) {
            writeNull();
            return;
        }
        serializeAsObj(map.entrySet().iterator(), (w, e) -> {
            w.writeField(e.getKey());
            serializeOrNull(e.getValue(), ser);
        });
    }



    public <K, T> void serialize(Map<K, T> map, Function<K, String> stringer, Serializer<T> ser) {
        if (map == null) {
            writeNull();
            return;
        }
        serializeAsObj(map.entrySet().iterator(), (w, e) -> {
            T val = e.getValue();
            w.writeField(stringer.apply(e.getKey()));
            serializeOrNull(val, ser);
        });
    }



    public <K, T> void serializeUnwrapped(Map<K, T> map, Function<K, String> stringer, Serializer<T> ser) {
        serializeUnwrapped(map.entrySet().iterator(), (w, e) -> {
            T val = e.getValue();
            w.writeField(stringer.apply(e.getKey()));
            serializeOrNull(val, ser);
        });
    }

    public <T> void serializeUnwrapped(Map<String, T> map, Serializer<T> ser) {
        serializeUnwrapped(map, Object::toString, ser);
    }

}
