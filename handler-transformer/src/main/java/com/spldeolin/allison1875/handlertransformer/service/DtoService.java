package com.spldeolin.allison1875.handlertransformer.service;

import java.util.List;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.handlertransformer.service.impl.DtoServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(DtoServiceImpl.class)
public interface DtoService {

    List<ClassOrInterfaceDeclaration> detectDtosBottomTop(BlockStmt initBody);

}