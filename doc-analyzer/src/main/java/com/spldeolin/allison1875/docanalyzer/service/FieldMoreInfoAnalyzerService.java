package com.spldeolin.allison1875.docanalyzer.service;

import java.lang.reflect.Field;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.service.impl.FieldMoreInfoAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(FieldMoreInfoAnalyzerServiceImpl.class)
public interface FieldMoreInfoAnalyzerService {

    Object moreAnalyzerField(Field field);

    String formatMoreInfo(Object moreInfo);

}