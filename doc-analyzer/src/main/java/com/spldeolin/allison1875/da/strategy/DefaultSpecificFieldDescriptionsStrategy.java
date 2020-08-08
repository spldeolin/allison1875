package com.spldeolin.allison1875.da.strategy;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Deolin 2020-07-08
 */
public class DefaultSpecificFieldDescriptionsStrategy implements SpecificFieldDescriptionsStrategy {

    @Override
    public Table<String, String, String> provideSpecificFieldDescriptions() {
        return HashBasedTable.create();
    }

}