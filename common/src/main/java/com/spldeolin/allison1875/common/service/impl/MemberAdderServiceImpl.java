package com.spldeolin.allison1875.common.service.impl;

import java.util.Optional;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.service.MemberAdderService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-02-14
 */
@Slf4j
@Singleton
public class MemberAdderServiceImpl implements MemberAdderService {

    @Override
    public Optional<FieldDeclaration> addField(String typeQualifier, String varName, ClassOrInterfaceDeclaration coid) {
        Preconditions.checkNotNull(typeQualifier);
        Preconditions.checkNotNull(varName);
        Preconditions.checkNotNull(coid);

        // 尝试在coid中找到同varName的field
        VariableDeclarator sameVarNameVd = null;
        for (FieldDeclaration fd : coid.getFields()) {
            for (VariableDeclarator vd : fd.getVariables()) {
                if (vd.getNameAsString().equals(varName)) {
                    sameVarNameVd = vd;
                }
            }
        }

        // 连名称一致的field都找不到，直接添加到coid并返回
        if (sameVarNameVd == null) {
            return Optional.of(coid.addField(typeQualifier, varName, Keyword.PRIVATE));
        }

        // 找得到名称一致的field，尝试判断类型是否一致
        boolean isTypeSame;
        String describe;
        try {
            describe = sameVarNameVd.getType().resolve().describe();
            isTypeSame = typeQualifier.equals(describe);
        } catch (Exception e) {
            log.warn("fail to resolve and describe, considered not same, type={}", sameVarNameVd.getType());
            // 悲观地视为类型不一致，确保不会出错
            isTypeSame = false;
        }

        // 找得到名称和类型都一致的field，无需添加直接返回
        if (isTypeSame) {
            return Optional.empty();
        }

        // 找到名称一致但是类型不一致的field，等于varName被其他类型占用，只能改名继续添加
        return addField(typeQualifier, varName + "Ex", coid);
    }

}