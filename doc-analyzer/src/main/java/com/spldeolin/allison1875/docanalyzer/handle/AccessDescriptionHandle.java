package com.spldeolin.allison1875.docanalyzer.handle;

import java.util.Collection;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.spldeolin.allison1875.docanalyzer.javabean.HandlerFullDto;

/**
 * @author Deolin 2021-04-06
 */
public interface AccessDescriptionHandle {

    Collection<String> accessMethod(HandlerFullDto handlerFullDto);

    Collection<String> accessField(FieldDeclaration field);

}
