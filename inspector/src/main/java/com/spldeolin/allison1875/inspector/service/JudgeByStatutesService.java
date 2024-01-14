package com.spldeolin.allison1875.inspector.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.service.impl.JudgeByStatutesServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JudgeByStatutesServiceImpl.class)
public interface JudgeByStatutesService {

    List<LawlessDto> judge(List<PardonDto> pardons, AstForest astForest);

}
