package com.spldeolin.allison1875.inspector.processor;

import java.util.Collection;
import java.util.Set;
import javax.validation.ConstraintViolation;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.inspector.InspectorConfig;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;
import com.spldeolin.allison1875.inspector.dto.PardonDto;
import com.spldeolin.allison1875.inspector.statute.Statute;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-31
 */
@Log4j2
public class Inspector implements Allison1875MainProcessor<InspectorConfig, Inspector> {

    protected Collection<Statute> statutes = Lists.newArrayList();

    public static final ThreadLocal<InspectorConfig> CONFIG = ThreadLocal.withInitial(InspectorConfig::new);

    @Override
    public Inspector config(InspectorConfig config) {
        Set<ConstraintViolation<InspectorConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<InspectorConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        CONFIG.set(config);
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);

        DetectPardonProc pardonDetectProc = new DetectPardonProc().process();
        Collection<PardonDto> pardons = pardonDetectProc.pardons();

        JudgeByStatutesProc judgeByStatutesProc = new JudgeByStatutesProc().statutes(statutes).pardons(pardons)
                .process();
        Collection<LawlessDto> lawlesses = judgeByStatutesProc.lawlesses();

        new ReportLawlessProc().lawlesses(lawlesses).process();
    }

    public Inspector statutes(Collection<Statute> statutes) {
        this.statutes = statutes;
        return this;
    }

}