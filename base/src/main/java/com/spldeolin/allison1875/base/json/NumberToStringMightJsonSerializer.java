package com.spldeolin.allison1875.base.json;

import java.io.IOException;
import java.util.Collection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

/**
 * @author Deolin 2020-09-06
 */
public class NumberToStringMightJsonSerializer extends JsonSerializer<Number> {

    private final Collection<ClassAndField> classAndFields = Lists.newArrayList();

    public NumberToStringMightJsonSerializer registerIgnoreProperty(String className, String field) {
        classAndFields.add(new ClassAndField(className, field));
        return this;
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonStreamContext outputContext = gen.getOutputContext();
        String className = outputContext.getCurrentValue().getClass().getName();
        String fieldName = outputContext.getCurrentName();
        if (classAndFields.stream()
                .anyMatch(caf -> className.equals(caf.className) && fieldName.equals(caf.fieldName))) {
            gen.writeNumber(value.toString());
        } else {
            gen.writeString(value.toString());
        }
    }

    @AllArgsConstructor
    private static class ClassAndField {

        private final String className;

        private final String fieldName;

    }

}