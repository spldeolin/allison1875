package com.spldeolin.allison1875.formgenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.service.impl.FormGeneratorCommonItemsExpansionServiceImpl;

/**
 * @author Deolin 2026-06-23
 */
@ImplementedBy(FormGeneratorCommonItemsExpansionServiceImpl.class)
public interface CommonItemsExpansionService {

    List<FormDef> addCommonItems(List<FormDef> forms);

}
