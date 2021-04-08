package com.spldeolin.allison1875.base;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-12-09
 */
@Log4j2
public class Version {

    public static final String numberCode = "5.3-SNAPSHOT";

    public static final String title = "Allison 1875 " + numberCode;

    public static void greeting() {
        log.info(title);
    }

}