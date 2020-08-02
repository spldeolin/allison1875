package com.spldeolin.allison1875.docanalyzer.yapi;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;
import com.spldeolin.allison1875.docanalyzer.util.MarkdownUtils;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.CommonRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.InterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.ProjectGetRespDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-26
 */
@Log4j2
public class YApiProcessor {

    private static final String url = DocAnalyzerConfig.getInstance().getYapiUrl();

    private static final String token = DocAnalyzerConfig.getInstance().getYapiToken();

    private static final Long projectId;

    static {
        String json = HttpUtils.get(url + "/api/project/get?token=" + token);
        CommonRespDto<ProjectGetRespDto> resp = JsonUtils
                .toParameterizedObject(json, new TypeReference<CommonRespDto<ProjectGetRespDto>>() {
                });
        ensureSuccess(resp);
        projectId = resp.getData().getId();
    }

    public Map<String, Long> getYapiCatIdsEachName() {
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

    public void addCat(Collection<String> catNames) {
        for (String catName : catNames) {
            Map<String, String> form = Maps.newHashMap();
            form.put("desc", "");
            form.put("name", catName);
            form.put("project_id", projectId.toString());
            form.put("token", token);
            HttpUtils.postForm(url + "/api/interface/add_cat", form);
        }

    }

    public Map<String, JsonNode> listInterfaces() {
        JsonNode interfaceListMenuDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/interface/list_menu?token=" + token + "&project_id" + projectId));

        Map<String, JsonNode> result = Maps.newHashMap();
        for (JsonNode jsonNode : interfaceListMenuDto) {
            for (JsonNode interf : jsonNode.get("list")) {
                result.put(interf.get("path").asText(), interf);
            }
        }
        return result;
    }

    public void deleteInterface(JsonNode jsonNode, Long recycleBinCatId) {
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
        String resp = HttpUtils.postForm(url + "/api/interface/up", form);
        log.info(resp);
    }

    public void addInterface(String title, String url, String requestBodyJsonSchema, String responseBodyJsonSchema,
            String description, String httpMethod, Long catId) {
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
        form.put("desc", MarkdownUtils.convertToHtml(description));
        form.put("method", httpMethod);
        form.put("catid", catId);
        form.put("token", token);
        String resp = HttpUtils.postForm(this.url + "/api/interface/save", form);
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
        if (resp.getCode() != 0) {
            throw new YapiException(resp.getErrmsg());
        }
    }

}