package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.util.List;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JavadocUtils;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.AccessDescriptionService;

/**
 * @author Deolin 2021-04-06
 */
@Singleton
public class AccessDescriptionServiceImpl implements AccessDescriptionService {

    @Override
    public List<String> accessMethod(HandlerFullDto handlerFullDto) {
        return JavadocUtils.getCommentAsLines(handlerFullDto.getMd());
    }

    @Override
    public List<String> accessField(FieldDeclaration field) {
        return JavadocUtils.getCommentAsLines(field);
    }

}