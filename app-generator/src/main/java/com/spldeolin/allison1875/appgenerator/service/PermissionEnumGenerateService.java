package com.spldeolin.allison1875.appgenerator.service;

import java.nio.file.Path;
import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.appgenerator.service.impl.PermissionEnumGenerateServiceImpl;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;

/**
 * @author Deolin 2026-06-20
 */
@ImplementedBy(PermissionEnumGenerateServiceImpl.class)
public interface PermissionEnumGenerateService {

    void generatePermissionEnum(List<FormDef> allForms, Path backendOutputRoot, String namespace);

}
