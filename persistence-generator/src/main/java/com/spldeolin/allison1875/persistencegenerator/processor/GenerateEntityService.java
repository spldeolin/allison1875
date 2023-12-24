package com.spldeolin.allison1875.persistencegenerator.processor;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.processor.impl.GenerateEntityServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(GenerateEntityServiceImpl.class)
public interface GenerateEntityService {

    EntityGeneration process(PersistenceDto persistence, AstForest astForest);

}