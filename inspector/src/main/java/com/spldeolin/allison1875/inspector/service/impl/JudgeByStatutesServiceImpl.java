package com.spldeolin.allison1875.inspector.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.ast.Locations;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.javabean.VcsResultDto;
import com.spldeolin.allison1875.inspector.service.JudgeByStatutesService;
import com.spldeolin.allison1875.inspector.statute.Statute;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-02-22
 */
@Singleton
@Log4j2
public class JudgeByStatutesServiceImpl implements JudgeByStatutesService {

    @Inject
    private Collection<Statute> statutes;

    @Inject
    private InspectorConfig config;

    @Inject
    private VcsServiceImpl vcsProc;

    @Override
    public Collection<LawlessDto> judge(Collection<PardonDto> pardons, AstForest astForest) {
        final Collection<LawlessDto> lawlesses = Lists.newArrayList();

        VcsResultDto vcsResultDto = vcsProc.analyze(Paths.get(config.getProjectLocalGitPath()));

        astForest.forEach(cu -> {
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

                log.info("CompilationUnit [{}] inspection completed with [{}]ms.", Locations.getAbsolutePath(cu),
                        System.currentTimeMillis() - start);
            }
        });

        lawlesses.addAll(
                lawlesses.stream().sorted(Comparator.comparing(LawlessDto::getStatuteNo)).collect(Collectors.toList()));

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
