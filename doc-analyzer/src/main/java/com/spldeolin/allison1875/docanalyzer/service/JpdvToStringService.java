package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.JpdvToStringServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(JpdvToStringServiceImpl.class)
public interface JpdvToStringService {

    String toString(JsonPropertyDescriptionValueDto jpdv);

}