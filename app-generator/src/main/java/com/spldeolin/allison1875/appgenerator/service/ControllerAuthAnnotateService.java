package com.spldeolin.allison1875.appgenerator.service;

import java.nio.file.Path;
import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.appgenerator.service.impl.ControllerAuthAnnotateServiceImpl;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;

/**
 * @author Deolin 2026-06-21
 */
@ImplementedBy(ControllerAuthAnnotateServiceImpl.class)
public interface ControllerAuthAnnotateService {

    void annotateControllers(List<FormDef> forms, Path backendOutputRoot, String namespace);

}
