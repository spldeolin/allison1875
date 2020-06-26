package com.spldeolin.allison1875.handlergenerator;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.creator.CuCreator;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.PackageAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.JavadocTags;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.handlergenerator.strategy.DefaultStrategyAggregation;
import com.spldeolin.allison1875.handlergenerator.strategy.HandlerStrategy;
import com.spldeolin.allison1875.handlergenerator.strategy.PackageStrategy;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-06-22
 */
@Accessors(chain = true)
public class MainProcessor {

    @Setter
    private HandlerStrategy handlerStrategy = new DefaultStrategyAggregation();

    @Setter
    private PackageStrategy packageStrategy = new DefaultStrategyAggregation();

    {
        String handler = "getUser", service = "UserService", author = "Deolin 2020";
        String desc = "获取用户信息";
        {
            Long userId;
        }
        {
            String userNames[], nickName;
            Integer age, no;
            {
                String dtos = "address";
                String province, city;
                Integer areaCode;
            }
            {
                String dto = "id";
                Long id;
                {
                    String dto$ = "name";
                    String name;
                }
            }
        }
    }

    public void process() {
        for (CompilationUnit cu : AstForest.getInstance()) {
            Multimap<ClassOrInterfaceDeclaration, HandlerMetaInfo> handlerMetaInfos = ArrayListMultimap.create();
            for (ClassOrInterfaceDeclaration coid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                // if (Annotations.isAnnotationAbsent(coid, "org.springframework.web.bind.annotation.RestController")) {
                // continue;
                // }
                ClassOrInterfaceDeclaration controller = coid;
                String controllerPackage = cu.getPackageDeclaration().orElseThrow(PackageAbsentException::new)
                        .getNameAsString();
                for (BodyDeclaration<?> member : controller.getMembers()) {
                    if (!member.isInitializerDeclaration()) {
                        continue;
                    }
                    InitializerDeclaration init = member.asInitializerDeclaration();
                    // handler meta
                    HandlerMetaInfo handlerBuilder = new HandlerMetaInfo();
                    for (Statement stmtInInit : init.getBody().getStatements()) {
                        stmtInInit.ifExpressionStmt(
                                exprInInit -> exprInInit.getExpression().ifVariableDeclarationExpr(vde -> {
                                    for (VariableDeclarator vd : vde.getVariables()) {
                                        vd.getInitializer().ifPresent(i -> {
                                            String value = i.asStringLiteralExpr().getValue();
                                            switch (vd.getNameAsString()) {
                                                case "handler":
                                                    handlerBuilder.handlerName(value);
                                                    break;
                                                case "desc":
                                                    handlerBuilder.handlerDescription(value);
                                                    break;
                                                case "service":
                                                    handlerBuilder.serviceName(value);
                                                    break;
                                                case "author":
                                                    handlerBuilder.author(value);
                                                    break;
                                            }
                                        });
                                    }
                                }));
                    }
                    // dto meta
                    Map<BlockStmt, DtoMetaInfo> dtos = Maps.newLinkedHashMap();
                    List<BlockStmt> allBlockStmt = init.findAll(BlockStmt.class);
                    allBlockStmt.remove(0);
                    for (int i = 0; i < allBlockStmt.size(); i++) {
                        BlockStmt blockStmt = allBlockStmt.get(i);
                        DtoMetaInfo dtoBuilder = new DtoMetaInfo();
                        for (VariableDeclarationExpr vde : BlockStmts
                                .listExpressions(blockStmt, VariableDeclarationExpr.class)) {
                            for (VariableDeclarator vd : vde.getVariables()) {
                                String variableName = removeLastDollars(vd.getNameAsString());
                                if (StringUtils.equalsAny(variableName, "dto", "dtos")) {
                                    vd.getInitializer().ifPresent(ir -> {
                                        String rawDtoName = ir.asStringLiteralExpr().getValue();
                                        dtoBuilder.typeName(StringUtils.upperFirstLetter(rawDtoName) + "Dto");
                                        dtoBuilder.packageName(packageStrategy.calcDtoPackage(controllerPackage));
                                        dtoBuilder
                                                .typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                                        String asVariableDeclarator;
                                        String dtoName;
                                        if ("dtos".equals(vd.getNameAsString())) {
                                            dtoName = English.plural(rawDtoName);
                                            asVariableDeclarator =
                                                    "Collection<" + dtoBuilder.typeName() + "> " + dtoName;
                                        } else {
                                            dtoName = rawDtoName;
                                            asVariableDeclarator = dtoBuilder.typeName() + " " + dtoName;
                                        }
                                        dtoBuilder.dtoName(dtoName);
                                        dtoBuilder.asVariableDeclarator(asVariableDeclarator);
                                    });
                                } else {
                                    String standradVd = standardizeVd(vd);
                                    dtoBuilder.variableDeclarators().add(standradVd);
                                }
                            }
                        }
                        if (dtoBuilder.typeName() == null) {
                            if (i == 0) {
                                dtoBuilder.typeName(StringUtils.upperFirstLetter(handlerBuilder.handlerName()) + "Req");
                                dtoBuilder.packageName(packageStrategy.calcReqPackage(controllerPackage));
                                dtoBuilder.typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                                dtoBuilder.dtoName("req");
                                handlerBuilder.reqBodyDto(dtoBuilder);
                            } else {
                                dtoBuilder
                                        .typeName(StringUtils.upperFirstLetter(handlerBuilder.handlerName()) + "Resp");
                                dtoBuilder.packageName(packageStrategy.calcRespPackage(controllerPackage));
                                dtoBuilder.typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                                dtoBuilder.dtoName("resp");
                                handlerBuilder.respBodyDto(dtoBuilder);
                            }
                            handlerBuilder.imports().add(dtoBuilder.typeQualifier());
                            dtoBuilder.asVariableDeclarator(dtoBuilder.typeName() + " " + dtoBuilder.dtoName());
                        }
                        dtos.put(blockStmt, dtoBuilder);
                    }
                    handlerBuilder.dtos(dtos.values());
                    dtos.forEach((blockStmt, dtoBuilder) -> blockStmt.getParentNode().filter(dtos::containsKey)
                            .ifPresent(parentBlock -> {
                                DtoMetaInfo parentMeta = dtos.get(parentBlock);
                                parentMeta.variableDeclarators().add(dtoBuilder.asVariableDeclarator());
                                parentMeta.imports().add(dtoBuilder.typeQualifier());
                            }));
                    System.out.println(handlerBuilder);
                    outputDtos(cu, dtos.values());
                    handlerMetaInfos.put(controller, handlerBuilder);
                }
            }
            handlerMetaInfos.forEach(this::outputHandler);
            Saves.prettySave(cu);
        }
    }

