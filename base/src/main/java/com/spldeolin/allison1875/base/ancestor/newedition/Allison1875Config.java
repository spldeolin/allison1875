package com.spldeolin.allison1875.base.ancestor.newedition;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-15
 */
@Slf4j
public abstract class Allison1875Config extends Allison1875Component {

    @Override
    @Inject
    protected final void configure() {
        log.info("detect config properties {}", this);
        super.configure();
    }

}