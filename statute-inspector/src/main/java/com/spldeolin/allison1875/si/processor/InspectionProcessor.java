package com.spldeolin.allison1875.si.processor;

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.mutable.MutableInt;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticGitAddedFileContainer;
import com.spldeolin.allison1875.si.dto.PublicAckDto;
import com.spldeolin.allison1875.si.statute.StatuteEnum;
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
    private Collection<PublicAckDto> publicAcks;

    @Getter
    private Collection<LawlessVo> lawlesses = Lists.newLinkedList();

    public InspectionProcessor process() {
        Collection<CompilationUnit> cus = StaticGitAddedFileContainer
                .removeIfNotContain(StaticAstContainer.getCompilationUnits());
        MutableInt no = new MutableInt(1);
        Arrays.stream(StatuteEnum.values()).forEach(statuteEnum -> {
            statuteEnum.getStatute().inspect(cus).forEach(vo -> {
                String statuteNo = statuteEnum.getNo();
                if (isNotInPublicAcks(vo, statuteNo)) {
                    vo.setNo(no.getAndAdd(1));
                    vo.setStatuteNo(statuteNo);
                    lawlesses.add(vo);
                }
            });

        });
        return this;
    }

    private boolean isNotInPublicAcks(LawlessVo vo, String statuteNo) {
        String qualifier = vo.getQualifier();
        String sourceCode = vo.getSourceCode();

        for (PublicAckDto pa : publicAcks) {
            if (statuteNo.equals(pa.getStatuteNo())) {
                if (qualifier != null && qualifier.equals(pa.getQualifier())) {
                    return false;
                }
                if (sourceCode.equals(pa.getQualifier())) {
                    return false;
                }
            }
        }
        return true;
    }

}
