package com.spldeolin.allison1875.persistencegenerator.processor;

import org.apache.commons.lang3.StringUtils;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.HashUtil;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;

/**
 * @author Deolin 2021-01-03
 */
@Singleton
public class AnchorProc {

    public String createLeftAnchor(PersistenceDto persistence) {
        String target = persistence.getMapperName();
        return StringUtils.left(HashUtil.md5(target), 6);
    }

    public String createRightAnchor(PersistenceDto persistence) {
        String target = persistence.getMapperName();
        return StringUtils.right(HashUtil.md5(target), 6);
    }

}