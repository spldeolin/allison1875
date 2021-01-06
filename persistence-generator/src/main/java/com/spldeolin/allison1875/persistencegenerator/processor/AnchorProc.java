package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.hash.Hashing;
import com.google.inject.Singleton;

/**
 * @author Deolin 2021-01-03
 */
@Singleton
public class AnchorProc {

    public String createLeftAnchor(File mapperXmlFile, List<String> sourceLines) {
        String target = mapperXmlFile.getName() + ":" + sourceLines.toString();
        String hashCode = Hashing.goodFastHash(64).hashString(target, StandardCharsets.UTF_8).toString();
        return StringUtils.left(hashCode, 6);
    }

    public String createRightAnchor(File mapperXmlFile, List<String> sourceLines) {
        String target = mapperXmlFile.getName() + ":" + sourceLines.toString();
        String hashCode = Hashing.goodFastHash(64).hashString(target, StandardCharsets.UTF_8).toString();
        return StringUtils.right(hashCode, 6);
    }

}