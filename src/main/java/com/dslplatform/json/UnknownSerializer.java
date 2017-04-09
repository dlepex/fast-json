package com.dslplatform.json;

import github.fastjson.JsonWriter;

import java.io.IOException;

public interface UnknownSerializer {
    void serialize(JsonWriter writer, Object unknown) throws IOException;
}
