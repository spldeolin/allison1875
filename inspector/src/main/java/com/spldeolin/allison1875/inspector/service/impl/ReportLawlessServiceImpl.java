package com.spldeolin.allison1875.inspector.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.service.ReportLawlessService;
import com.spldeolin.allison1875.inspector.util.CsvUtils;
import com.spldeolin.allison1875.inspector.util.TimeUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Singleton
@Log4j2
public class ReportLawlessServiceImpl implements ReportLawlessService {

    @Inject
    private InspectorConfig config;

    @Override
    public void report(Collection<LawlessDto> lawlesses) {
        File lawlessDirectory = config.getLawlessDirectory();
        lawlesses.forEach(log::info);

        if (lawlessDirectory != null) {
            String csvContent = CsvUtils.writeCsv(lawlesses, LawlessDto.class);
            String fileName = "lawless-" + TimeUtils.toString(LocalDateTime.now(), "yyyyMMdd");
            Path outputDirectory = lawlessDirectory.toPath();
            Path csvFile = outputDirectory.resolve(fileName + ".csv");
            Path csvGbkFile = outputDirectory.resolve(fileName + "-gbk.csv");
            try {
                Files.write(csvContent, csvFile.toFile(), StandardCharsets.UTF_8);
                Files.write(csvContent, csvGbkFile.toFile(), Charset.forName("GBK"));
                log.info("Lawless print to [{}] completed.", csvFile);
                log.info("Lawless print to [{}] completed.", csvGbkFile);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

}
