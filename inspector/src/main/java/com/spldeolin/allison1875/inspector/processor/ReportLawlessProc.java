package com.spldeolin.allison1875.inspector.processor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import com.spldeolin.allison1875.base.util.CsvUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.TimeUtils;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Log4j2
public class ReportLawlessProc {

    private Collection<LawlessDto> lawlesses;

    public void process() {
        String lawlessDirectoryPath = Inspector.CONFIG.get().getLawlessDirectoryPath();
        lawlesses.forEach(log::info);

        if (!StringUtils.isEmpty(lawlessDirectoryPath)) {
            String csvContent = CsvUtils.writeCsv(lawlesses, LawlessDto.class);
            String fileName = "lawless-" + TimeUtils.toString(LocalDateTime.now(), "yyyyMMdd");
            Path outputDirectory = Paths.get(lawlessDirectoryPath);
            Path csvFile = outputDirectory.resolve(fileName + ".csv");
            Path csvGbkFile = outputDirectory.resolve(fileName + "-gbk.csv");
            try {
                FileUtils.writeStringToFile(csvFile.toFile(), csvContent, StandardCharsets.UTF_8);
                FileUtils.writeStringToFile(csvGbkFile.toFile(), csvContent, "GBK");
                log.info("Lawless print to [{}] completed.", csvFile);
                log.info("Lawless print to [{}] completed.", csvGbkFile);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public ReportLawlessProc lawlesses(Collection<LawlessDto> lawlesses) {
        this.lawlesses = lawlesses;
        return this;
    }

}
