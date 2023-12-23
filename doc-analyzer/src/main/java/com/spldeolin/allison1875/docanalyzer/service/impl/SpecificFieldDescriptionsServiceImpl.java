package com.spldeolin.allison1875.docanalyzer.service.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.service.SpecificFieldDescriptionsService;

/**
 * @author Deolin 2020-07-08
 */
@Singleton
public class SpecificFieldDescriptionsServiceImpl implements SpecificFieldDescriptionsService {

    @Override
    public Table<String, String, String> provideSpecificFieldDescriptions() {
        return HashBasedTable.create();
    }

}