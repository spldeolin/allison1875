package com.spldeolin.allison1875.persistencegenerator.processor;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.JdbcTypeServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JdbcTypeServiceImpl.class)
public interface JdbcTypeService {

    JavaTypeNamingDto jdbcType2javaType(InformationSchemaDto columnMeta, AstForest astForest);

}