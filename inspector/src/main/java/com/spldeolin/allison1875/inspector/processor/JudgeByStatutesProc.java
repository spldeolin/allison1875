package com.spldeolin.allison1875.inspector.processor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.javabean.VcsResultDto;
import com.spldeolin.allison1875.inspector.statute.Statute;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Singleton
@Log4j2
public class JudgeByStatutesProc {

    @Inject
    private Collection<Statute> statutes;

    @Inject
    private InspectorConfig config;

    @Inject
    private VcsProc vcsProc;

    public Collection<LawlessDto> process(Collection<PardonDto> pardons) {
        final Collection<LawlessDto> lawlesses = Lists.newArrayList();

        VcsResultDto vcsResultDto = vcsProc.process(Paths.get(config.getProjectLocalGitPath()));

        AstForestContext.getCurrent().forEach(cu -> {
            Path cuPath = Locations.getAbsolutePath(cu);
            if (vcsResultDto.getIsAllTarget() || vcsResultDto.getAddedFiles().contains(cuPath)) {
                long start = System.currentTimeMillis();
                if (statutes != null) {
                    for (Statute statute : statutes) {
                        Collection<LawlessDto> dtos = statute.inspect(cu);
                        dtos.forEach(dto -> {
                            String statuteNo = statute.declareStatuteNo();
                            if (isNotInPublicAcks(dto, statuteNo, pardons)) {
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

        lawlesses.addAll(lawlesses.stream().sorted(Comparator.comparing(LawlessDto::getStatuteNo))
                .collect(Collectors.toList()));

        log.info("All inspections completed");
        return lawlesses;
    }

    private boolean isNotInPublicAcks(LawlessDto vo, String statuteNo, Collection<PardonDto> pardons) {
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
