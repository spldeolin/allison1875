package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.dto.CategorizedMarkdownDTO;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.service.ShowdocService;
import com.spldeolin.allison1875.docanalyzer.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2025-03-15
 */
@Singleton
@Slf4j
public class ShowdocServiceImpl implements ShowdocService {

    @Inject
    private Config config;

    @Inject
    private MarkdownService markdownService;

    @Override
    public void flushToShowdoc(List<EndpointDTO> endpoints) {
        List<CategorizedMarkdownDTO> categorizedMds = markdownService.categorizeMarkdowns(endpoints);
        for (CategorizedMarkdownDTO categorizedMd : categorizedMds) {
            List<String> catNames = Lists.newArrayList(categorizedMd.getHierarchicalCategories());
            catNames.add(0, config.getShowdocBaseCatName());
            catNames.removeIf(StringUtils::isBlank);
            String catName = Joiner.on("/").join(catNames);
            String pageTitle = categorizedMd.getDirectCategory();

            Map<String, String> formData = Maps.newHashMap();
            formData.put("api_key", config.getShowdocApiKey());
            formData.put("api_token", config.getShowdocApiToken());
            formData.put("cat_name", catName);
            formData.put("page_title", pageTitle);
            formData.put("page_content", categorizedMd.getContent());
            JsonNode responseBody = HttpUtils.formPost(config.getShowdocUrl(), formData);
            ensureSuccess(responseBody);
            log.info("create showdoc page. catName={} pageTitle={} rawRespBody={}", catName, pageTitle,
                    JsonUtils.toJson(responseBody));
        }
    }

    private void ensureSuccess(JsonNode node) {
        if (node == null) {
            throw new Allison1875Exception("node is null");
        }
        if (node.get("error_code").asInt() != 0 || node.get("data") == null) {
            throw new Allison1875Exception(node.toString());
        }
    }

}
