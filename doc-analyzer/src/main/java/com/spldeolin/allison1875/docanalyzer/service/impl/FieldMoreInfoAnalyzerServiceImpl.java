package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.lang.reflect.Field;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.docanalyzer.service.FieldMoreInfoAnalyzerService;

/**
 * @author Deolin 2020-12-02
 */
@Singleton
public class FieldMoreInfoAnalyzerServiceImpl implements FieldMoreInfoAnalyzerService {

    @Override
    public Object moreAnalyzerField(Field field) {
        // 可拓展为对field的额外信息进行分析
        return null;
    }

    @Override
    public String formatMoreInfo(Object moreInfo) {
        // 可拓展为对field的额外信息进行格式化
        return null;
    }

}