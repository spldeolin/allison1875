package com.spldeolin.allison1875.docanalyzer.handle;

import com.google.common.collect.Table;

/**
 * 提供 对特定Field的描述 的策略
 *
 * @author Deolin 2020-07-08
 */
public interface SpecificFieldDescriptionsHandle {

    /**
     * @return 类的全限定名，字段名，描述
     */
    Table<String, String, String> provideSpecificFieldDescriptions();

}
