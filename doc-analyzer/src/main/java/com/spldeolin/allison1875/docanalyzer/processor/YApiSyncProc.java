package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.github.javaparser.utils.StringEscapeUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.redis.RedissonFactory;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.constant.YApiConstant;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.MarkdownUtils;
import com.spldeolin.allison1875.docanalyzer.yapi.YapiException;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.InterfaceListMenuRespDto;
import lombok.extern.log4j.Log4j2;

/**
 * 将endpoints同步到YApi
 *
 * @author Deolin 2020-07-26
 */
@Singleton
@Log4j2
public class YApiSyncProc {

    @Inject
    private JpdvToStringProc jpdvToStringProc;

    @Inject
    private EndpointToStringProc endpointToStringProc;

    @Inject
    private DocAnalyzerConfig docAnalyzerConfig;

    @Inject
    private RedissonFactory redissonFactory;

    @Inject
    private YApiOpenProc yApiOpenProc;

    public void process(Collection<EndpointDto> endpoints) throws Exception {
        String baseUrl = docAnalyzerConfig.getYapiUrl();

        Long projectId = yApiOpenProc.getProject().getId();

        RedissonClient redisson = redissonFactory.getRedissonClient();
        RLock lock = redisson.getLock("allison1875_docanalyzer_" + baseUrl + "_" + projectId);
        // 尝试加锁，最多等待100秒，上锁以后30秒自动解锁
        if (lock.tryLock(100, 20, TimeUnit.SECONDS)) {
            try {
                Set<String> catNames = endpoints.stream().map(EndpointDto::getCat).collect(Collectors.toSet());
                catNames.add("回收站");
                Set<String> yapiCatNames = this.getYapiCatIdsEachName(projectId).keySet();
                this.createYApiCat(Sets.difference(catNames, yapiCatNames), projectId);

                Map<String, Long> catName2catId = this.getYapiCatIdsEachName(projectId);

                Map<String, JsonNode> yapiUrls = this.listAutoInterfaces(projectId);
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
                    String yapiDesc = endpointToStringProc.toString(endpoint);

                    String reqJs = toJson(endpoint.getRequestBodyJsonSchema());
                    String respJs = toJson(endpoint.getResponseBodyJsonSchema());

                    this.createYApiInterface(title, endpoint.getUrl(), reqJs, respJs, yapiDesc,
                            endpoint.getHttpMethod(), catName2catId.get(endpoint.getCat()));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private String toJson(JsonSchema bodyJsonSchema) {
        if (bodyJsonSchema == null) {
            return "";
        }
        ObjectMapper om = JsonUtils.createObjectMapper();
        try {
            om.writeValueAsString(bodyJsonSchema.getDescription());
        } catch (Exception e) {
            log.info("不再重复toPrettyString [{}]", bodyJsonSchema.getId());
            return JsonUtils.toJson(bodyJsonSchema);
        }

        // jpdv -> Pretty String
        JsonSchemaTraverseUtils.traverse("根节点", bodyJsonSchema, (propertyName, jsonSchema, parentJsonSchema) -> {
            JsonPropertyDescriptionValueDto jpdv = toJpdvSkipNull(jsonSchema.getDescription());
            if (jpdv != null) {
                jsonSchema.setDescription(jpdvToStringProc.toString(jpdv));
            }
        });
        return JsonUtils.toJson(bodyJsonSchema);
    }

    public Map<String, Long> getYapiCatIdsEachName(Long projectId) {
        List<InterfaceListMenuRespDto> cats = yApiOpenProc.listCats(projectId);
        Map<String, Long> result = Maps.newHashMap();
        for (InterfaceListMenuRespDto cat : cats) {
            result.put(cat.getName(), cat.getId());
        }
        return result;
    }

    private void createYApiCat(Collection<String> catNames, Long projectId) {
        for (String catName : catNames) {
            JsonNode responseBody = yApiOpenProc.createCat("", catName, projectId);
            log.info("create yapi cat. catName={} rawRespBody={}", catName, JsonUtils.toJson(responseBody));
        }
    }

    private Map<String, JsonNode> listAutoInterfaces(Long projectId) {
        JsonNode jsonNode = yApiOpenProc.listCatsAsJsonNode(projectId);
        Map<String, JsonNode> result = Maps.newHashMap();
        for (JsonNode data : jsonNode) {
            for (JsonNode interf : data.get("list")) {
                List<String> tags = JsonUtils.toListOfObject(interf.get("tag").toString(), String.class);
                if (tags.contains(YApiConstant.ALLISON_1875_TAG)) {
                    result.put(interf.get("path").asText(), interf);
                }
            }
        }
        return result;
    }

    private void ensureSuccess(JsonNode respNode) {
        if (respNode == null) {
            throw new YapiException();
        }
        if (respNode.get("errcode").asInt() != 0 || respNode.get("data") == null) {
            throw new YapiException(respNode.toString());
        }
    }

    private void deleteInterface(JsonNode jsonNode, Long recycleBinCatId) {
        if (recycleBinCatId.equals(jsonNode.get("catid").asLong())) {
            // 已在"回收站"分类中
            return;
        }
        Long id = jsonNode.get("_id").asLong();

        JsonNode respNode = yApiOpenProc.getEndpoint(id);
        ensureSuccess(respNode);

        Map<String, Object> body = Maps.newHashMap();
        body.put("id", id);
        body.put("catid", recycleBinCatId);
        List<String> tags = JsonUtils.toListOfObject(jsonNode.get("tag").toString(), String.class);
        tags.add(YApiConstant.DELETE_TAG);
        body.put("tag", tags);

        String desc = "";
        JsonNode descNode = respNode.get("data").get("desc");
        if (descNode != null) {
            desc = descNode.asText();
        }
        String deleteMessage = MarkdownUtils.convertToHtml("> 该接口已被删除，或是它的URL已被更改，**禁止调用**\n");
        deleteMessage = MoreStringUtils
                .replaceLast(deleteMessage, "<strong>", "<span style='background:black;color:#FFD9E6'>");
        deleteMessage = MoreStringUtils.replaceLast(deleteMessage, "</strong>", "</span>");

        body.put("desc", deleteMessage + desc);
        body.put("token", docAnalyzerConfig.getYapiToken());
        JsonNode jsonNode1 = yApiOpenProc.updateEndpoint(body);
        log.info(JsonUtils.toJson(jsonNode1));
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
        form.put("tag", Lists.newArrayList(YApiConstant.ALLISON_1875_TAG));
        form.put("desc", MarkdownUtils.convertToHtml(description));
        form.put("method", httpMethod);
        form.put("catid", catId);
        form.put("token", docAnalyzerConfig.getYapiToken());
        JsonNode responseBody = yApiOpenProc.createOrUpdateEndpoint(form);
        log.info(JsonUtils.toJson(responseBody));
    }

    private JsonPropertyDescriptionValueDto toJpdvSkipNull(String nullableJson) {
        if (nullableJson == null) {
            return null;
        }
        try {
            return JsonUtils.createObjectMapper().readValue(nullableJson, JsonPropertyDescriptionValueDto.class);
        } catch (Exception e) {
            log.info("jpdv has been pretty [{}]", StringEscapeUtils.escapeJava(nullableJson));
        }
        return null;
    }

}