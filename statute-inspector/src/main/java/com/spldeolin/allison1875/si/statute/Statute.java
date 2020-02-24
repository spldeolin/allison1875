package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.si.dto.LawlessDto;

/**
 * @author Deolin 2020-02-22
 */
public interface Statute {

    Collection<LawlessDto> inspect(Collection<CompilationUnit> cus);

}
