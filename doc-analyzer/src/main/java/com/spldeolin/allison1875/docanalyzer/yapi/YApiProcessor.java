package com.spldeolin.allison1875.docanalyzer.yapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-26
 */
@Log4j2
public class YApiProcessor {

    public void process() {
        String url = "http://localhost:3000";
        String token = "c43cae37d890bc0cbef59cd2bb1b4066c7877f181f9fed44dce646da1fa142ea";
        JsonNode projectGetDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/project/get" + "?token=" + token));
        if (projectGetDto == null) {
            return;
        }
        Long projectId = projectGetDto.get("_id").asLong();

        JsonNode interfaceListMenuDto = ensureSusscessAndToGetData(
                HttpUtils.get(url + "/api/interface/list_menu" + "?token=" + token + "&project_id" + projectId));

        System.out.println(interfaceListMenuDto);
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

    public static void main(String[] args) {
        new YApiProcessor().process();
    }

}