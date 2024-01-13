package com.spldeolin.allison1875.inspector;

import java.util.Collection;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ancestor.Allison1875MainService;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.service.DetectPardonService;
import com.spldeolin.allison1875.inspector.service.JudgeByStatutesService;
import com.spldeolin.allison1875.inspector.service.ReportLawlessService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-08-31
 */
@Singleton
@Log4j2
public class Inspector implements Allison1875MainService {

    @Inject
    private DetectPardonService pardonDetectProc;

    @Inject
    private ReportLawlessService reportLawlessProc;

    @Inject
    private JudgeByStatutesService judgeByStatutesProc;

    @Override
    public void process(AstForest astForest) {
        Collection<PardonDto> pardons = pardonDetectProc.detect();
        Collection<LawlessDto> lawlesses = judgeByStatutesProc.judge(pardons, astForest);
        reportLawlessProc.report(lawlesses);
    }

}