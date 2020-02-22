package com.spldeolin.allison1875.si;

import static com.spldeolin.allison1875.base.BaseConfig.CONFIG;

import java.util.Collections;
import java.util.List;
import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.Locations;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 报告出所有方法的行数
 *
 * 行数包含方法签名，低于80行的方法不会被报告
 *
 * @author Deolin 2020-02-10
 */
@Log4j2
public class MethodLineNumberReporter {

    public static void main(String[] args) {
        new MethodLineNumberReporter().process();
    }

    private void process() {
        CONFIG.setDoNotCollectWithLoadingClass(true);
        List<LineNumber> lineNumbers = Lists.newArrayList();
        StaticGitAddedFileContainer.removeIfNotContain(StaticAstContainer.getCompilationUnits()).forEach(

                cu -> cu.findAll(MethodDeclaration.class, method -> method.getBody().isPresent()).forEach(method -> {
                    String qualifier = method.findAncestor(TypeDeclaration.class).map(td -> (TypeDeclaration<?>) td)
                            .orElseThrow(() -> new RuntimeException(method.getName() + "没有声明在类中"))
                            .getFullyQualifiedName().orElseThrow(QualifierAbsentException::new) + method.getName();
                    Range range = Locations.getRange(method);
                    int number = range.end.line - range.begin.line + 1;
                    if (number > 80) {
                        lineNumbers.add(new LineNumber(qualifier, number));
                    }

                }));
        Collections.sort(lineNumbers);
        lineNumbers.forEach(LineNumber::report);
    }

    @AllArgsConstructor
    private static class LineNumber implements Comparable<LineNumber> {

        String qualifier;

        Integer number;

        @Override
        public int compareTo(LineNumber that) {
            return that.number.compareTo(this.number);
        }

        public void report() {
            log.info("[{}] {}", number, qualifier);
        }

    }

}
