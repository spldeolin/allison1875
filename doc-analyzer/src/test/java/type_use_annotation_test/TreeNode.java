package type_use_annotation_test;

import java.util.Collection;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ObjectMapperUtils;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;

/**
 * @author Deolin 2020-07-25
 */
public class TreeNode {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(TreeNode.class);

    @NotNull
    private Long id;

    @NotBlank
    private String title;

    private Collection<@NotBlank TreeNode>[][] children;

    public TreeNode() {
    }

    public static void main(String[] args) throws JsonSchemaException, JsonProcessingException {
        ObjectMapper om = ObjectMapperUtils.initDefault(new ObjectMapper());
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
                .generateSchema("type_use_annotation_test.TreeNode", new JsonSchemaGenerator(om));

        JsonNode jsonNode = om.readTree(JsonUtils.toJson(jsonSchema));
        log.info(JsonUtils.toJson(jsonSchema));
    }

    public @NotNull Long getId() {
        return this.id;
    }

    public @NotBlank String getTitle() {
        return this.title;
    }

    public Collection<@NotBlank TreeNode>[][] getChildren() {
        return this.children;
    }

    public void setId(@NotNull Long id) {
        this.id = id;
    }

    public void setTitle(@NotBlank String title) {
        this.title = title;
    }

    public void setChildren(Collection<@NotBlank TreeNode>[][] children) {
        this.children = children;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TreeNode)) {
            return false;
        }
        final TreeNode other = (TreeNode) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) {
            return false;
        }
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) {
            return false;
        }
        return java.util.Arrays.deepEquals(this.getChildren(), other.getChildren());
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TreeNode;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getChildren());
        return result;
    }

    public String toString() {
        return "TreeNode(id=" + this.getId() + ", title=" + this.getTitle() + ", children=" + java.util.Arrays
                .deepToString(this.getChildren()) + ")";
    }

}