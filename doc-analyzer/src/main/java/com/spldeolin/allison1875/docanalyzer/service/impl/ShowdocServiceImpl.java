package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.CategorizedMarkdownDTO;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.service.ShowdocService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Deolin 2025-03-15
 */
@Singleton
@Slf4j
public class ShowdocServiceImpl implements ShowdocService {

    @Inject
    private OkHttpClient okHttpClient;

    @Inject
    private DocAnalyzerConfig config;

    @Inject
    private MarkdownService markdownService;

    @Override
    public void flushToShowdoc(List<EndpointDTO> endpoints) {
        List<CategorizedMarkdownDTO> categorizedMds = markdownService.categorizeMarkdowns(endpoints);
        for (CategorizedMarkdownDTO categorizedMd : categorizedMds) {
            List<String> catNames = categorizedMd.getHierarchicalCategories();
            catNames.add(0, config.getShowdocBaseCatName());
            catNames.removeIf(StringUtils::isBlank);
            String catName = Joiner.on("/").join(catNames);
            String pageTitle = categorizedMd.getDirectCategory();

            RequestBody formBody = new FormBody.Builder().add("api_key", config.getShowdocApiKey())
                    .add("api_token", config.getShowdocApiToken()).add("cat_name", catName).add("page_title", pageTitle)
                    .add("page_content", categorizedMd.getContent()).build();
            try (Response response = okHttpClient.newCall(
                    new Builder().url(config.getShowdocUrl()).post(formBody).build()).execute()) {
                if (response.body() == null) {
                    throw new Allison1875Exception("response body absent");
                }
                JsonNode responseBody = JsonUtils.toTree(response.body().string());
                ensureSuccess(responseBody);
                log.info("create showdoc page. catName={} pageTitle={} rawRespBody={}", catName, pageTitle,
                        JsonUtils.toJson(responseBody));
            } catch (Exception e) {
                throw new Allison1875Exception(e);
            }
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
