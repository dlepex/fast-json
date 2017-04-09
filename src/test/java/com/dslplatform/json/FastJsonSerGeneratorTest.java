package com.dslplatform.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import github.fastjson.FastJsonSerGenerator;
import github.fastjson.FastJsonSerializable;
import github.fastjson.JsonWriter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FastJsonSerGeneratorTest {


    public static final class Pojo implements FastJsonSerializable {
        public int a;
        public double b;
        @JsonIgnore
        public double b1;

        public Map<String, String> dict;
        @JsonProperty @JsonUnwrapped
        private Map<String, String> dictu;

        @JsonUnwrapped
        public OtherPojo emedded;

        public List<OtherPojo> pojos;


        @JsonProperty("class") @JsonIgnore
        public transient String myStringField;

        @Override
        public void serializeUnwrapped(JsonWriter jw) {
            jw.writeField("a"); jw.serialize(this.a); jw.writeSep();
            jw.writeField("b"); jw.serialize(this.b); jw.writeSep();
            if (this.dict != null) { jw.writeField("dict"); jw.serialize(this.dict, JsonWriter.serOfString); jw.writeSep();}
            if (this.dictu != null) { jw.serializeUnwrapped(this.dictu, JsonWriter.serOfString); jw.writeSep();}
            if (this.emedded != null) { jw.serializeUnwrapped(this.emedded); jw.writeSep();}
            if (this.pojos != null) { jw.writeField("pojos"); jw.serialize(this.pojos);}

        }
    }

    public static final class OtherPojo implements FastJsonSerializable  {
        public int z;
        public int z2;


        @Override
        public void serializeUnwrapped(JsonWriter jw) {
            jw.writeField("z"); jw.serialize(this.z); jw.writeSep();
            jw.writeField("z2"); jw.serialize(this.z2);
        }
    }

    FastJsonSerGenerator gen = new FastJsonSerGenerator();

    void gen(Class<?> pojoClass) {
        System.out.println("\n"+ pojoClass.getSimpleName() + " ================================\n");
        System.out.println(gen.generate(pojoClass));
    }

    @Test
    public void genJsonSerMethods() { // not a test really
        gen(Pojo.class);
        gen(OtherPojo.class);
    }

    @Test
    public void test1() {
        Pojo pojo = new Pojo();
        pojo.a = 1;
        pojo.b = Double.MAX_VALUE;
        pojo.myStringField = "myStringField \nmyStringField \n\t";
        byte[] myBufferProbablyThreadLocal = new byte[8192];
        JsonWriter jw = JsonWriter.create(myBufferProbablyThreadLocal);
        OtherPojo p = new OtherPojo();
        p.z = 1;
        pojo.pojos = Arrays.asList(p, null);
        pojo.emedded = p;
        pojo.dict = new HashMap<>();
        pojo.dict.put("key_1", null);
        pojo.dict.put("key_2", "val");
        pojo.dictu = pojo.dict;

        jw.serialize(pojo);

        System.out.println(jw.toString() + " " + jw.size());
    }

}