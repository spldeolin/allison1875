package com.spldeolin.allison1875.si;

import com.spldeolin.allison1875.si.processor.InspectionProcessor;
import com.spldeolin.allison1875.si.processor.LawlessReportProcessor;

/**
 * @author Deolin 2020-02-22
 */
public class Boot {

    public static void main(String[] args) {
        InspectionProcessor inspectionP = new InspectionProcessor().process();
        new LawlessReportProcessor().lawlesses(inspectionP.lawlesses()).report();
    }

}
