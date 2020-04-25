package com.spldeolin.allison1875.da.deprecated;

import java.util.Collection;
import com.spldeolin.allison1875.da.deprecated.core.definition.ApiDefinition;
import com.spldeolin.allison1875.da.deprecated.core.processor.MainProcessor;
import com.spldeolin.allison1875.da.deprecated.view.markdown.MarkdownConverter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-08
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {
        // main process
        Collection<ApiDefinition> apis = new MainProcessor().process();

        // convert to view
        new MarkdownConverter().convert(apis);
    }

}
