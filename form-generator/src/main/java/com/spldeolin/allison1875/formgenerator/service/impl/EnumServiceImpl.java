package com.spldeolin.allison1875.formgenerator.service.impl;

import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseBodyDeclaration;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseMethodDeclaration;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.config.DomainContext;
import com.spldeolin.allison1875.common.service.AnnotationExprService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.OptionDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.service.EnumService;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
public class EnumServiceImpl implements EnumService {

    @Inject
    private Config config;

    @Inject
    private AnnotationExprService annotationExprService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void generateEnums(List<FormDef> forms) {
        for (FormDef form : forms) {

            // 为单选和多选生成枚举
            for (ItemDef item : form.getItems()) {
                if (item.getType() != ItemType.MULTI_SELECT && item.getType() != ItemType.SELECT) {
                    continue;
                }

                // 枚举名防重
                String enumName = StringUtils.capitalize(item.getName()) + "Enum";
                Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(
                        DomainContext.get().getEnumSourceRoot(), DomainContext.get().getEnumPackage(),
                        enumName + ".java");
                // 暂不考虑重名，因为这次处理重名会导致与getJavaTypeInDTO方法的返回值对不上
//                absulutePath = antiDuplicationService.getNewPathIfExist(absulutePath);
                enumName = absulutePath.getFileName().toString().replaceFirst("\\.[^.]+$", "");

                // 枚举
                CompilationUnit cu = new CompilationUnit();
                cu.setPackageDeclaration(DomainContext.get().getEnumPackage());
                EnumDeclaration ed = new EnumDeclaration();
                JavadocUtils.setJavadoc(ed, item.getTitle(), config.getAuthor() + " " + LocalDate.now());
                ed.addAnnotation(annotationExprService.lombokGetter());
                ed.addAnnotation(annotationExprService.lombokAllArgsConstructor());
                ed.setPublic(true);
                ed.setName(enumName);

                // 枚举项
                for (OptionDef option : getOptions(item)) {
                    EnumConstantDeclaration ecd = new EnumConstantDeclaration().setName(option.javaEnumConstantName())
                            .addArgument(new StringLiteralExpr(option.getCode()))
                            .addArgument(new StringLiteralExpr(option.getTitle()));
                    ed.addEntry(ecd);
                }

                // 枚举其他成员
                ed.addMember(parseBodyDeclaration(
                        "@com.fasterxml.jackson.annotation.JsonValue private final String code;"));
                ed.addMember(parseBodyDeclaration("private final String title;"));
                ed.addMember(parseMethodDeclaration(
                        "public static boolean valid(String code) { return Arrays.stream(values())"
                                + ".anyMatch(anEnum -> anEnum.getCode().equals(code)); }")
                        .setJavadocComment("判断参数code是否是一个有效的枚举"));
                ed.addMember(parseMethodDeclaration(
                        "@com.fasterxml.jackson.annotation.JsonCreator public static %s of(String code) { "
                                + "return Arrays.stream(values()).filter(anEnum -> anEnum.getCode()"
                                + ".equals(code)).findFirst().orElse(null); }", enumName)
                        .setJavadocComment("获取code对应的枚举"));
                ed.addMember(parseMethodDeclaration(
                        "@Override public String toString() { return code; }"));
                cu.addType(ed);
                cu.setStorage(absulutePath);
                importExprService.extractQualifiedTypeToImport(cu);
                cu.addImport("java.util.Arrays");
                CompilationUnitUtils.writeJava(cu);
            }

            // 为表单生成字段枚举
            // 枚举名防重
            String enumName = StringUtils.capitalize(form.getName()) + "SortEnum";
            Path absulutePath = CodeGenerationUtils.fileInPackageAbsolutePath(DomainContext.get().getEnumSourceRoot(),
                    DomainContext.get().getEnumPackage(), enumName + ".java");
            // 暂不考虑重名，因为这次处理重名会导致与getJavaTypeInDTO方法的返回值对不上
//                absulutePath = antiDuplicationService.getNewPathIfExist(absulutePath);
            enumName = absulutePath.getFileName().toString().replaceFirst("\\.[^.]+$", "");

            // 枚举
            CompilationUnit cu = new CompilationUnit();
            cu.setPackageDeclaration(DomainContext.get().getEnumPackage());
            EnumDeclaration ed = new EnumDeclaration();
            JavadocUtils.setJavadoc(ed, form.getTitle() + "的排序字段", config.getAuthor() + " " + LocalDate.now());
            ed.addAnnotation(annotationExprService.lombokGetter());
            ed.addAnnotation(annotationExprService.lombokAllArgsConstructor());
            ed.setPublic(true);
            ed.setName(enumName);

            // 枚举项（仅可排序类型）
            for (ItemDef item : form.getItems()) {
                if (item.getType() != ItemType.NUMBER && item.getType() != ItemType.ON_OFF
                        && item.getType() != ItemType.TEXT && item.getType() != ItemType.TIME) {
                    continue;
                }
                EnumConstantDeclaration ecd = new EnumConstantDeclaration().setName(
                                MoreStringUtils.camelToSnakeCase(item.getName()).toUpperCase())
                        .addArgument(new StringLiteralExpr(item.getName()))
                        .addArgument(new StringLiteralExpr("按“" + item.getTitle() + "”排序"));
                ed.addEntry(ecd);
            }

            // 枚举其他成员
            ed.addMember(parseBodyDeclaration(
                    "@com.fasterxml.jackson.annotation.JsonValue private final String code;"));
            ed.addMember(parseBodyDeclaration("private final String title;"));
            ed.addMember(parseMethodDeclaration(
                    "public static boolean valid(String code) { return Arrays.stream(values())"
                            + ".anyMatch(anEnum -> anEnum.getCode().equals(code)); }")
                    .setJavadocComment("判断参数code是否是一个有效的枚举"));
            ed.addMember(parseMethodDeclaration(
                    "@com.fasterxml.jackson.annotation.JsonCreator public static %s of(String code) { "
                            + "return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code))"
                            + ".findFirst().orElse(null); }", enumName)
                    .setJavadocComment("获取code对应的枚举"));
            ed.addMember(parseMethodDeclaration(
                    "@Override public String toString() { return code; }"));
            cu.addType(ed);
            cu.setStorage(absulutePath);
            importExprService.extractQualifiedTypeToImport(cu);
            cu.addImport("java.util.Arrays");
            CompilationUnitUtils.writeJava(cu);
        }
    }

    private List<OptionDef> getOptions(ItemDef item) {
        if (item instanceof MultiSelectItemDef) {
            return ((MultiSelectItemDef) item).getOptions();
        }
        if (item instanceof SelectItemDef) {
            return ((SelectItemDef) item).getOptions();
        }
        throw new RuntimeException("impossible unless bug");
    }

}
