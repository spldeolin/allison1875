package com.spldeolin.allison1875.sqlapigenerator;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.ast.FileFlush;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.sqlapigenerator.javabean.CoidsOnTrackDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ControllerGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.MapperMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.javabean.ServiceMethodGenerationDto;
import com.spldeolin.allison1875.sqlapigenerator.service.GenerateMethodService;
import com.spldeolin.allison1875.sqlapigenerator.service.ListCoidsOnTrackService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2024-01-20
 */
@Slf4j
public class SqlapiGenerator implements Allison1875MainService {

    @Inject
    private ListCoidsOnTrackService listCoidsOnTrackService;

    @Inject
    private GenerateMethodService generateMethodService;

    @Override
    public void process(AstForest astForest) {
        List<FileFlush> flushes = Lists.newArrayList();

        CoidsOnTrackDto coidsOnTrack = listCoidsOnTrackService.listCoidsOnTrack(astForest);
        log.info("list coidsOnTrack={}", coidsOnTrack);

        List<FileFlush> xmlFlushes = generateMethodService.generateMapperXmlMethod(coidsOnTrack.getMapperXmls());
        flushes.addAll(xmlFlushes);

        // TODO add method to mapperXmls

        MapperMethodGenerationDto mapperMethodGeneration = generateMethodService.generateMapperMethod(coidsOnTrack);
        flushes.addAll(mapperMethodGeneration.getFlushes());

        // TODO add method to mapper

        ServiceMethodGenerationDto serviceMethodGeneration = generateMethodService.generateServiceMethod(coidsOnTrack);
        flushes.addAll(serviceMethodGeneration.getFlushes());

        // TODO add mapper to service

        // TODO add method to service and serviceimpls

        ControllerGenerationDto controllerGeneration = generateMethodService.generateControllerMethod(coidsOnTrack);
        flushes.add(controllerGeneration.getFlush());

        // TODO add service to contoller

        // TODO add method controller

        // flush
        if (CollectionUtils.isNotEmpty(flushes)) {
            flushes.forEach(FileFlush::flush);
            log.info("# REMEBER REFORMAT CODE #");
        } else {
            log.warn("no valid Chain transformed");
        }
    }

}