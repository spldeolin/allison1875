package com.spldeolin.allison1875.persistencegenerator.service;

import java.util.List;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.service.impl.TableAnalyzerServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(TableAnalyzerServiceImpl.class)
public interface TableAnalyzerService {

    List<TableAnalysisDTO> analyzeTable();

}