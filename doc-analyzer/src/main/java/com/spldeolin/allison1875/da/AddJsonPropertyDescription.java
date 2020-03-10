package com.spldeolin.allison1875.da;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.utils.StringEscapeUtils;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.util.Jsons;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.da.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.core.enums.StringFormatTypeEnum;
import com.spldeolin.allison1875.da.core.processor.ValidatorProcessor;

/**
 * @author Deolin 2020-03-10
 */
public class AddJsonPropertyDescription {

    public static void main(String[] args) {
        StaticAstContainer.getCompilationUnits().forEach(cu -> {
            MutableBoolean update = new MutableBoolean(false);
            cu.findAll(ClassOrInterfaceDeclaration.class, coid -> coid.getAnnotationByName("Data").isPresent())
                    .forEach(pojo -> {
                        pojo.getFields().forEach(field -> {
                            BodyFieldDefinition bodyField = new BodyFieldDefinition();

                            String description = Javadocs.extractFirstLine(field);
                            bodyField.setDescription(description);

                            boolean nullable = true;
                            if (field.getAnnotationByName("NotNull").isPresent() || field
                                    .getAnnotationByName("NotEmpty").isPresent() || field
                                    .getAnnotationByName("NotBlank").isPresent()) {
                                nullable = false;
                            }
                            bodyField.setNullable(nullable);

                            ValidatorProcessor validatorProcessor = new ValidatorProcessor().nodeWithAnnotations(field)
                                    .process();
                            bodyField.setValidators(validatorProcessor.validators());

                            field.getAnnotationByName("JsonFormat").ifPresent(
                                    jsonFormat -> jsonFormat.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                                        if (pair.getNameAsString().equals("pattern")) {
                                            bodyField.setStringFormat(
                                                    String.format(StringFormatTypeEnum.datetime.getValue(),
                                                            pair.getValue()));
                                        }
                                    }));
                            if (addJsonPropertyDescriptionByJavadoc(field, Jsons.toJson(bodyField))) {
                                update.setTrue();
                            }
                        });

                        pojo.getMethods().forEach(method -> {
                            BodyFieldDefinition bodyField = new BodyFieldDefinition();

                            String description = Javadocs.extractFirstLine(method);
                            bodyField.setDescription(description);

                            if (addJsonPropertyDescriptionByJavadoc(method, Jsons.toJson(bodyField))) {
                                update.setTrue();
                            }
                        });
                    });

            if (update.isTrue()) {
                try {
                    cu.addImport("com.fasterxml.jackson.annotation.JsonPropertyDescription");
                    cu.getStorage().ifPresent(Storage::save);
                } catch (Exception e) {
                    System.out.println(1);
                }
            }
        });

    }

    private static boolean addJsonPropertyDescriptionByJavadoc(NodeWithAnnotations<?> node, String content) {
        if (!node.getAnnotationByName("JsonPropertyDescription").isPresent()) {
            if (StringUtils.isNotBlank(content)) {
                content = StringEscapeUtils.escapeJava(content);
                node.addAnnotation(StaticJavaParser.parseAnnotation(f("@JsonPropertyDescription(\"%s\")", content)));
                return true;
            }
        }
        return false;
    }

}
