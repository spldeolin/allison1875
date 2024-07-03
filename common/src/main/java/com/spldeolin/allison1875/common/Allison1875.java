package com.spldeolin.allison1875.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;

/**
 * Allison1875
 *
 * @author Deolin 2020-12-06
 */
@Slf4j
public class Allison1875 {

    public static final String SHORT_VERSION = "1100R";

    public static void hello() {
        String banner;
        try {
            banner = Resources.toString(Resources.getResource("allison1875-banner.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.info(banner);
    }

}