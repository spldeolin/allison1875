package com.spldeolin.allison1875.gadget.processor;

import org.apache.commons.io.FileUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import lombok.extern.log4j.Log4j2;

/**
 * Bak文件清理器
 *
 * 用于删除使用[handler-transformer]、[persistence-generator]、[query-transformer]过程中产生的大量的分散在各处的bak文件
 *
 * @author Deolin 2020-10-22
 */
@Singleton
@Log4j2
public class BakFileCleaner implements Allison1875MainProcessor {

    @Override
    public void process(AstForest astForest) {
        FileUtils.iterateFiles(astForest.getHost().toFile(), new String[]{"bak"}, true).forEachRemaining(bakFile -> {
            log.info(bakFile.toString() + " 被删除");
            bakFile.deleteOnExit();
        });
    }

}