package com.spldeolin.allison1875.base.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.spldeolin.allison1875.base.util.CollectionUtils;


public class IgnoreCollectionNullElementDeserializeModule extends SimpleModule {

    private static class CustomizedCollectionDeserializer extends CollectionDeserializer {

        public CustomizedCollectionDeserializer(CollectionDeserializer src) {
            super(src);
        }

        private static final long serialVersionUID = 1L;

        @Override
        public Collection<Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Collection<Object> oldCol = super.deserialize(jp, ctxt);
            Collection<Object> newCol = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(oldCol)) {
                for (Object obj : oldCol) {
                    if (obj != null) {
                        newCol.add(obj);
                    }
                }
            }
            return newCol;
        }

        @Override
        public CollectionDeserializer createContextual(DeserializationContext ctxt, BeanProperty property)
                throws JsonMappingException {
            return new CustomizedCollectionDeserializer(super.createContextual(ctxt, property));
        }

    }

    private static final long serialVersionUID = 1L;

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, CollectionType type,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (deserializer instanceof CollectionDeserializer) {
                    return new CustomizedCollectionDeserializer((CollectionDeserializer) deserializer);
                } else {
                    return super.modifyCollectionDeserializer(config, type, beanDesc, deserializer);
                }
            }
        });
    }

}
