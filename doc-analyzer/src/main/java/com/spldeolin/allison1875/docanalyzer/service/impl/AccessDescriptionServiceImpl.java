package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.AccessDescriptionService;

/**
 * @author Deolin 2021-04-06
 */
@Singleton
public class AccessDescriptionServiceImpl implements AccessDescriptionService {

    @Override
    public Collection<String> accessMethod(HandlerFullDto handlerFullDto) {
        return JavadocDescriptions.getAsLines(handlerFullDto.getMd());
    }

    @Override
    public Collection<String> accessField(FieldDeclaration field) {
        return JavadocDescriptions.getAsLines(field);
    }

}