package com.spldeolin.allison1875.common.service;

import java.util.Optional;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;

/**
 * @author Deolin 2024-02-14
 */
@ImplementedBy(MemberAdderService.class)
public interface MemberAdderService {

    /**
     * 为参数coid添加参数typeQualifier和参数varName所组成的Field
     *
     * @return 如果所组成的Field已在coid中存在，无需添加，则返回empty；否则返回新添加的FieldDeclaration对象
     */
    Optional<FieldDeclaration> addField(String typeQualifier, String varName, ClassOrInterfaceDeclaration coid);

}