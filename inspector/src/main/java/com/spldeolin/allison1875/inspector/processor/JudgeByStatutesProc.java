package com.spldeolin.allison1875.inspector.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;
import com.spldeolin.allison1875.inspector.dto.PardonDto;
import com.spldeolin.allison1875.inspector.statute.Statute;
import com.spldeolin.allison1875.inspector.vcs.StaticVcsContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Log4j2
@Accessors(fluent = true)
public class JudgeByStatutesProc {

    @Setter
    private Collection<PardonDto> pardons;

    @Setter
    private Collection<Statute> statutes;

    @Getter
    private final Collection<LawlessDto> lawlesses = Lists.newArrayList();

    public JudgeByStatutesProc process() {
        final Collection<LawlessDto> lawlesses = Lists.newArrayList();
        AstForestContext.getCurrent().forEach(cu -> {
            if (StaticVcsContainer.contain(cu)) {
                long start = System.currentTimeMillis();

                if (statutes != null) {
                    for (Statute statute : statutes) {
                        Collection<LawlessDto> dtos = statute.inspect(cu);
                        dtos.forEach(dto -> {
                            String statuteNo = statute.declareStatuteNo();
                            if (isNotInPublicAcks(dto, statuteNo)) {
                                dto.setStatuteNo(statuteNo);
                                lawlesses.add(dto);
                            }
                        });
                    }
                }

                log.info("CompilationUnit [{}] inspection completed with [{}]ms.", Locations.getRelativePath(cu),
                        System.currentTimeMillis() - start);
            }
        });

        this.lawlesses.addAll(lawlesses.stream().sorted(Comparator.comparing(LawlessDto::getStatuteNo))
                .collect(Collectors.toList()));

        log.info("All inspections completed");
        return this;
    }

    private boolean isNotInPublicAcks(LawlessDto vo, String statuteNo) {
        String qualifier = vo.getQualifier();
        String sourceCode = vo.getSourceCode();

        for (PardonDto pa : pardons) {
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
