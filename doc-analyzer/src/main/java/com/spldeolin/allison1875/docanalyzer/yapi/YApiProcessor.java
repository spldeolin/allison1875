package com.spldeolin.allison1875.docanalyzer.yapi;

import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-26
 */
@Log4j2
public class YApiProcessor {

    final String url = "http://localhost:3000";

    final String token = "c43cae37d890bc0cbef59cd2bb1b4066c7877f181f9fed44dce646da1fa142ea";

    final Long projectId;

    public YApiProcessor() {
        JsonNode projectGetDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/project/get" + "?token=" + token));
        if (projectGetDto == null) {
            throw new RuntimeException("YApi project absent.");
        }
        projectId = projectGetDto.get("_id").asLong();
    }

    public Map<String, Long> getYapiCatIdsEachName() {
        JsonNode interfaceListMenuDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/interface/list_menu" + "?token=" + token + "&project_id" + projectId));
        Map<String, Long> result = Maps.newHashMap();
        for (JsonNode cat : interfaceListMenuDto) {
            result.put(cat.get("name").asText(), cat.get("_id").asLong());
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
                HttpUtils.get(url + "/api/interface/list_menu" + "?token=" + token + "&project_id" + projectId));

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
        Map<String, Object> form = Maps.newHashMap();
        form.put("id", jsonNode.get("_id").asLong());
        form.put("catid", recycleBinCatId);
        String desc = "";
        JsonNode descNode = jsonNode.get("desc");
        if (descNode != null) {
            desc = descNode.asText();
        }
        form.put("desc", "<h1 align='center' style='color:red'>这个URL已不再使用</h1>" + desc);
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
        form.put("desc", description);
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

}