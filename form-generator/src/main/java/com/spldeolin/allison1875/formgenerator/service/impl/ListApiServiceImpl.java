package com.spldeolin.allison1875.formgenerator.service.impl;

import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.formgenerator.FormGeneratorConfig;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.ListApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class ListApiServiceImpl implements ListApiService {

    @Inject
    private FormGeneratorConfig formGeneratorConfig;

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();

        log.info("new List" + English.plural(form.getName()) + "Resp()");

        body.addStatement(StaticJavaParser.parseStatement(
                "return " + formGeneratorConfig.getPageResultEmptyConstruction() + ";"));
        return body;
    }

}
