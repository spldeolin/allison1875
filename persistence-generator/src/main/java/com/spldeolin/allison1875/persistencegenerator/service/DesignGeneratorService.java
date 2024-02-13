package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.Optional;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.persistencegenerator.javabean.GenerateDesignArgs;
import com.spldeolin.allison1875.persistencegenerator.service.impl.GenerateDesignServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(GenerateDesignServiceImpl.class)
public interface DesignGeneratorService {

    Optional<FileFlush> generateDesign(GenerateDesignArgs args);

}