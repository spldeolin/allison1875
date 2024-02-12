package com.spldeolin.allison1875.docanalyzer.service;

import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.JpdvDocGeneratorServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(JpdvDocGeneratorServiceImpl.class)
public interface JpdvDocGeneratorService {

    String generateJpdvDoc(JsonPropertyDescriptionValueDto jpdv);

}