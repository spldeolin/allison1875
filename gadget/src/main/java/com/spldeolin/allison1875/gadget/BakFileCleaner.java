package com.spldeolin.allison1875.gadget;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;

/**
 * Bak文件清理器
 *
 * 用于删除使用[handler-transformer]、[persistence-generator]、[query-transformer]过程中产生的大量的分散在各处的bak文件
 *
 * @author Deolin 2020-10-22
 */
public class BakFileCleaner implements Allison1875MainProcessor {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(BakFileCleaner.class);

    @Override
    public void preProcess() {
    }

    @Override
    public void process(AstForest astForest) {
        FileUtils.iterateFiles(astForest.getHost().toFile(), new String[]{"bak"}, true).forEachRemaining(bakFile -> {
            log.info(bakFile.toString() + " 被删除");
            bakFile.deleteOnExit();
        });
    }

}