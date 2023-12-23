package com.spldeolin.allison1875.docanalyzer.service;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import com.spldeolin.allison1875.docanalyzer.service.impl.AccessDescriptionServiceImpl;

/**
 * @author Deolin 2023-12-23
 */
@ImplementedBy(AccessDescriptionServiceImpl.class)
public interface AccessDescriptionService {

    Collection<String> accessMethod(HandlerFullDto handlerFullDto);

    Collection<String> accessField(FieldDeclaration field);

}