package com.spldeolin.allison1875.docanalyzer.handleimpl;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.docanalyzer.handle.AccessDescriptionHandle;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;
import lombok.extern.java.Log;

/**
 * @author Deolin 2021-04-06
 */
@Singleton
@Log
public class DefaultAccessDescriptionHandle implements AccessDescriptionHandle {

    @Override
    public Collection<String> accessMethod(HandlerFullDto handlerFullDto) {
        return JavadocDescriptions.getAsLines(handlerFullDto.getMd());
    }

    @Override
    public Collection<String> accessField(FieldDeclaration field) {
        return JavadocDescriptions.getAsLines(field);
    }

}