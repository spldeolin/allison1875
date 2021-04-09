package com.spldeolin.allison1875.docanalyzer.handle;

import javax.inject.Singleton;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Deolin 2020-07-08
 */
@Singleton
public class SpecificFieldDescriptionsHandle {

    public Table<String, String, String> provideSpecificFieldDescriptions() {
        return HashBasedTable.create();
    }

}