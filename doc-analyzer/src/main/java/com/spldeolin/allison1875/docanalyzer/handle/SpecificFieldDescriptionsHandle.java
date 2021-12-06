package com.spldeolin.allison1875.docanalyzer.handle;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Singleton;

/**
 * @author Deolin 2020-07-08
 */
@Singleton
public class SpecificFieldDescriptionsHandle {

    public Table<String, String, String> provideSpecificFieldDescriptions() {
        return HashBasedTable.create();
    }

}