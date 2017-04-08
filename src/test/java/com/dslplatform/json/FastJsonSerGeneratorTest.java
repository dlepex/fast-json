package com.dslplatform.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


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

        @JsonUnwrapped
        public Pigo emedded;

        public List<Pigo> pigose;


        @JsonProperty("thisIsC")
        private String myStringField;

        @Override
        public void serializeFieldsOnly(JsonWriter jw) {
            jw.writeField("a"); jw.serialize(this.a); jw.writeSep();
            jw.writeField("b"); jw.serialize(this.b); jw.writeSep();
            if (this.emedded != null) { this.emedded.serializeFieldsOnly(jw); jw.writeSep();}
            if (this.pigose != null) { jw.writeField("pigose"); jw.serialize(this.pigose); jw.writeSep();}
            if (this.myStringField != null) { jw.writeField("thisIsC"); jw.serialize(this.myStringField);}
        }
    }

    @org.junit.Test
    public void test() {
        System.out.println(FastJsonSerGenerator.generateFieldsSer(Pojo.class, true, false));
        System.out.println(FastJsonSerGenerator.generateFieldsSer(Pigo.class, true, false));
        System.out.println(FastJsonSerGenerator.generateFieldsSer(Pojo.class, false, false));



    }

    @Test
    public void test1() {
        Pojo pojo = new Pojo();
        pojo.a = 1;
        pojo.b = 2;
        pojo.myStringField = "myStringField \nmyStringField \n\t";

        JsonWriter jw = new JsonWriter(10, null);




        Pigo p = new Pigo();

        p.z = 1;
        pojo.pigose = Arrays.asList(p, null);
        pojo.emedded = p;
        pojo.serialize(jw, false);


        System.out.println(jw.toString() + " " + jw.size());
    }

}