package com.spldeolin.allison1875.si.processor;

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.mutable.MutableInt;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.collection.vcs.StaticVcsContainer;
import com.spldeolin.allison1875.si.dto.LawlessDto;
import com.spldeolin.allison1875.si.dto.PublicAckDto;
import com.spldeolin.allison1875.si.statute.StatuteEnum;
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
    private Collection<LawlessDto> lawlesses = Lists.newLinkedList();

    public InspectionProcessor process() {
        Collection<CompilationUnit> cus = StaticVcsContainer
                .removeIfNotContain(StaticAstContainer.getCompilationUnits());
        Arrays.stream(StatuteEnum.values()).forEach(statuteEnum -> {
            long start = System.currentTimeMillis();
            Collection<LawlessDto> dtos = statuteEnum.getStatute().inspect(cus);
            log.info("Statute[{}] inspection completed with [{}]ms.", statuteEnum.getNo(),
                    System.currentTimeMillis() - start);
            dtos.forEach(dto -> {
                String statuteNo = statuteEnum.getNo();
                if (isNotInPublicAcks(dto, statuteNo)) {
                    dto.setStatuteNo(statuteNo);
                    lawlesses.add(dto);
                }
            });
        });
        log.info("All inspections completed");
        return this;
    }

    private boolean isNotInPublicAcks(LawlessDto vo, String statuteNo) {
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
