package com.spldeolin.allison1875.da;

import java.util.Collection;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.processor.MainProcessor;
import com.spldeolin.allison1875.da.view.markdown.MarkdownConverter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-08
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {
        // main process
        Collection<ApiDomain> apis = new MainProcessor().process();

        // convert to view
        new MarkdownConverter().convert(apis);
    }

}
