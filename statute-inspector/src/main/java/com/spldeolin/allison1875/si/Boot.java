package com.spldeolin.allison1875.si;

import java.util.Collection;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.si.processor.InspectionProcessor;
import com.spldeolin.allison1875.si.processor.LawlessReportProcessor;
import com.spldeolin.allison1875.si.statute.MultiNewResponseInfoStatute;
import com.spldeolin.allison1875.si.statute.NormalControllerStatute;
import com.spldeolin.allison1875.si.statute.Statute;
import com.spldeolin.allison1875.si.statute.UncommittedModifiedFileStatute;

/**
 * @author Deolin 2020-02-22
 */
public class Boot {

    public static void main(String[] args) {
        Collection<Statute> statutes = Lists
                .newArrayList(new UncommittedModifiedFileStatute(), new NormalControllerStatute(),
                        new MultiNewResponseInfoStatute());
        InspectionProcessor inspectionP = new InspectionProcessor().statutes(statutes).processor();

        new LawlessReportProcessor().lawlesses(inspectionP.lawlesses()).report();
    }

}
