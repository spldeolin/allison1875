package json_schema_traverse_test;

import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.exception.JsonSchemaException;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaGenerateUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-02
 */
@Log4j2
public class JsonSchemaTraverseUtilsTest {

    public static void main(String[] args) throws JsonSchemaException {
        JsonSchema root = JsonSchemaGenerateUtils
                .generateSchema("json_schema_traverse_test.RootDto", JsonSchemaGenerateUtils.DEFAULT_JSG);

        Collection<String> names = Lists.newArrayList();
        Map<JsonSchema, String> pathMap = Maps.newLinkedHashMap();
        Collection<String> paths = Lists.newArrayList();
        Collection<String> types = Lists.newArrayList();

        JsonSchemaTraverseUtils.traverse(root, (propertyName, jsonSchema, parentJsonSchema) -> {
            if (jsonSchema.isArraySchema()) {
                types.add("Array");
            } else {
                String typeName = jsonSchema.getClass().getSimpleName().replace("Schema", "");
                types.add(typeName);
            }

            names.add(propertyName);
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
            pathMap.put(jsonSchema, path + propertyName);
            paths.add(path + propertyName);
        });

        log.info(names);

        System.out.println(names);
        System.out.println(paths);
        System.out.println(types);
    }

}