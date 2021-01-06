package com.spldeolin.allison1875.base;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-09
 */
@Slf4j
public class Version {

    public static final String numberCode = "2.0-SNAPSHOT";

    public static final String title = "Allison 1875 " + numberCode;

    public static void greeting() {
        log.info(title);
    }

}