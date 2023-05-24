package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.exception.YapiException;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiCommonRespDto;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiInterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.javabean.YApiProjectGetRespDto;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Deolin 2021-06-10
 */
@Singleton
public class YApiOpenProc {

    private static final OkHttpClient okHttpClient = new OkHttpClient();

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private DocAnalyzerConfig config;

    public YApiProjectGetRespDto getProject() {
        String url = config.getYapiUrl() + "/api/project/get" + tokenQuery();
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            YApiCommonRespDto<YApiProjectGetRespDto> responseBody = JsonUtils.toParameterizedObject(
                    response.body().string(), new TypeReference<YApiCommonRespDto<YApiProjectGetRespDto>>() {
                    });
            ensureSuccess(responseBody);
            return responseBody.getData();
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    public List<YApiInterfaceListMenuRespDto> listCats(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            YApiCommonRespDto<List<YApiInterfaceListMenuRespDto>> responseBody = JsonUtils.toParameterizedObject(
                    response.body().string(),
                    new TypeReference<YApiCommonRespDto<List<YApiInterfaceListMenuRespDto>>>() {
                    });
            ensureSuccess(responseBody);
            return responseBody.getData();
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    public JsonNode listCatsAsJsonNode(Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/list_menu" + tokenQuery() + "&project_id=" + projectId;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody.get("data");
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    //
    public JsonNode createCat(String desc, String name, Long projectId) {
        String url = config.getYapiUrl() + "/api/interface/add_cat";
        RequestBody formBody = new FormBody.Builder().add("desc", desc).add("name", name)
                .add("project_id", projectId.toString()).add("token", config.getYapiToken()).build();
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(formBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    public JsonNode getEndpoint(Long id) {
        String url = config.getYapiUrl() + "/api/interface/get" + tokenQuery() + "&id=" + id;
        try (Response response = okHttpClient.newCall(new Builder().url(url).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody.get("data");
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    public JsonNode createOrUpdateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/save";
        RequestBody requestBody = RequestBody.create(mediaType, JsonUtils.toJson(requestBodyMap));
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(requestBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    public JsonNode updateEndpoint(Map<String, Object> requestBodyMap) {
        String url = config.getYapiUrl() + "/api/interface/up";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                JsonUtils.toJson(requestBodyMap));
        try (Response response = okHttpClient.newCall(new Builder().url(url).post(requestBody).build()).execute()) {
            if (response.body() == null) {
                throw new YapiException("response body absent");
            }
            JsonNode responseBody = JsonUtils.toTree(response.body().string());
            ensureSuccess(responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new YapiException(e);
        }
    }

    private String tokenQuery() {
        return "?token=" + config.getYapiToken();
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