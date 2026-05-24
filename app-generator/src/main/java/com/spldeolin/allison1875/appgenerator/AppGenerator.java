package com.spldeolin.allison1875.appgenerator;

import com.spldeolin.allison1875.common.guice.Allison1875MainService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-05-24
 */
@Slf4j
public class AppGenerator implements Allison1875MainService {

    @Override
    public void process() {
        log.info("hi there");
    }

}
