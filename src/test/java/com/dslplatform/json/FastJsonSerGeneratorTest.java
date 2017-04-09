package com.dslplatform.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FastJsonSerGeneratorTest {

    public static class Pigo implements FastJsonSerializable  {
        public int z;
        public int z2;


        @Override
        public void serializeFieldsOnly(JsonWriter jw) {
            jw.writeField("z"); jw.serialize(this.z); jw.writeSep();
            jw.writeField("z2"); jw.serialize(this.z2);
        }
    }

    public static class Pojo implements FastJsonSerializable {
        public int a;
        public double b;
        @JsonIgnore
        public double b1;

        public Map<String, String> dictionary;

        @JsonUnwrapped
        public Pigo emedded;

        public List<Pigo> pigose;


        @JsonProperty("") @JsonIgnore
        public transient String myStringField;

        @Override
        public void serializeFieldsOnly(JsonWriter jw) {
            jw.writeField("a"); jw.serialize(this.a); jw.writeSep();
            jw.writeField("b"); jw.serialize(this.b); jw.writeSep();
            if (this.dictionary != null) { jw.writeField("dictionary"); jw.serialize(this.dictionary, JsonWriter::serialize); jw.writeSep();}
            if (this.emedded != null) { jw.serializeFieldsOnly(this.emedded); jw.writeSep();}
            if (this.pigose != null) { jw.writeField("pigose"); jw.serialize(this.pigose);}
        }
    }

    FastJsonSerGenerator gen = new FastJsonSerGenerator();

    @org.junit.Test
    public void test() {
        //gen.writeNulls = true;
        System.out.println(gen.generate(Pojo.class));




    }

    @Test
    public void test1() {
        Pojo pojo = new Pojo();
        pojo.a = 1;
        pojo.b = Double.MAX_VALUE;
        pojo.myStringField = "myStringField \nmyStringField \n\t";

        JsonWriter jw = new JsonWriter(10, null);




        Pigo p = new Pigo();

        p.z = 1;
        pojo.pigose = Arrays.asList(p, null);
        pojo.emedded = p;
        pojo.dictionary = new HashMap<>();
        pojo.dictionary.put("fix", null);
        pojo.dictionary.put("key", "val");



        pojo.serialize(jw, false);


        System.out.println(jw.toString() + " " + jw.size());
    }

}