package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2021-01-03
 */
@Singleton
public class AnchorProc {

    private static final HashFunction hash = Hashing.hmacMd5("Allison 1875".getBytes(StandardCharsets.UTF_8));

    public String createLeftAnchor(PersistenceDto persistence) {
        String target = persistence.getMapperName();
        String hashCode = hash.hashString(target, StandardCharsets.UTF_8).toString();
        return StringUtils.left(hashCode, 6);
    }

    public String createRightAnchor(PersistenceDto persistence) {
        String target = persistence.getMapperName();
        String hashCode = hash.hashString(target, StandardCharsets.UTF_8).toString();
        return StringUtils.right(hashCode, 6);
    }

}