package com.spldeolin.allison1875.sqlapigenerator.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.sqlapigenerator.javabean.TrackCoidDto;
import com.spldeolin.allison1875.sqlapigenerator.service.impl.MemberAdderServiceImpl;

/**
 * @author Deolin 2024-01-22
 */
@ImplementedBy(MemberAdderServiceImpl.class)
public interface MemberAdderService {

    void addMethodToCoid(MethodDeclaration method, ClassOrInterfaceDeclaration coid);

    List<FileFlush> addMethodToXml(List<String> xmlMethodCodeLines, TrackCoidDto trackCoid);

    void ensureAuwired(ClassOrInterfaceDeclaration toBeAutowired, ClassOrInterfaceDeclaration coid);

}