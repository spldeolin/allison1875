package com.spldeolin.allison1875.base.ancestor.newedition;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-15
 */
@Log4j2
public abstract class Allison1875Config extends Allison1875Component {

    @Override
    @Inject
    protected final void configure() {
        log.info("detect config properties {}", this);
        super.configure();
    }

}