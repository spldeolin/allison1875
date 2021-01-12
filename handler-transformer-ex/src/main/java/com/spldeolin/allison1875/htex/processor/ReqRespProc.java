package com.spldeolin.allison1875.htex.processor;

import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.htex.javabean.FirstLineDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-01-12
 */
@Singleton
@Log4j2
public class ReqRespProc {

    public void checkInitBody(BlockStmt initBody, FirstLineDto firstLineDto) {
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 2) {
            throw new IllegalArgumentException(
                    "构造代码块下最多只能有2个类声明，分别用于代表Req和Resp。[" + firstLineDto.getHandlerUrl() + "] 当前："
                            + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                            .map(one -> one.getClassDeclaration().getNameAsString())
                            .collect(Collectors.joining("、")));
        }
        if (initBody.findAll(LocalClassDeclarationStmt.class).size() > 0) {
            for (LocalClassDeclarationStmt lcds : initBody.findAll(LocalClassDeclarationStmt.class)) {
                if (!StringUtils
                        .equalsAnyIgnoreCase(lcds.getClassDeclaration().getNameAsString(), "Req",
                                "Resp")) {
                    throw new IllegalArgumentException(
                            "构造代码块下类的命名只能是Req或者Resp。[" + firstLineDto.getHandlerUrl() + "] 当前："
                                    + initBody.findAll(LocalClassDeclarationStmt.class).stream()
                                    .map(one -> one.getClassDeclaration().getNameAsString())
                                    .collect(Collectors.joining("、")));
                }
            }
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class,
                coid -> coid.getNameAsString().equals("Req")).size() > 1) {
            throw new IllegalArgumentException(
                    "构造代码块下不能重复声明Req类。[" + firstLineDto.getHandlerUrl() + "]");
        }
        if (initBody.findAll(ClassOrInterfaceDeclaration.class,
                coid -> coid.getNameAsString().equals("Resp")).size() > 1) {
            throw new IllegalArgumentException(
                    "构造代码块下不能重复声明Resp类。[" + firstLineDto.getHandlerUrl() + "]");
        }
    }

}