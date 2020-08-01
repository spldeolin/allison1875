package type_use_annotation_test;

import java.util.Collection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import lombok.Data;

/**
 * @author Deolin 2020-07-25
 */
@Data
public class TreeNode {

    @NotNull
    private Long id;

    @NotBlank
    private String title;

    private Collection<@NotBlank TreeNode>[][] children;

    public static void main(String[] args) throws JsonSchemaException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        om.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            private static final long serialVersionUID = 4963154137655948984L;

            @Override
            public Boolean hasRequiredMarker(AnnotatedMember m) {
                boolean isNotNull = super._findAnnotation(m, NotNull.class) != null;
                boolean isNotEmpty = super._findAnnotation(m, NotEmpty.class) != null;
                boolean isNotBlank = super._findAnnotation(m, NotBlank.class) != null;
                return isNotNull || isNotEmpty || isNotBlank;
            }
        });

        JsonSchema jsonSchema = JsonSchemaGenerateUtils
                .generateSchema("com.spldeolin.allison1875.docanalyzer.processor.demo.TreeNode",
                        new JsonSchemaGenerator(om));

        JsonNode jsonNode = om.readTree(JsonUtils.toJson(jsonSchema));
        System.out.println(JsonUtils.toJson(jsonSchema));
    }

}