    private void outputHandler(ClassOrInterfaceDeclaration controller, HandlerMetaInfo metaInfo) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        cu.addImport(metaInfo.reqBodyDto().typeQualifier());
        cu.addImport(metaInfo.respBodyDto().typeQualifier());
        MethodDeclaration handler = new MethodDeclaration();
        Javadoc javadoc = new JavadocComment(metaInfo.handlerDescription()).parse();
        if (StringUtils.isNotBlank(metaInfo.author())) {
            boolean noneMatch = JavadocTags.getEveryLineByTag(controller, JavadocBlockTag.Type.AUTHOR).stream()
                    .noneMatch(line -> line.contains(metaInfo.author()));
            if (noneMatch) {
                javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.AUTHOR, metaInfo.author()));
            }
        }
        handler.setJavadocComment(javadoc);
        handler.addAnnotation(
                StaticJavaParser.parseAnnotation("@PostMapping(value=\"/" + metaInfo.handlerName() + "\")"));
        handler.setPublic(true);
        handler.setType(handlerStrategy.resolveReturnType(metaInfo));
        handler.setName(metaInfo.handlerName());
        Parameter requestBody = StaticJavaParser.parseParameter(metaInfo.reqBodyDto().asVariableDeclarator());
        requestBody.addAnnotation(StaticJavaParser.parseAnnotation("@RequestBody"));
        handler.addParameter(requestBody);
        handler.setBody(handlerStrategy.resolveBody(metaInfo));
        controller.addMember(handler);
    }

    private void outputDtos(CompilationUnit cu, Collection<DtoMetaInfo> dtos) {
        Path sourceRoot = Locations.getStorage(cu).getSourceRoot();
        for (DtoMetaInfo dto : dtos) {
            Collection<ImportDeclaration> imports = Lists.newArrayList(new ImportDeclaration("java.util", false, true),
                    new ImportDeclaration("lombok.Data", false, false),
                    new ImportDeclaration("lombok.experimental.Accessors", false, false));
            for (String anImport : dto.imports()) {
                imports.add(new ImportDeclaration(anImport, false, false));
            }
            CuCreator cuCreator = new CuCreator(sourceRoot, dto.packageName(), imports, () -> {
                ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
                coid.addAnnotation(StaticJavaParser.parseAnnotation("@Data"))
                        .addAnnotation(StaticJavaParser.parseAnnotation("@Accessors(chain = true)"));
                coid.setPublic(true).setName(dto.typeName());
                for (String vd : dto.variableDeclarators()) {
                    coid.addMember(StaticJavaParser.parseBodyDeclaration("private " + vd + ";"));
                }
                return coid;
            });
            cuCreator.create(true);
        }
    }

    private String standardizeVd(VariableDeclarator variable) {
        com.github.javaparser.ast.type.Type varType = variable.getType();
        if (varType.isArrayType()) {
            varType = arrayTypeToCollectionType(varType.asArrayType());
        }
        String varName = removeLastDollars(variable.getNameAsString());
        return f("%s %s", varType, varName);
    }

    private ClassOrInterfaceType arrayTypeToCollectionType(ArrayType arrayType) {
        com.github.javaparser.ast.type.Type componentType = arrayType.getComponentType();
        ClassOrInterfaceType result = new ClassOrInterfaceType();
        result.setName(Collection.class.getSimpleName());
        NodeList<Type> typeArguments = new NodeList<>(componentType);
        result.setTypeArguments(typeArguments);
        return result;
    }

    private String removeLastDollars(String text) {
        if (text == null) {
            return null;
        }
        if (text.lastIndexOf("$") > 0) {
            text = text.substring(0, text.lastIndexOf("$"));
            return removeLastDollars(text);
        } else {
            return text;
        }
    }

}
