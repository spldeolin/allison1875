package com.spldeolin.allison1875.si.statute;

import java.util.Collection;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.util.Locations;
import com.spldeolin.allison1875.base.util.MethodQualifiers;
import com.spldeolin.allison1875.si.vo.LawlessVo;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出所有方法的行数
 *
 * 行数包含方法签名，低于200行的方法不会被报告
 *
 * @author Deolin 2020-02-10
 */
@Log4j2
public class MethodLineNumberStatute implements Statute {

    @Override
    public Collection<LawlessVo> inspect(Collection<CompilationUnit> cus) {
        Collection<LawlessVo> result = Lists.newLinkedList();
        StaticGitAddedFileContainer.removeIfNotContain(cus).forEach(
                cu -> cu.findAll(MethodDeclaration.class, method -> method.getBody().isPresent()).forEach(method -> {
                    Range range = Locations.getRange(method);
                    int lineCount = range.end.line - range.begin.line + 1;
                    if (lineCount > 200) {
                        LawlessVo vo = new LawlessVo(method, MethodQualifiers.getTypeQualifierWithMethodName(method));
                        result.add(vo.setMessage("行数已超过200，当前：" + lineCount));
                    }
                }));
        return result;
    }

    @Override
    public String getStatuteNo() {
        return "5yya";
    }

}
