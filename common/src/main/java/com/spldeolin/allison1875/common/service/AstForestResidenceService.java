package com.spldeolin.allison1875.common.service;

import java.io.File;
import java.nio.file.Path;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.MavenAstForestResidenceService;

/**
 * @author Deolin 2024-01-17
 */
@ImplementedBy(MavenAstForestResidenceService.class)
public interface AstForestResidenceService {

    File findAstForestRoot(Class<?> primaryClass);

    Path findModuleRoot(Class<?> primaryClass);

}