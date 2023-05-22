package com.spldeolin.allison1875.startransformer.processor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-05-05
 */
@Singleton
@Log4j2
public class StarTransformer implements Allison1875MainProcessor {

    @Inject
    private StarTransformerConfig config;

    @Inject
    private DetectStarChainProc detectStarChainProc;

    @Inject
    private AnalyzeChainProc analyzeChainProc;

    @Override
    public void process(AstForest astForest) {
        int detected = 0;
        for (CompilationUnit cu : astForest) {
            detected = 0;
            LexicalPreservingPrinter.setup(cu);
            for (BlockStmt block : cu.findAll(BlockStmt.class)) {
                for (MethodCallExpr starChain : detectStarChainProc.process(cu)) {
                    // analyze chain
                    ChainAnalysisDto analysis;
                    try {
                        analysis = analyzeChainProc.process(starChain, astForest);
                        log.info("chainAnalysis={}", analysis);
                    } catch (Exception e) {
                        log.error("illegal chain: " + e.getMessage());
                        return;
                    }

                    // transform 'XxxWholeDto' Javabean
                    JavabeanArg javabeanArg = new JavabeanArg();
                    javabeanArg.setAstForest(astForest);
                    javabeanArg.setPackageName(config.getWholeDtoPackge());
                    javabeanArg.setDescription("");
                    javabeanArg.setClassName(analysis.getWholeDtoName());
                    FieldArg cftFieldArg = new FieldArg();
                    cftFieldArg.setTypeQualifier(analysis.getCftEntityQualifier());
                    cftFieldArg.setTypeName(analysis.getCftEntityName());
                    cftFieldArg.setFieldName(this.entityNameToVarName(analysis.getCftEntityName()));
                    javabeanArg.getFieldArgs().add(cftFieldArg);
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        FieldArg dtFieldArg = new FieldArg();
                        dtFieldArg.setTypeQualifier(phrase.getDtEntityQualifier());
                        if (phrase.getIsOneToOne()) {
                            dtFieldArg.setTypeName(phrase.getDtEntityName());
                            dtFieldArg.setFieldName(this.entityNameToVarName(phrase.getDtEntityName()));
                        } else {
                            dtFieldArg.setTypeName("List<" + phrase.getDtEntityName() + ">");
                            dtFieldArg.setFieldName(English.plural(this.entityNameToVarName(phrase.getDtEntityName())));
                        }
                        javabeanArg.getFieldArgs().add(dtFieldArg);
                        if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(
                                phrase.getMkeys())) {
                            for (String key : phrase.getKeys()) {
                                FieldArg keyFieldArg = new FieldArg();
                                keyFieldArg.setTypeName(
                                        "Map<" + phrase.getEntityFieldTypesEachFieldName().get(key) + ","
                                                + phrase.getDtEntityName() + ">");
                                keyFieldArg.setFieldName(
                                        English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                                + StringUtils.capitalize(key));
                                javabeanArg.getFieldArgs().add(keyFieldArg);
                            }
                            for (String mkey : phrase.getMkeys()) {
                                FieldArg mkeyFieldArg = new FieldArg();
                                mkeyFieldArg.setTypeName(
                                        "Multimap<" + phrase.getEntityFieldTypesEachFieldName().get(mkey) + ","
                                                + phrase.getDtEntityName() + ">");
                                mkeyFieldArg.setFieldName(
                                        English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                                + StringUtils.capitalize(mkey));
                                javabeanArg.getFieldArgs().add(mkeyFieldArg);
                            }
                        }
                    }
                    CompilationUnit wholeDtoCu = JavabeanFactory.buildCu(javabeanArg);
                    Saves.add(wholeDtoCu);

                    // transform Query Chain and replace Star Chain
                    cu.addImport(javabeanArg.getPackageName() + "." + javabeanArg.getClassName());
                    int i = block.getStatements().indexOf(starChain.findAncestor(Statement.class).get());
                    block.setStatement(i, StaticJavaParser.parseStatement(
                            analysis.getWholeDtoName() + " whole = new " + analysis.getWholeDtoName() + "();"));
                    block.addStatement(++i, StaticJavaParser.parseStatement(
                            analysis.getCftEntityName() + " " + entityNameToVarName(analysis.getCftEntityName()) + " = "
                                    + analysis.getCftDesignName() + "." + "query().byForced().id.eq("
                                    + analysis.getCftSecondArgument() + ").one();"));
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        String code;
                        if (phrase.getIsOneToOne()) {
                            code = phrase.getDtEntityName() + " " + entityNameToVarName(phrase.getDtEntityName());
                        } else {
                            code = "List<" + phrase.getDtEntityName() + "> " + English.plural(
                                    entityNameToVarName(phrase.getDtEntityName()));
                        }
                        code += " = " + phrase.getDtDesignName() + ".query().byForced()." + phrase.getFk() + ".eq("
                                + entityNameToVarName(analysis.getCftEntityName()) + ".getId())";
                        if (phrase.getIsOneToOne()) {
                            code += ".one();";
                        } else {
                            code += ".many();";
                        }
                        block.addStatement(++i, StaticJavaParser.parseStatement(code));
                        NodeList<Statement> stmtsInForBlock = new NodeList<>();
                        for (String key : phrase.getKeys()) {
                            String mapVarName = English.plural(entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                    + StringUtils.capitalize(key);
                            block.addStatement(++i, StaticJavaParser.parseStatement(
                                    "Map<" + phrase.getEntityFieldTypesEachFieldName().get(key) + ", "
                                            + phrase.getDtEntityName() + "> " + mapVarName
                                            + " = Maps.newLinkedHashMap();"));
                            stmtsInForBlock.add(StaticJavaParser.parseStatement(
                                    mapVarName + ".put(" + entityNameToVarName(phrase.getDtEntityName()) + ".get"
                                            + StringUtils.capitalize(key) + "(), " + entityNameToVarName(
                                            phrase.getDtEntityName()) + ");"));
                        }
                        for (String mkey : phrase.getMkeys()) {
                            String mapVarName = English.plural(entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                    + StringUtils.capitalize(mkey);
                            block.addStatement(++i, StaticJavaParser.parseStatement(
                                    "Multimap<" + phrase.getEntityFieldTypesEachFieldName().get(mkey) + ", "
                                            + phrase.getDtEntityName() + "> " + mapVarName
                                            + " = LinkedListMultimap.create();"));
                            stmtsInForBlock.add(StaticJavaParser.parseStatement(
                                    mapVarName + ".put(" + entityNameToVarName(phrase.getDtEntityName()) + ".get"
                                            + StringUtils.capitalize(mkey) + "(), " + entityNameToVarName(
                                            phrase.getDtEntityName()) + ");"));
                        }
                        if (CollectionUtils.isNotEmpty(stmtsInForBlock)) {
                            ForEachStmt forEach = new ForEachStmt();
                            forEach.setVariable(
                                    new VariableDeclarationExpr(StaticJavaParser.parseType(phrase.getDtEntityName()),
                                            entityNameToVarName(phrase.getDtEntityName())));
                            forEach.setIterable(
                                    new NameExpr(English.plural(entityNameToVarName(phrase.getDtEntityName()))));
                            forEach.setBody(new BlockStmt(stmtsInForBlock));
                            block.addStatement(++i, forEach);
                        }
                    }
                    block.addStatement(++i, StaticJavaParser.parseStatement(
                            "whole." + CodeGenerationUtils.setterName(entityNameToVarName(analysis.getCftEntityName()))
                                    + "(" + entityNameToVarName(analysis.getCftEntityName()) + ");"));
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        String dtVarName = English.plural(entityNameToVarName(phrase.getDtEntityName()),
                                phrase.getIsOneToOne() ? 1 : 2);
                        block.addStatement(++i, StaticJavaParser.parseStatement(
                                "whole." + CodeGenerationUtils.setterName(dtVarName) + "(" + dtVarName + ");"));
                        for (String key : phrase.getKeys()) {
                            block.addStatement(++i, StaticJavaParser.parseStatement(
                                    "whole." + CodeGenerationUtils.setterName(dtVarName) + "Each"
                                            + StringUtils.capitalize(key) + "(" + dtVarName + "Each"
                                            + StringUtils.capitalize(key) + ");"));
                        }
                        for (String mkey : phrase.getMkeys()) {
                            block.addStatement(++i, StaticJavaParser.parseStatement(
                                    "whole." + CodeGenerationUtils.setterName(dtVarName) + "Each"
                                            + StringUtils.capitalize(mkey) + "(" + dtVarName + "Each"
                                            + StringUtils.capitalize(mkey) + ");"));
                        }
                    }

                    // add import
                    cu.addImport(analysis.getCftDesignQualifier());
                    for (PhraseDto phrase : analysis.getPhrases()) {
                        cu.addImport(phrase.getDtDesignQulifier());
                        cu.addImport(phrase.getDtEntityQualifier());
                    }

                    detected++;
                }
            }
            if (detected > 0) {
                cu.addImport("com.google.common.collect.*");
                cu.addImport("java.util.*");
                try (Writer writer = new BufferedWriter(new FileWriter(Locations.getAbsolutePath(cu).toString()))) {
                    LexicalPreservingPrinter.print(cu, writer);
                } catch (Exception e) {
                    log.error(e);
                }
                Saves.saveAll(); // save all WholeDtos
            }
        }
        if (detected == 0) {
            log.warn("no valid Chain transformed");
        } else {
            log.info("# REMEBER REFORMAT CODE #");
        }
    }

    private String entityNameToVarName(String entityName) {
        return MoreStringUtils.lowerFirstLetter(StringUtils.removeEnd(entityName, "Entity"));
    }

}