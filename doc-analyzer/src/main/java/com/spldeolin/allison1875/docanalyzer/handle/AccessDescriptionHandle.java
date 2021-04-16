package com.spldeolin.allison1875.docanalyzer.handle;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2021-04-06
 */
@Singleton
public class AccessDescriptionHandle {

    public Collection<String> accessMethod(HandlerFullDto handlerFullDto) {
        return JavadocDescriptions.getAsLines(handlerFullDto.getMd());
    }

    public Collection<String> accessField(FieldDeclaration field) {
        return JavadocDescriptions.getAsLines(field);
    }

}