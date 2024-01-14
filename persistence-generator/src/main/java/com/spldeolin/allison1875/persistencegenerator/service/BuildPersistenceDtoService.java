package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.impl.BuildPersistenceDtoServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(BuildPersistenceDtoServiceImpl.class)
public interface BuildPersistenceDtoService {

    List<PersistenceDto> build(AstForest astForest);

}