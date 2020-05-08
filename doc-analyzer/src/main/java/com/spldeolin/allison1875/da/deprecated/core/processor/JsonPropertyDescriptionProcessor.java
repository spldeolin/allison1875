package com.spldeolin.allison1875.da.deprecated.core.processor;

import static com.github.javaparser.utils.CodeGenerationUtils.f;
import static com.spldeolin.allison1875.da.deprecated.DocAnalyzerConfig.CONFIG;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.utils.StringEscapeUtils;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.da.deprecated.core.definition.BodyFieldDefinition;
import com.spldeolin.allison1875.da.deprecated.core.enums.NumberFormatTypeEnum;
import com.spldeolin.allison1875.da.deprecated.core.enums.StringFormatTypeEnum;
import lombok.extern.log4j.Log4j2;

/**
 * 1. 生成@JsonPropertyDescription
 * 2. 重新mvn clean package
 * 3. 刷新WarOrFatJarClassLoaderFactory缓存、刷新StaticAstContainer
 *
 * @author Deolin 2020-03-10
 */
@Log4j2
@Deprecated
class JsonPropertyDescriptionProcessor {

    void process() {
        if (StringUtils.isEmpty(CONFIG.getMavenPackageCommandLine())) {
            return;
        }

        long start = System.currentTimeMillis();
        AstForest.getInstance().forEach(cu -> {
            MutableBoolean save = new MutableBoolean(false);
            cu.findAll(ClassOrInterfaceDeclaration.class, this::isJavabean)
                    .forEach(javabean -> javabean.getFields().forEach(field -> {
                        BodyFieldDefinition bodyField = buildBodyFieldDefinition(field);
                        String content = StringEscapeUtils.escapeJava(JsonUtils.toJson(bodyField));
                        field.getAnnotationByName("JsonPropertyDescription").ifPresent(AnnotationExpr::remove);
                        field.addAnnotation(
                                StaticJavaParser.parseAnnotation(f("@JsonPropertyDescription(\"%s\")", content)));
                        save.setTrue();
                    }));
            if (save.isTrue()) {
                cu.addImport(QualifierConstants.JSON_PROPERTY_DESCRIPTION);
                Saves.prettySave(cu);
            }
        });
        log.info("Add JsonPropertyDescription for javabean complete with elapsing {}ms.",
                System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        new MavenPackageProcessor().process();
        log.info("Repackage maven project complete with elapsing {}ms.", System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        log.info("Refresh WarOrFatJarClassLoader and AstContainer complete with elapsing {}ms.",
                System.currentTimeMillis() - start);
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

//        ValidatorProcessor validatorProcessor = new ValidatorProcessor().nodeWithAnnotations(field).process();
//        bodyField.setValidators(validatorProcessor.validators());

        String stringFormat = calcStringFormat(field);
        bodyField.setStringFormat(stringFormat);

        NumberFormatTypeEnum numberFormat = calcNumberFormat(field.getCommonType().asString());
        bodyField.setNumberFormat(numberFormat);

        return bodyField;
    }

    private boolean isJavabean(ClassOrInterfaceDeclaration coid) {
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
