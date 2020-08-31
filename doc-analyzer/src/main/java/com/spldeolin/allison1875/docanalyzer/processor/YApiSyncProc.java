package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.redisson.api.RLock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.redis.RedissonFactory;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.MarkdownUtils;
import com.spldeolin.allison1875.docanalyzer.yapi.YapiException;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.CommonRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.InterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.ProjectGetRespDto;
import lombok.extern.log4j.Log4j2;

/**
 * 将endpoints同步到YApi
 *
 * @author Deolin 2020-07-26
 */
@Log4j2
class YApiSyncProc {

    private static final String ALLISON_1875_TAG = "Allison 1875";

    private static final String DELETE_TAG = "已删除";

    private static final String url = DocAnalyzerConfig.getInstance().getYapiUrl();

    private static final String token = DocAnalyzerConfig.getInstance().getYapiToken();

    private static final Long projectId = getProjectIdFromYApi();

    private final Collection<EndpointDto> endpoints;

    private static Long getProjectIdFromYApi() {
        String json = HttpUtils.get(url + "/api/project/get?token=" + token);
        CommonRespDto<ProjectGetRespDto> resp = JsonUtils
                .toParameterizedObject(json, new TypeReference<CommonRespDto<ProjectGetRespDto>>() {
                });
        ensureSuccess(resp);
        return resp.getData().getId();
    }

    YApiSyncProc(Collection<EndpointDto> endpoints) {
        this.endpoints = endpoints;
    }

