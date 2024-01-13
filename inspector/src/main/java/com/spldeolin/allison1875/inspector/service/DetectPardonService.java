package com.spldeolin.allison1875.inspector.service;

import java.util.Collection;
import com.google.inject.ImplementedBy;
import com.spldeolin.allison1875.inspector.javabean.PardonDto;
import com.spldeolin.allison1875.inspector.service.impl.DetectPardonServiceImpl;

/**
 * @author Deolin 2023-12-24
 */
@ImplementedBy(DetectPardonServiceImpl.class)
public interface DetectPardonService {

    Collection<PardonDto> detect();

}
