package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.exception.YapiException;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiCommonRespDto;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiInterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiProjectGetRespDto;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

/**
 * @author Deolin 2021-06-10
 */
@Singleton
public class YApiOpenProc {

    @Inject
    private DocAnalyzerConfig docAnalyzerConfig;

    public YApiProjectGetRespDto getProject() {
        String url = docAnalyzerConfig.getYapiUrl() + "/api/project/get" + tokenQuery();
        HttpResponse response = HttpRequest.get(url).send();
        YApiCommonRespDto<YApiProjectGetRespDto> responseBody = JsonUtils.toParameterizedObject(response.bodyText(),
                new TypeReference<YApiCommonRespDto<YApiProjectGetRespDto>>() {
                });
        ensureSuccess(responseBody);
        return responseBody.getData();
    }

    public List<YApiInterfaceListMenuRespDto> listCats(Long projectId) {
        String url =
                docAnalyzerConfig.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        HttpResponse response = HttpRequest.get(url).send();
        YApiCommonRespDto<List<YApiInterfaceListMenuRespDto>> responseBody = JsonUtils
                .toParameterizedObject(response.bodyText(),
                        new TypeReference<YApiCommonRespDto<List<YApiInterfaceListMenuRespDto>>>() {
                        });
        ensureSuccess(responseBody);
        return responseBody.getData();
    }

    public JsonNode listCatsAsJsonNode(Long projectId) {
        String url =
                docAnalyzerConfig.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        HttpResponse response = HttpRequest.get(url).send();
        JsonNode responseBody = JsonUtils.toTree(response.bodyText());
        ensureSuccess(responseBody);
        return responseBody.get("data");
    }

    //
    public JsonNode createCat(String desc, String name, Long projectId) {
        String url = docAnalyzerConfig.getYapiUrl() + "/api/interface/add_cat";
        Map<String, Object> form = Maps.newHashMap();
        form.put("desc", desc);
        form.put("name", name);
        form.put("project_id", projectId);
        form.put("token", docAnalyzerConfig.getYapiToken());
        HttpResponse response = HttpRequest.post(url).form(form).send();
        JsonNode responseBody = JsonUtils.toTree(response.bodyText());
        ensureSuccess(responseBody);
        return responseBody;
    }

    public JsonNode getEndpoint(Long id) {
        String url = docAnalyzerConfig.getYapiUrl() + "/api/interface/get" + tokenQuery() + "&id=" + id;
        HttpResponse response = HttpRequest.get(url).send();
        JsonNode responseBody = JsonUtils.toTree(response.bodyText());
        ensureSuccess(responseBody);
        return responseBody.get("data");
    }

    public JsonNode createOrUpdateEndpoint(Map<String, Object> requestBody) {
        String url = docAnalyzerConfig.getYapiUrl() + "/api/interface/save";
        HttpResponse response = HttpRequest.post(url)
                .bodyText(JsonUtils.toJson(requestBody), "application/json", "utf-8").send();
        JsonNode responseBody = JsonUtils.toTree(response.bodyText());
        ensureSuccess(responseBody);
        return responseBody;
    }

    public JsonNode updateEndpoint(Map<String, Object> requestBody) {
        String url = docAnalyzerConfig.getYapiUrl() + "/api/interface/up";
        HttpResponse response = HttpRequest.post(url)
                .bodyText(JsonUtils.toJson(requestBody), "application/json", "utf-8").send();
        JsonNode responseBody = JsonUtils.toTree(response.bodyText());
        ensureSuccess(responseBody);
        return responseBody;
    }

    private String tokenQuery() {
        return "?token=" + docAnalyzerConfig.getYapiToken();
    }

    private static void ensureSuccess(YApiCommonRespDto<?> resp) throws YapiException {
        if (resp == null) {
            throw new YapiException();
        }
        if (resp.getErrcode() != 0) {
            throw new YapiException(resp.getErrmsg());
        }
    }

    private void ensureSuccess(JsonNode respNode) {
        if (respNode == null) {
            throw new YapiException();
        }
        if (respNode.get("errcode").asInt() != 0 || respNode.get("data") == null) {
            throw new YapiException(respNode.toString());
        }
    }

}