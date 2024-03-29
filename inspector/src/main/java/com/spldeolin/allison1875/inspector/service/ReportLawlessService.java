package com.spldeolin.allison1875.inspector.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.service.impl.ReportLawlessServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(ReportLawlessServiceImpl.class)
public interface ReportLawlessService {

    void report(List<LawlessDto> lawlesses);

}
