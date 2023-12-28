package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.nio.file.Path;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.Cus;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.exception.IllegalDesignException;
import com.spldeolin.allison1875.persistencegenerator.service.FindMethodNamingOffsetService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Singleton
@Log4j2
public class FindMethodNamingOffsetServiceImpl implements FindMethodNamingOffsetService {

    @Override
    public FieldDeclaration findMethodNamingOffsetField(Path designPath) {
        CompilationUnit cu = Cus.parseCu(designPath);
        if (cu != null) {
            TypeDeclaration<?> design = cu.getPrimaryType().orElseThrow(IllegalDesignException::new);
            FieldDeclaration offset = design.getFieldByName(TokenWordConstant.OFFSET_FIELD_NAME).orElse(null);
            if (offset != null) {
                log.info("find offset for [{}]: {}", design.getNameAsString(), offset.getVariables());
                return offset;
            }
        }
        log.info("init offset for [{}]: {}", designPath, "new int[] {1, 1, 1};");
        return (FieldDeclaration) StaticJavaParser.parseBodyDeclaration("String offset = \"111\";");
    }

}