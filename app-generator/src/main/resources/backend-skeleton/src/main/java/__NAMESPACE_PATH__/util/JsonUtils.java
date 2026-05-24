package __NAMESPACE__.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper om = createObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.setTimeZone(TimeZone.getDefault());
        om.registerModule(java8timeSimplePattern());
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return om;
    }

    public static String toJson(Object object) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new RuntimeException(e);
        }
    }

    public static String toJsonPrettily(Object object) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> toListOfObject(String json, Class<T> clazz) {
        try {
            CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
            return om.readValue(json, collectionType);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference) {
        try {
            return om.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("json={}, typeReference={}", json, typeReference, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toTree(String json) {
        try {
            return om.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("json={}", json, e);
            throw new RuntimeException(e);
        }
    }

    private static SimpleModule java8timeSimplePattern() {
        SimpleModule module = new JavaTimeModule();
        DateTimeFormatter ldtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter ldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter ltFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ldtFormatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ldtFormatter))
                .addSerializer(LocalDate.class, new LocalDateSerializer(ldFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(ldFormatter))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(ltFormatter))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(ltFormatter));
        return module;
    }

}
