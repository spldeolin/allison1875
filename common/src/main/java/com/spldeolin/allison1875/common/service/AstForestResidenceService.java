package com.spldeolin.allison1875.common.service;

import java.io.File;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.service.impl.MavenAstForestResidenceService;

/**
 * @author Deolin 2024-01-17
 */
@ImplementedBy(MavenAstForestResidenceService.class)
public interface AstForestResidenceService {

    File findWorkModuleRoot(Class<?> primaryClass);

    File findWorkAstForestRoot(Class<?> primaryClass);

}