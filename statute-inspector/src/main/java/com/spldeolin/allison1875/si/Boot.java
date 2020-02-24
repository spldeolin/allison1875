package com.spldeolin.allison1875.si;

import com.spldeolin.allison1875.si.processor.InspectionProcessor;
import com.spldeolin.allison1875.si.processor.LawlessReportProcessor;
import com.spldeolin.allison1875.si.processor.PublicAckProcessor;

/**
 * @author Deolin 2020-02-22
 */
public class Boot {

    public static void main(String[] args) {
        PublicAckProcessor publicAckP = new PublicAckProcessor().process();

        InspectionProcessor inspectionP = new InspectionProcessor().publicAcks(publicAckP.publicAcks()).process();

        new LawlessReportProcessor().lawlesses(inspectionP.lawlesses()).report();
    }

}
