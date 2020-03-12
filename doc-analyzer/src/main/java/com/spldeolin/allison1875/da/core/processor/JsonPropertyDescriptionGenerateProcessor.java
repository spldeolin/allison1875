package com.spldeolin.allison1875.da.core.processor;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.Jsons;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.da.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.core.enums.NumberFormatTypeEnum;
import com.spldeolin.allison1875.da.core.enums.StringFormatTypeEnum;

/**
 * @author Deolin 2020-03-10
 */
class JsonPropertyDescriptionGenerateProcessor {

    void process() {
        StaticAstContainer.getCompilationUnits().forEach(cu -> {
            cu.findAll(ClassOrInterfaceDeclaration.class, this::isPojo)
                    .forEach(pojo -> pojo.getFields().forEach(field -> {
                        BodyFieldDefinition bodyField = buildBodyFieldDefinition(field);
                        String content = StringEscapeUtils.escapeJava(Jsons.toJson(bodyField));
                        field.getAnnotationByName("JsonPropertyDescription").ifPresent(AnnotationExpr::remove);
                        field.addAnnotation(
                                StaticJavaParser.parseAnnotation(f("@JsonPropertyDescription(\"%s\")", content)));
                    }));

            cu.addImport(QualifierConstants.JsonPropertyDescription);
            Saves.prettySave(cu);
        });
    }

    private BodyFieldDefinition buildBodyFieldDefinition(FieldDeclaration field) {
        BodyFieldDefinition bodyField = new BodyFieldDefinition();

        String description = Javadocs.extractFirstLine(field);
        bodyField.setDescription(description);

        boolean nullable = true;
        if (field.getAnnotationByName("NotNull").isPresent() || field.getAnnotationByName("NotEmpty").isPresent()
                || field.getAnnotationByName("NotBlank").isPresent()) {
            nullable = false;
        }
        bodyField.setNullable(nullable);

        ValidatorProcessor validatorProcessor = new ValidatorProcessor().nodeWithAnnotations(field).process();
        bodyField.setValidators(validatorProcessor.validators());

        String stringFormat = calcStringFormat(field);
        bodyField.setStringFormat(stringFormat);

        NumberFormatTypeEnum numberFormat = calcNumberFormat(field.getCommonType().asString());
        bodyField.setNumberFormat(numberFormat);

        return bodyField;
    }

    private boolean isPojo(ClassOrInterfaceDeclaration coid) {
        if (coid.isInterface()) {
            return false;
        }
        if (coid.getFullyQualifiedName()
                .filter(qualifier -> qualifier.contains("dto") || qualifier.contains("vo") || qualifier
                        .contains("entity")).isPresent()) {
            return true;
        }
        return !coid.isInterface() && coid.getAnnotations().stream()
                .anyMatch(anno -> StringUtils.equalsAny(anno.getNameAsString(), "Data", "Getter", "Setter"));
    }

    private String calcStringFormat(FieldDeclaration field) {
        StringBuilder sb = new StringBuilder(64);
        field.getAnnotationByName("JsonFormat")
                .ifPresent(jsonFormat -> jsonFormat.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                    if (pair.getNameAsString().equals("pattern")) {
                        sb.append(String.format(StringFormatTypeEnum.datetime.getValue(), pair.getValue()));
                    }
                }));
        if (sb.length() == 0) {
            sb.append(StringFormatTypeEnum.normal.getValue());
        }
        return sb.toString();
    }

    private NumberFormatTypeEnum calcNumberFormat(String javaTypeName) {
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Float", "Double", "BigDecimal")) {
            return NumberFormatTypeEnum.f1oat;
        } else if (StringUtils.equalsAnyIgnoreCase(javaTypeName, "Long", "BigInteger")) {
            return NumberFormatTypeEnum.int64;
        } else {
            return NumberFormatTypeEnum.int32;
        }
    }

}
