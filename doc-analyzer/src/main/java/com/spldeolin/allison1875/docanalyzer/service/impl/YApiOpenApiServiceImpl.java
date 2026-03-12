package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.docanalyzer.dto.YApiCommonRespDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiInterfaceListMenuRespDTO;
import com.spldeolin.allison1875.docanalyzer.dto.YApiProjectGetRespDTO;
import com.spldeolin.allison1875.docanalyzer.service.YApiOpenApiService;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;

/**
 * @author Deolin 2021-06-10
 */
@Singleton
public class YApiOpenApiServiceImpl implements YApiOpenApiService {

    @Inject
    private Config config;

    @Override
    public YApiProjectGetRespDTO getProject() {
        String url = config.getYapiUrl() + "/api/project/get" + tokenQuery();
        YApiCommonRespDTO<YApiProjectGetRespDTO> responseBody = HttpUtils.get(url,
                new TypeReference<YApiCommonRespDTO<YApiProjectGetRespDTO>>() {
                });
        ensureSuccess(responseBody);
        return responseBody.getData();
    }

    @Override
    public List<YApiInterfaceListMenuRespDTO> listCats(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        YApiCommonRespDTO<List<YApiInterfaceListMenuRespDTO>> responseBody = HttpUtils.get(url,
                new TypeReference<YApiCommonRespDTO<List<YApiInterfaceListMenuRespDTO>>>() {
                });
        ensureSuccess(responseBody);
        return responseBody.getData();
    }

    @Override
    public JsonNode listCatsAsJsonNode(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        JsonNode responseBody = HttpUtils.get(url);
        ensureSuccess(responseBody);
        return responseBody.get("data");
    }

    @Override
    public JsonNode createCat(String desc, String name, Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/add_cat";
        Map<String, String> formData = Maps.newHashMap();
        formData.put("desc", desc);
        formData.put("name", name);
        formData.put("project_id", projectId.toString());
        formData.put("token", config.getYapiToken());
        JsonNode responseBody = HttpUtils.formPost(url, formData);
        ensureSuccess(responseBody);
        return responseBody;
    }

    @Override
    public JsonNode getEndpoint(Long id) {
        String url = config.getYapiUrl() + "/api/interface/get" + tokenQuery() + "&id=" + id;
        JsonNode responseBody = HttpUtils.get(url);
        ensureSuccess(responseBody);
        return responseBody.get("data");
    }

    @Override
    public JsonNode createOrUpdateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/save";
        JsonNode responseBody = HttpUtils.post(url, requestBodyMap);
        ensureSuccess(responseBody);
        return responseBody;
    }

    @Override
    public JsonNode updateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/up";
        JsonNode responseBody = HttpUtils.post(url, requestBodyMap);
        ensureSuccess(responseBody);
        return responseBody;
    }

    private String tokenQuery() {
        return "?token=" + config.getYapiToken();
    }

    private static void ensureSuccess(YApiCommonRespDTO<?> resp) {
        if (resp == null) {
            throw new Allison1875Exception("resp is null");
        }
        if (resp.getErrcode() != 0) {
            throw new Allison1875Exception(resp.getErrmsg());
        }
    }

    private void ensureSuccess(JsonNode respNode) {
        if (respNode == null) {
            throw new Allison1875Exception("respNode is null");
        }
        if (respNode.get("errcode").asInt() != 0 || respNode.get("data") == null) {
            throw new Allison1875Exception(respNode.toString());
        }
    }

}