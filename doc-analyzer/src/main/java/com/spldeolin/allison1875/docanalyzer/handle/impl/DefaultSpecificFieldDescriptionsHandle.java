package com.spldeolin.allison1875.docanalyzer.handle.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;

/**
 * @author Deolin 2020-07-08
 */
public class DefaultSpecificFieldDescriptionsHandle implements SpecificFieldDescriptionsHandle {

    @Override
    public Table<String, String, String> provideSpecificFieldDescriptions() {
        return HashBasedTable.create();
    }

}