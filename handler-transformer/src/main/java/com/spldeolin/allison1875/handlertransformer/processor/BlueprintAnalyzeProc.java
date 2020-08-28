package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.exception.ParentAbsentException;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.handlertransformer.HandlerTransformerConfig;
import com.spldeolin.allison1875.handlertransformer.meta.DtoMetaInfo;
import com.spldeolin.allison1875.handlertransformer.meta.MetaInfo;
import com.spldeolin.allison1875.handlertransformer.util.BlockStmts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-06-27
 */
@Slf4j
public class BlueprintAnalyzeProc {

    private final ClassOrInterfaceDeclaration controller;

    private final InitializerDeclaration blueprint;

    @Getter
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

        for (Statement stmtInInit : blueprint.getBody().getStatements()) {
            stmtInInit.ifExpressionStmt(exprInInit -> exprInInit.getExpression().ifVariableDeclarationExpr(vde -> {
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

        // dto meta
        Map<BlockStmt, DtoMetaInfo> dtos = Maps.newLinkedHashMap();
        List<BlockStmt> allBlockStmt = blueprint.findAll(BlockStmt.class);
        allBlockStmt.remove(0);
        BlockStmt reqBlockStmt = null;
        for (BlockStmt blockStmt : allBlockStmt) {
            DtoMetaInfo dtoMetaInfo = new DtoMetaInfo();
            boolean inReqScope = isInReqScope(blockStmt, reqBlockStmt);

            HandlerTransformerConfig conf = HandlerTransformerConfig.getInstance();
            if (isReqOrRespLevel(blockStmt, blueprint) && metaInfo.getReqBody() != null) {
                dtoMetaInfo.typeName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "RespDto");
                dtoMetaInfo.packageName(conf.getRespDtoPackage());
                dtoMetaInfo.typeQualifier(dtoMetaInfo.packageName() + "." + dtoMetaInfo.typeName());
                dtoMetaInfo.dtoName("resp");
                builder.respBody(dtoMetaInfo);
            }
            if (isReqOrRespLevel(blockStmt, blueprint) && metaInfo.getReqBody() == null) {
                dtoMetaInfo.typeName(StringUtils.upperFirstLetter(metaInfo.getHandlerName()) + "ReqDto");
                dtoMetaInfo.packageName(conf.getReqDtoPackage());
                dtoMetaInfo.typeQualifier(dtoMetaInfo.packageName() + "." + dtoMetaInfo.typeName());
                dtoMetaInfo.dtoName("req");
                builder.reqBody(dtoMetaInfo);
                reqBlockStmt = blockStmt;
            }

            for (VariableDeclarationExpr vde : BlockStmts.listExpressions(blockStmt, VariableDeclarationExpr.class)) {
                VariableDeclarator vd = vde.getVariable(0);
                String lineComment = getLineComment(vde);
                String variableName = removeLastDollars(vd.getNameAsString());
                if (StringUtils.equalsAny(variableName, "dto", "dtos")) {
                    vd.getInitializer().ifPresent(ir -> {
                        String rawDtoName = ir.asStringLiteralExpr().getValue();
                        String typeName = StringUtils.upperFirstLetter(rawDtoName) + (inReqScope ? "Req" : "Resp");
                        dtoMetaInfo.typeName(typeName + "Dto");
                        dtoMetaInfo.packageName(
                                (inReqScope ? conf.getReqDtoPackage() : conf.getRespDtoPackage()) + ".dto");
                        dtoMetaInfo.typeQualifier(dtoMetaInfo.packageName() + "." + dtoMetaInfo.typeName());
                        String asVariableDeclarator;
                        String dtoName;
                        if ("dtos".equals(vd.getNameAsString())) {
                            dtoName = English.plural(rawDtoName);
                            asVariableDeclarator = "Collection<" + dtoMetaInfo.typeName() + "> " + dtoName;
                        } else {
                            dtoName = rawDtoName;
                            asVariableDeclarator = dtoMetaInfo.typeName() + " " + dtoName;
                        }
                        dtoMetaInfo.dtoName(dtoName);
                        dtoMetaInfo.asVariableDeclarator(Pair.of(lineComment, asVariableDeclarator));
                    });
                } else {
                    arrayToCollectionMight(vd);
                    dtoMetaInfo.variableDeclarators().add(Pair.of(lineComment, vde.toString()));
                }
            }

            // 大括号内没有指定任何dto和dtos时
            if (dtoMetaInfo.typeName() == null) {
                log.warn("{}中未指定dto或者dtos属性", blockStmt.toString().replaceAll("\\r?\\n", " "));
                break;
            }

            dtos.put(blockStmt, dtoMetaInfo);
        }
        builder.dtos(ImmutableList.copyOf(dtos.values()));

        dtos.forEach((blockStmt, dtoBuilder) -> blockStmt.getParentNode().filter(dtos::containsKey)
                .ifPresent(parentBlock -> {
                    DtoMetaInfo parentMeta = dtos.get(parentBlock);
                    parentMeta.variableDeclarators().add(dtoBuilder.asVariableDeclarator());
                    parentMeta.imports().add(new ImportDeclaration(dtoBuilder.typeQualifier(), false, false));
                }));

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

}
