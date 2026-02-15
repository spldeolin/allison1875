package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.DdlServiceImpl;

/**
 * @author Deolin 2026-02-15
 */
@ImplementedBy(DdlServiceImpl.class)
public interface DdlService {

    String generateDdl(List<FormDef> forms);

}
