package com.spldeolin.allison1875.si.processor;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.si.statute.Statute;
import com.spldeolin.allison1875.si.vo.LawlessVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Log4j2
@Accessors(fluent = true)
public class InspectionProcessor {

    @Setter
    private Collection<Statute> statutes;

    @Getter
    private Collection<LawlessVo> lawlesses = Lists.newLinkedList();

    public InspectionProcessor processor() {
        statutes.forEach(statute -> {
            Collection<LawlessVo> vos = statute.inspect(StaticAstContainer.getCompilationUnits());
            vos.forEach(vo -> vo.setStatuteNo(statute.getStatuteNo()));
            lawlesses.addAll(vos);
        });
        return this;
    }

}
