package com.spldeolin.allison1875.base;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-09
 */
@Log4j2
public class Version {

    public static final String logDisplayVersion = "Allison 1875 8.7-SNAPSHOT";

    public static final String lotNoVersion = "0807S";

    public static void greeting() {
        log.info(logDisplayVersion);
    }

}