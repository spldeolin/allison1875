package com.spldeolin.allison1875.docanalyzer.service;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiInterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiProjectGetRespDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.YApiOpenServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(YApiOpenServiceImpl.class)
public interface YApiOpenService {

    YApiProjectGetRespDto getProject();

    List<YApiInterfaceListMenuRespDto> listCats(Long projectId);

    JsonNode listCatsAsJsonNode(Long projectId);

    JsonNode createCat(String desc, String name, Long projectId);

    JsonNode getEndpoint(Long id);

    JsonNode createOrUpdateEndpoint(Map<String, Object> requestBodyMap);

    JsonNode updateEndpoint(Map<String, Object> requestBodyMap);

}