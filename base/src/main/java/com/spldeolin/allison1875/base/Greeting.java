package com.spldeolin.allison1875.base;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-12-09
 */
@Singleton
@Slf4j
public class Greeting {

    public void version() {
        log.info("Allison 1875 v1.0");
    }

}