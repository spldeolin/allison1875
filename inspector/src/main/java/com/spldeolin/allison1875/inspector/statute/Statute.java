package com.spldeolin.allison1875.inspector.statute;

import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;

/**
 * 该接口用于抽象一条规约
 *
 * @author Deolin 2020-02-22
 */
public interface Statute {

    /**
     * 对一个CU进行规约检查，返回不合规的地方（如果有的话）
     */
    Collection<LawlessDto> inspect(CompilationUnit cu);

    /**
     * 声明该规约的规约号
     */
    String declareStatuteNo();

    /**
     * 声明该规约的描述
     */
    String declareStatuteDescription();

}
