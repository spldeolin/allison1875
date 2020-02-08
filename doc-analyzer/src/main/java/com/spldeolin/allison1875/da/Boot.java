package com.spldeolin.allison1875.da;

import java.util.Collection;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.Config;
import com.spldeolin.allison1875.da.core.domain.ApiDomain;
import com.spldeolin.allison1875.da.core.processor.ApiProcessor;
import com.spldeolin.allison1875.da.core.processor.HandlerProcessor;
import com.spldeolin.allison1875.da.core.processor.result.HandlerProcessResult;
import com.spldeolin.allison1875.da.core.strategy.DefaultHandlerFilter;
import com.spldeolin.allison1875.da.core.strategy.ReturnStmtBaseResponseBodyTypeParser;
import com.spldeolin.allison1875.da.view.rap.RapConverter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-08
 */
@Log4j2
public class Boot {

    public static void main(String[] args) {

        // collect
        Collection<HandlerProcessResult> handlerEntries = new HandlerProcessor().process(new DefaultHandlerFilter() {
            @Override
            public boolean filter(ClassOrInterfaceDeclaration controller) {
                return true;
            }
        }, new ReturnStmtBaseResponseBodyTypeParser());

        // process
        Collection<ApiDomain> apis = Lists.newLinkedList();
        handlerEntries.forEach(entry -> apis.add(new ApiProcessor(entry).process()));

        // convert to view
        String result = new RapConverter().convert(apis);
        log.info(result);
    }

}
