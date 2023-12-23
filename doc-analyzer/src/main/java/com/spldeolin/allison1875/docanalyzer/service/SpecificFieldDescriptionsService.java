package com.spldeolin.allison1875.docanalyzer.service;

import com.google.common.collect.Table;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.SpecificFieldDescriptionsServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(SpecificFieldDescriptionsServiceImpl.class)
public interface SpecificFieldDescriptionsService {

    Table<String, String, String> provideSpecificFieldDescriptions();

}