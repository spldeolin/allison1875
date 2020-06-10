package com.spldeolin.allison1875.da;

import com.spldeolin.allison1875.da.processor.MainProcessor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-01
 */
@Log4j2
public class DocAnanlyzerBoot {

    public static void main(String[] args) {
        new MainProcessor().process();
    }

}
