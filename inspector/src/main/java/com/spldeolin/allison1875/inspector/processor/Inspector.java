package com.spldeolin.allison1875.inspector.processor;

import java.util.Collection;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.inspector.dto.LawlessDto;
import com.spldeolin.allison1875.inspector.dto.PardonDto;

/**
 * @author Deolin 2020-08-31
 */
public class Inspector implements Allison1875MainProcessor {

    @Override
    public void process(AstForest astForest) {
        AstForestContext.setCurrent(astForest);

        DetectPardonProc pardonDetectProc = new DetectPardonProc().process();
        Collection<PardonDto> pardons = pardonDetectProc.pardons();

        JudgeByStatutesProc judgeByStatutesProc = new JudgeByStatutesProc().pardons(pardons).process();
        Collection<LawlessDto> lawlesses = judgeByStatutesProc.lawlesses();

        new ReportLawlessProc().lawlesses(lawlesses).process();
    }

}