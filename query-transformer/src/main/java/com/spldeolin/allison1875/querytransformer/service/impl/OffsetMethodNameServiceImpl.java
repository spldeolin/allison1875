package com.spldeolin.allison1875.querytransformer.service.impl;

import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.TokenWordConstant;
import com.spldeolin.allison1875.persistencegenerator.facade.exception.IllegalDesignException;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.util.HashingUtils;
import com.spldeolin.allison1875.querytransformer.enums.ChainMethodEnum;
import com.spldeolin.allison1875.querytransformer.exception.IllegalChainException;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.service.DesignService;
import com.spldeolin.allison1875.querytransformer.service.OffsetMethodNameService;
import com.spldeolin.allison1875.querytransformer.util.CharUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-12-06
 */
@Log4j2
@Singleton
public class OffsetMethodNameServiceImpl implements OffsetMethodNameService {

    @Inject
    private DesignService designService;

    @Override
    public CompilationUnit useOffsetMethod(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            ClassOrInterfaceDeclaration design) {
        // 获取并使用offset方法名
        StringBuilder offsetText = designService.parseOffset(design);
        String methodName;
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.query) {
            methodName = "query" + StringUtils.removeEnd(designMeta.getEntityName(), "Entity") + offsetText.charAt(0);
        } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.update) {
            methodName = "update" + StringUtils.removeEnd(designMeta.getEntityName(), "Entity") + offsetText.charAt(1);
        } else if (chainAnalysis.getChainMethod() == ChainMethodEnum.drop) {
            methodName = "drop" + StringUtils.removeEnd(designMeta.getEntityName(), "Entity") + offsetText.charAt(2);
        } else {
            throw new IllegalChainException("chainMethod is none of query, update or drop");
        }
        log.info("use offset methodName={}", methodName);
        chainAnalysis.setMethodName(methodName);

        // 更新Design类中的offset属性
        StringBuilder sb = new StringBuilder(offsetText);
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.query) {
            sb.setCharAt(0, CharUtils.intToDigitChar(CharUtils.digitCharToInt(sb.charAt(0)) + 1));
        }
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.update) {
            sb.setCharAt(1, CharUtils.intToDigitChar(CharUtils.digitCharToInt(sb.charAt(1)) + 1));
        }
        if (chainAnalysis.getChainMethod() == ChainMethodEnum.drop) {
            sb.setCharAt(2, CharUtils.intToDigitChar(CharUtils.digitCharToInt(sb.charAt(2)) + 1));
        }
        FieldDeclaration offsetField = design.getFieldByName(TokenWordConstant.OFFSET_FIELD_NAME)
                .orElseThrow(IllegalDesignException::new);
        offsetField.getVariable(0).setInitializer(new StringLiteralExpr(sb.toString()));
        CompilationUnit cu = design.findCompilationUnit().orElseThrow(CuAbsentException::new);
        Comment hashComment = Iterables.getLast(cu.getOrphanComments());
        hashComment.remove();
        cu.addOrphanComment(new LineComment(HashingUtils.hashTypeDeclaration(design)));
        return cu;
    }

}