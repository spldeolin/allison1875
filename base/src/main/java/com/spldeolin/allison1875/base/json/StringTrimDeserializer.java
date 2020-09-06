package com.spldeolin.allison1875.base.json;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

/**
 * @author Deolin 2020-09-06
 */
public class StringTrimDeserializer extends StringDeserializer {

    private static final long serialVersionUID = -2622411209321638082L;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.getValueAsString().trim();
    }

}