package com.spldeolin.allison1875.handlertransformer.processor;

import static com.github.javaparser.utils.CodeGenerationUtils.f;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.PackageAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.meta.DtoMetaInfo;
import com.spldeolin.allison1875.handlertransformer.meta.HandlerMetaInfo;
import com.spldeolin.allison1875.handlertransformer.util.BlockStmts;

/**
 * @author Deolin 2020-06-27
 */
public class InitializerDeclarationAnalyzeProcessor {


    private final ClassOrInterfaceDeclaration controller;

    public InitializerDeclarationAnalyzeProcessor(ClassOrInterfaceDeclaration controller) {
        this.controller = controller;
    }

    HandlerMetaInfo analyze(InitializerDeclaration blueprint) {
        // metaInfo meta
        HandlerMetaInfo metaInfo = new HandlerMetaInfo();
        metaInfo.setController(controller);
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        metaInfo.setSourceRoot(Locations.getStorage(controller).getSourceRoot());

        for (Statement stmtInInit : blueprint.getBody().getStatements()) {
            stmtInInit.ifExpressionStmt(exprInInit -> exprInInit.getExpression().ifVariableDeclarationExpr(vde -> {
                for (VariableDeclarator vd : vde.getVariables()) {
                    vd.getInitializer().ifPresent(i -> {
                        String value = i.asStringLiteralExpr().getValue();
                        switch (vd.getNameAsString()) {
                            case "handler":
                                metaInfo.setHandlerName(StringUtils.lowerFirstLetter(value));
                                break;
                            case "desc":
                                metaInfo.setHandlerDescription(value);
                                break;
                        }
                    });
                }
            }));
        }

        String controllerPackage = cu.getPackageDeclaration().orElseThrow(PackageAbsentException::new)
                .getNameAsString();
        metaInfo.setControllerPackage(controllerPackage);

        // dto meta
        Map<BlockStmt, DtoMetaInfo> dtos = Maps.newLinkedHashMap();
        List<BlockStmt> allBlockStmt = blueprint.findAll(BlockStmt.class);
        allBlockStmt.remove(0);
        BlockStmt reqBlockStmt = null;
        for (BlockStmt blockStmt : allBlockStmt) {
            DtoMetaInfo dtoBuilder = new DtoMetaInfo();
            boolean inReqScope = isInReqScope(blockStmt, reqBlockStmt);

            HandlerTransformerConfig conf = HandlerTransformerConfig.getInstance();
            if (isReqOrRespLevel(blockStmt, blueprint) && metaInfo.getReqBody() != null) {
                dtoBuilder.typeName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "RespDto");
                dtoBuilder.packageName(conf.getRespDtoPackage());
                dtoBuilder.typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                dtoBuilder.dtoName("resp");
                metaInfo.setRespBody(dtoBuilder);
            }
            if (isReqOrRespLevel(blockStmt, blueprint) && metaInfo.getReqBody() == null) {
                dtoBuilder.typeName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "ReqDto");
                dtoBuilder.packageName(conf.getReqDtoPackage());
                dtoBuilder.typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                dtoBuilder.dtoName("req");
                metaInfo.setReqBody(dtoBuilder);
                reqBlockStmt = blockStmt;
            }

            for (VariableDeclarationExpr vde : BlockStmts.listExpressions(blockStmt, VariableDeclarationExpr.class)) {
                VariableDeclarator vd = vde.getVariable(0);
                String variableName = removeLastDollars(vd.getNameAsString());
                if (StringUtils.equalsAny(variableName, "dto", "dtos")) {
                    vd.getInitializer().ifPresent(ir -> {
                        String rawDtoName = ir.asStringLiteralExpr().getValue();
                        String typeName = StringUtils.upperFirstLetter(rawDtoName) + (inReqScope ? "Req" : "Resp");
                        dtoBuilder.typeName(typeName + "Dto");
                        dtoBuilder.packageName(
                                (inReqScope ? conf.getReqDtoPackage() : conf.getRespDtoPackage()) + ".dto");
                        dtoBuilder.typeQualifier(dtoBuilder.packageName() + "." + dtoBuilder.typeName());
                        String asVariableDeclarator;
                        String dtoName;
                        if ("dtos".equals(vd.getNameAsString())) {
                            dtoName = English.plural(rawDtoName);
                            asVariableDeclarator = "Collection<" + dtoBuilder.typeName() + "> " + dtoName;
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

            metaInfo.getImports().add(dtoBuilder.typeQualifier());
            dtoBuilder.asVariableDeclarator(dtoBuilder.typeName() + " " + dtoBuilder.dtoName());
            dtos.put(blockStmt, dtoBuilder);
        }
        metaInfo.setDtos(dtos.values());
        dtos.forEach((blockStmt, dtoBuilder) -> blockStmt.getParentNode().filter(dtos::containsKey)
                .ifPresent(parentBlock -> {
                    DtoMetaInfo parentMeta = dtos.get(parentBlock);
                    parentMeta.variableDeclarators().add(dtoBuilder.asVariableDeclarator());
                    parentMeta.imports().add(dtoBuilder.typeQualifier());
                }));
        return metaInfo;
    }

    private boolean isInReqScope(BlockStmt blockStmt, BlockStmt reqBlockStmt) {
        if (reqBlockStmt == null) {
            return false;
        }
        return reqBlockStmt.isAncestorOf(blockStmt);
    }

    private boolean isReqOrRespLevel(BlockStmt blockStmt, InitializerDeclaration init) {
        return blockStmt.getParentNode().filter(parent -> parent.equals(init.getBody())).isPresent();
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

}
