package com.spldeolin.allison1875.inspector.processor;

import java.util.Collection;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-31
 */
@Singleton
@Log4j2
public class Inspector implements Allison1875MainProcessor {

    @Inject
    private DetectPardonProc pardonDetectProc;

    @Inject
    private ReportLawlessProc reportLawlessProc;

    @Inject
    private JudgeByStatutesProc judgeByStatutesProc;

    @Override
    public void process(AstForest astForest) {
        Collection<PardonDto> pardons = pardonDetectProc.process();
        Collection<LawlessDto> lawlesses = judgeByStatutesProc.process(pardons, astForest);
        reportLawlessProc.process(lawlesses);
    }

}