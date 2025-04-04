package com.spldeolin.allison1875.docanalyzer.enums;

/**
 * @author Deolin 2023-12-10
 */
public enum FlushToEnum {

    /**
     * 保存到本地markdown文件
     */
    MARKDOWN,

    /**
     * 同步到YAPI平台
     */
    YAPI,

    /**
     * 同步到ShowDoc平台
     */
    SHOWDOC,

    /**
     * 将endpoint直接序列化成DSL保存到本地json文件
     */
    DSL,

}
