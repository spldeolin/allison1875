package com.spldeolin.allison1875.inspector.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;

/**
 * @author Deolin 2020-02-22
 */
public interface Statute {

    Collection<LawlessDto> inspect(CompilationUnit cu);

}
