package com.spldeolin.allison1875.inspector.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.inspector.javabean.LawlessDto;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.service.impl.JudgeByStatutesServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(JudgeByStatutesServiceImpl.class)
public interface JudgeByStatutesService {

    Collection<LawlessDto> process(Collection<PardonDto> pardons, AstForest astForest);

}
