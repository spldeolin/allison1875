package json_schema_traverse_test;

import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;

/**
 * @author Deolin 2020-08-02
 */
public class JsonSchemaTraverseUtilsTest {

    public static void main(String[] args) throws JsonSchemaException {
        JsonSchema root = JsonSchemaGenerateUtils
                .generateSchema("json_schema_traverse_test.RootDto", JsonSchemaGenerateUtils.DEFAULT_JSG);

        Collection<String> names = Lists.newArrayList();
        Map<JsonSchema, String> pathMap = Maps.newLinkedHashMap();
        Collection<String> paths = Lists.newArrayList();

        Map<String, String> id2Path = Maps.newHashMap();
        id2Path.put(root.getId(), "root");
        JsonSchemaTraverseUtils.traverse("root", root, (propertyName, jsonSchema, parentJsonSchema) -> {
            String path = pathMap.get(parentJsonSchema);
            if (path == null) {
                path = "";
            } else {
                if (parentJsonSchema.isArraySchema()) {
                    path += "[]";
                }
                if (parentJsonSchema.isObjectSchema()) {
                    path += ".";
                }
            }
            path = path + propertyName;
            pathMap.put(jsonSchema, path);

            if (jsonSchema.getId() != null) {
                id2Path.put(jsonSchema.getId(), path);
            }

            names.add(propertyName);
            paths.add(path);

            if (jsonSchema instanceof ReferenceSchema) {
                System.out.println(path + "的数据结构与" + id2Path.get(jsonSchema.get$ref()) + "一致");
            }

        });

        System.out.println(names);
        System.out.println(paths);
    }

}