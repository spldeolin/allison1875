package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.si.vo.LawlessVo;

/**
 * @author Deolin 2020-02-22
 */
public interface Statute {

    Collection<LawlessVo> inspect(Collection<CompilationUnit> cus);

}