    void process() {
        RLock lock = RedissonFactory.getSingleServer().getLock("allison1875_docanalyzer_" + url + "_" + projectId);
        try {
            // 尝试加锁，最多等待100秒，上锁以后30秒自动解锁
            if (lock.tryLock(100, 20, TimeUnit.SECONDS)) {
                try {
                    Set<String> catNames = endpoints.stream().map(EndpointDto::getCat).collect(Collectors.toSet());
                    catNames.add("回收站");
                    Set<String> yapiCatNames = this.getYapiCatIdsEachName().keySet();
                    this.createYApiCat(Sets.difference(catNames, yapiCatNames));

                    Map<String, Long> catName2catId = this.getYapiCatIdsEachName();

                    Map<String, JsonNode> yapiUrls = this.listAutoInterfaces();
                    Set<String> analysisUrls = endpoints.stream().map(EndpointDto::getUrl).collect(Collectors.toSet());

                    // yapi中，在解析出endpoint中找不到url的接口，移动到回收站
                    for (String yapiUrl : yapiUrls.keySet()) {
                        if (!analysisUrls.contains(yapiUrl)) {
                            this.deleteInterface(yapiUrls.get(yapiUrl), catName2catId.get("回收站"));
                        }
                    }

                    // 新增接口
                    for (EndpointDto endpoint : endpoints) {
                        Collection<String> descriptionLines = endpoint.getDescriptionLines();
                        String title = Iterables.getFirst(descriptionLines, null);
                        if (title == null || title.length() == 0) {
                            title = endpoint.getHandlerSimpleName();
                        }
                        String yapiDesc = endpoint.toStringPrettily();

                        String reqJs = toJson(endpoint.getRequestBodyJsonSchema());
                        String respJs = toJson(endpoint.getResponseBodyJsonSchema());

                        this.createYApiInterface(title, endpoint.getUrl(), reqJs, respJs, yapiDesc,
                                endpoint.getHttpMethod(), catName2catId.get(endpoint.getCat()));
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    private String toJson(JsonSchema bodyJsonSchema) {
        String json = "";
        if (bodyJsonSchema != null) {
            // jpdv -> Pretty String
            JsonSchemaTraverseUtils.traverse("根节点", bodyJsonSchema, (propertyName, jsonSchema, parentJsonSchema) -> {
                JsonPropertyDescriptionValueDto jpdv = JsonUtils
                        .toObjectSkipNull(jsonSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
                if (jpdv != null) {
                    jsonSchema.setDescription(jpdv.toStringPrettily());
                }
            });

            json = JsonUtils.toJson(bodyJsonSchema);
        }
        return json;
    }

    Map<String, Long> getYapiCatIdsEachName() {
        String json = HttpUtils.get(url + "/api/interface/list_menu?token=" + token + "&project_id" + projectId);
        CommonRespDto<List<InterfaceListMenuRespDto>> resp = JsonUtils
                .toParameterizedObject(json, new TypeReference<CommonRespDto<List<InterfaceListMenuRespDto>>>() {
                });
        ensureSuccess(resp);

        Map<String, Long> result = Maps.newHashMap();
        for (InterfaceListMenuRespDto cat : resp.getData()) {
            result.put(cat.getName(), cat.getId());
        }
        return result;
    }

    private void createYApiCat(Collection<String> catNames) {
        for (String catName : catNames) {
            Map<String, String> form = Maps.newHashMap();
            form.put("desc", "");
            form.put("name", catName);
            form.put("project_id", projectId.toString());
            form.put("token", token);
            HttpUtils.postForm(url + "/api/interface/add_cat", form);
        }

    }

    private Map<String, JsonNode> listAutoInterfaces() {
        JsonNode interfaceListMenuDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/interface/list_menu?token=" + token + "&project_id" + projectId));

        Map<String, JsonNode> result = Maps.newHashMap();
        for (JsonNode jsonNode : interfaceListMenuDto) {
            for (JsonNode interf : jsonNode.get("list")) {
                List<String> tags = JsonUtils.toListOfObject(interf.get("tag").toString(), String.class);
                if (tags.contains(ALLISON_1875_TAG)) {
                    result.put(interf.get("path").asText(), interf);
                }
            }
        }
        return result;
    }

    private void deleteInterface(JsonNode jsonNode, Long recycleBinCatId) {
        if (recycleBinCatId.equals(jsonNode.get("catid").asLong())) {
            // 已在"回收站"分类中
            return;
        }
        Long id = jsonNode.get("_id").asLong();

        JsonNode detail = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/interface/get?id=" + id + "&token=" + token));

        Map<String, Object> form = Maps.newHashMap();
        form.put("id", id);
        form.put("catid", recycleBinCatId);
        List<String> tags = JsonUtils.toListOfObject(jsonNode.get("tag").toString(), String.class);
        tags.add(DELETE_TAG);
        form.put("tag", tags);

        String desc = "";
        JsonNode descNode = detail.get("desc");
        if (descNode != null) {
            desc = descNode.asText();
        }
        String deleteMessage = MarkdownUtils.convertToHtml("> 该接口已被删除，或是它的URL已被更改，**禁止调用**\n");
        deleteMessage = StringUtils
                .replaceLast(deleteMessage, "<strong>", "<span style='background:black;color:#FFD9E6'>");
        deleteMessage = StringUtils.replaceLast(deleteMessage, "</strong>", "</span>");

        form.put("desc", deleteMessage + desc);
        form.put("token", token);
        String resp = HttpUtils.postJson(url + "/api/interface/up", JsonUtils.toJson(form));
        log.info(resp);
    }

    private void createYApiInterface(String title, String url, String requestBodyJsonSchema,
            String responseBodyJsonSchema, String description, String httpMethod, Long catId) {
        Map<String, Object> form = Maps.newHashMap();
        form.put("title", title);
        form.put("path", url);
        form.put("status", "done");
        form.put("req_body_type", "json");
        form.put("req_body_is_json_schema", true);
        form.put("req_body_other", requestBodyJsonSchema);
        form.put("res_body_type", "json");
        form.put("res_body_is_json_schema", true);
        form.put("res_body", responseBodyJsonSchema);
        form.put("switch_notice", true);
        form.put("message", "1");
        form.put("tag", Lists.newArrayList(ALLISON_1875_TAG));
        form.put("desc", MarkdownUtils.convertToHtml(description));
        form.put("method", httpMethod);
        form.put("catid", catId);
        form.put("token", token);
        String resp = HttpUtils.postJson(YApiSyncProc.url + "/api/interface/save", JsonUtils.toJson(form));
        log.info(resp);
    }

    private JsonNode ensureSusscessAndToGetData(String respJson) {
        ObjectMapper om = JsonUtils.initObjectMapper(new ObjectMapper());
        JsonNode jsonNode;
        try {
            jsonNode = om.readTree(respJson);
            if (jsonNode.get("errcode").asInt() == 0) {
                return jsonNode.get("data");
            }
        } catch (JsonProcessingException e) {
            log.error(e);
        }
        return null;
    }

    private static void ensureSuccess(CommonRespDto<?> resp) throws YapiException {
        if (resp.getErrcode() != 0) {
            throw new YapiException(resp.getErrmsg());
        }
    }

}