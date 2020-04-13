package com.spldeolin.allison1875.da.core.processor;

import static com.spldeolin.allison1875.da.DocAnalyzerConfig.CONFIG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-03-10
 */
@Log4j2
class MavenPackageProcessor {

    void process() {
        Runtime run = Runtime.getRuntime();
        try {
            Process proc = run.exec(new String[]{"/bin/bash", "-c", CONFIG.getMavenPackageCommandLine()});
            try (InputStream in = proc.getInputStream(); BufferedReader br = new BufferedReader(
                    new InputStreamReader(in))) {
                String message;
                while ((message = br.readLine()) != null) {
                    log.info("\t" + message);
                }
            }
        } catch (IOException e) {
            log.error("something wasn't right here.", e);
            System.exit(-1);
        }

    }

}
