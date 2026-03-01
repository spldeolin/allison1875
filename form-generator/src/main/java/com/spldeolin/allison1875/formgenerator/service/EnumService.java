package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.EnumServiceImpl;

/**
 * @author Deolin 2026-02-15
 */
@ImplementedBy(EnumServiceImpl.class)
public interface EnumService {

    void generateEnums(List<FormDef> forms);

}
