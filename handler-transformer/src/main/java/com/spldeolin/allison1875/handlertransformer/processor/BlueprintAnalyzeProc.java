package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.exception.ParentAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.javabean.DtoMetaInfo;
import com.spldeolin.allison1875.handlertransformer.javabean.DtoMetaInfo.DtoMetaInfoBuilder;
import com.spldeolin.allison1875.handlertransformer.javabean.MetaInfo;
import com.spldeolin.allison1875.handlertransformer.util.BlockStmts;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-27
 */
@Log4j2
class BlueprintAnalyzeProc {

    private final ClassOrInterfaceDeclaration controller;

    private final InitializerDeclaration blueprint;

    private MetaInfo metaInfo;

    BlueprintAnalyzeProc(ClassOrInterfaceDeclaration controller, InitializerDeclaration blueprint) {
        this.controller = controller;
        this.blueprint = blueprint;
    }

    BlueprintAnalyzeProc process() {
        MetaInfo.MetaInfoBuilder builder = MetaInfo.builder();
        builder.location(Locations.getRelativePathWithLineNo(blueprint));
        builder.controller(controller);
        builder.sourceRoot(Locations.getStorage(controller).getSourceRoot());

        for (Statement stmtInBlueprint : blueprint.getBody().getStatements()) {
            stmtInBlueprint.ifExpressionStmt(exprInInit -> exprInInit.getExpression().ifVariableDeclarationExpr(vde -> {
                for (VariableDeclarator vd : vde.getVariables()) {
                    vd.getInitializer().ifPresent(i -> {
                        String value = i.asStringLiteralExpr().getValue();
                        switch (vd.getNameAsString()) {
                            case "handler":
                                builder.handlerName(StringUtils.lowerFirstLetter(value));
                                break;
                            case "desc":
                                builder.handlerDescription(value);
                                break;
                        }
                    });
                }
            }));
        }

        String handlerName = builder.build().getHandlerName();
        if (handlerName == null) {
            metaInfo = builder.build();
            return this;
        }

        // dto meta
        Map<BlockStmt, DtoMetaInfoBuilder> dtos = Maps.newLinkedHashMap();
        List<BlockStmt> allBlockStmt = blueprint.findAll(BlockStmt.class);
        allBlockStmt.remove(0);
        BlockStmt reqBlockStmt = null;
        DtoMetaInfo reqBody = null;
        DtoMetaInfo respBody = null;
        for (BlockStmt blockStmt : allBlockStmt) {
            DtoMetaInfoBuilder dtoBuilder = DtoMetaInfo.builder();
            boolean inReqScope = isInReqScope(blockStmt, reqBlockStmt);

            HandlerTransformerConfig conf = HandlerTransformer.CONFIG.get();
            if (isReqOrRespLevel(blockStmt, blueprint) && reqBody != null) {
                String typeName = StringUtils.upperFirstLetter(handlerName) + "RespDto";
                String respPackageName = conf.getRespDtoPackage();
                dtoBuilder.typeName(typeName);
                dtoBuilder.packageName(respPackageName);
                dtoBuilder.typeQualifier(respPackageName + "." + typeName);
                dtoBuilder.dtoName("resp");
                respBody = dtoBuilder.build();
            }
            if (isReqOrRespLevel(blockStmt, blueprint) && reqBody == null) {
                String typeName = StringUtils.upperFirstLetter(handlerName) + "ReqDto";
                String packageName = conf.getReqDtoPackage();
                dtoBuilder.typeName(typeName);
                dtoBuilder.packageName(packageName);
                dtoBuilder.typeQualifier(packageName + "." + typeName);
                dtoBuilder.dtoName("req");
                reqBody = dtoBuilder.build();
                reqBlockStmt = blockStmt;
            }

            Collection<Pair<String, String>> variableDeclarators = Lists.newArrayList();
            for (VariableDeclarationExpr vde : BlockStmts.listExpressions(blockStmt, VariableDeclarationExpr.class)) {
                VariableDeclarator vd = vde.getVariable(0);
                String lineComment = getLineComment(vde);
                String variableName = removeLastDollars(vd.getNameAsString());
                if (StringUtils.equalsAny(variableName, "dto", "dtos")) {
                    vd.getInitializer().ifPresent(ir -> {
                        String rawDtoName = ir.asStringLiteralExpr().getValue();
                        String typeName =
                                StringUtils.upperFirstLetter(rawDtoName) + (inReqScope ? "Req" : "Resp") + "Dto";
                        String packageName = (inReqScope ? conf.getReqDtoPackage() : conf.getRespDtoPackage()) + ".dto";
                        dtoBuilder.typeName(typeName);
                        dtoBuilder.packageName(packageName);
                        dtoBuilder.typeQualifier(packageName + "." + typeName);
                        String asVariableDeclarator;
                        String dtoName;
                        if ("dtos".equals(variableName)) {
                            dtoName = English.plural(rawDtoName);
                            asVariableDeclarator = "Collection<" + typeName + "> " + dtoName;
                        } else {
                            dtoName = rawDtoName;
                            asVariableDeclarator = typeName + " " + dtoName;
                        }
                        dtoBuilder.dtoName(dtoName);
                        dtoBuilder.asVariableDeclarator(Pair.of(lineComment, asVariableDeclarator));
                    });
                } else {
                    arrayToCollectionMight(vd);
                    variableDeclarators.add(Pair.of(lineComment, vde.toString()));
                }
            }
            dtoBuilder.variableDeclarators(ImmutableList.copyOf(variableDeclarators));

            // 大括号内没有指定任何dto和dtos时
            if (dtoBuilder.build().getTypeName() == null) {
                log.warn("存在未指定dto或者dtos属性的区域，忽略这个dto[{}]", Locations.getRelativePathWithLineNo(blueprint));
                continue;
            }
            // 大括号内没有除dto或dtos以外的vde
            if (dtoBuilder.build().getVariableDeclarators().size() == 0) {
                log.warn("存在没有指定除dto或dtos以外属性的区域，忽略这个dto[{}]", Locations.getRelativePathWithLineNo(blueprint));
                continue;
            }

            dtoBuilder.imports(Imports.listImports(controller));

            dtos.put(blockStmt, dtoBuilder);
        }

        // 把子BlockStmt的asVariableDeclarator添加到父BlockStmt的variableDeclarators中
        // 把子BlockStmt的typeQualifier添加到父BlockStmt的imports中
        for (Entry<BlockStmt, DtoMetaInfoBuilder> entry : dtos.entrySet()) {
            BlockStmt blockStmt = entry.getKey();
            DtoMetaInfo dtoMeta = entry.getValue().build();

            BlockStmt parent = (BlockStmt) blockStmt.getParentNode().orElseThrow(ParentAbsentException::new);
            DtoMetaInfoBuilder parentMetaBuilder = dtos.get(parent);
            if (parentMetaBuilder == null) {
                // parent是blueprint时
                continue;
            }

            DtoMetaInfo parentMetaInfo = parentMetaBuilder.build();

            List<ImportDeclaration> importDeclarations = Lists.newArrayList(parentMetaInfo.getImports());
            importDeclarations.add(new ImportDeclaration(dtoMeta.getTypeQualifier(), false, false));
            parentMetaBuilder.imports(ImmutableList.copyOf(importDeclarations));

            List<Pair<String, String>> pairs = Lists.newArrayList(parentMetaInfo.getVariableDeclarators());
            pairs.add(dtoMeta.getAsVariableDeclarator());
            parentMetaBuilder.variableDeclarators(ImmutableList.copyOf(pairs));
        }

        List<DtoMetaInfo> dtoMetaInfos = dtos.values().stream().map(DtoMetaInfoBuilder::build)
                .collect(Collectors.toList());
        builder.dtos(ImmutableList.copyOf(dtoMetaInfos));

        Map<String, DtoMetaInfo> dtoMetaInfoMap = Maps.newHashMap();
        dtoMetaInfos.forEach(one -> dtoMetaInfoMap.put(one.getTypeQualifier(), one));

        if (reqBody != null) {
            builder.reqBody(dtoMetaInfoMap.get(reqBody.getTypeQualifier()));
        } else {
            throw new RuntimeException("Blueprint不能缺少代表req的block");
        }
        if (respBody != null) {
            builder.respBody(dtoMetaInfoMap.get(respBody.getTypeQualifier()));
        } else {
            throw new RuntimeException("Blueprint不能缺少代表resp的block");
        }

        metaInfo = builder.build();
        return this;
    }

    private String getLineComment(VariableDeclarationExpr vde) {
        Optional<Comment> comment = vde.getParentNode().orElseThrow(ParentAbsentException::new).getComment();
        return comment.map(Comment::getContent).orElse(null);
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

    private void arrayToCollectionMight(VariableDeclarator vd) {
        vd.getType().ifArrayType(arrayType -> vd
                .setType(StaticJavaParser.parseType("Collection<" + arrayType.getComponentType() + ">")));
    }

    public MetaInfo getMetaInfo() {
        return this.metaInfo;
    }

}
