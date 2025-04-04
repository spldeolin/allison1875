package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.dto.CategorizedMarkdownDTO;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.service.EndpointDslService;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2025-03-17
 */
@Slf4j
@Singleton
public class EndpointDslServiceImpl implements EndpointDslService {

    private static final String illegalChars = "\\/:*?\"<>|";

    @Inject
    private MarkdownService markdownService;

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void flushToEndpointDsl(List<EndpointDTO> endpoints) {
        List<CategorizedMarkdownDTO> categorizedMds = markdownService.categorizeMarkdowns(endpoints);

        for (CategorizedMarkdownDTO categorizedMd : categorizedMds) {
            StringBuilder dirPath = new StringBuilder(config.getDslDir().getPath());
            if (CollectionUtils.isNotEmpty(categorizedMd.getHierarchicalCategories())) {
                for (String hierarchicalCategory : categorizedMd.getHierarchicalCategories()) {
                    dirPath.append(File.separator).append(sanitizeFileName(hierarchicalCategory));
                }
            }
            if (!new File(dirPath.toString()).exists()) {
                new File(dirPath.toString()).mkdirs();
            }
            File json = new File(
                    dirPath + File.separator + sanitizeFileName(categorizedMd.getDirectCategory()) + ".json");
            try {
                FileUtils.writeStringToFile(new File(
                                dirPath + File.separator + sanitizeFileName(categorizedMd.getDirectCategory()) +
                                        ".json"),
                        JsonUtils.toJsonPrettily(categorizedMd.getEndpointGroup()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            log.info("create dsl file. file={}", json);
        }
    }

    private String sanitizeFileName(String fileName) {
        StringBuilder sanitized = new StringBuilder();
        for (char c : fileName.toCharArray()) {
            if (illegalChars.indexOf(c) != -1) { // 直接检查非法字符
                log.warn("replaced illegal character: '{}'", c);
                sanitized.append('_');
            } else {
                sanitized.append(c);
            }
        }
        // 清理首尾空格
        String processed = sanitized.toString().trim();
        if (StringUtils.isEmpty(processed)) {
            return "_";
        }
        return processed;
    }

}
