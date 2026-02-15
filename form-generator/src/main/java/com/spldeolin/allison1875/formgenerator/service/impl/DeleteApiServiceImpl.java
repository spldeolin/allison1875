package com.spldeolin.allison1875.formgenerator.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.DeleteApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-02-15
 */
@Singleton
@Slf4j
public class DeleteApiServiceImpl implements DeleteApiService {

    @Inject
    private CommonConfig commonConfig;

    @Override
    public BlockStmt generateMethodBody(FormDef form) {
        BlockStmt body = new BlockStmt();
        body.addStatement(StaticJavaParser.parseStatement(form.getName() + "Design.delete().over();"));
        return body;
    }

}
