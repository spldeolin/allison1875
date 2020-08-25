package com.spldeolin.allison1875.handlertransformer.processor;

import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;

/**
 * @author Deolin 2020-06-27
 */
public class ServiceFindProcessor {

    private final AstForest forest;

    private final Collection<String> serviceNames;

    private final Map<String, CompilationUnit> cus = Maps.newHashMap();

    private final Map<String, ClassOrInterfaceDeclaration> services = Maps.newHashMap();

    private final Multimap<String, TypeDeclaration<?>> serviceImpls = ArrayListMultimap.create();

    public ServiceFindProcessor(AstForest forest, Collection<String> serviceNames) {
        this.forest = forest;
        this.serviceNames = serviceNames;
    }

    public void findAll() {
        for (CompilationUnit cu : forest) {
            cu.getPrimaryType().ifPresent(pt -> {
                if (isPublicInterface(pt)) {
                    ClassOrInterfaceDeclaration service = pt.asClassOrInterfaceDeclaration();
                    String serviceName = service.getNameAsString();
                    if (!services.containsKey(serviceName)) {
                        if (serviceNames.contains(serviceName)) {
                            cus.put(serviceName, cu);
                            services.put(serviceName, service);
                        }
                    }
                }
            });
        }
        for (CompilationUnit cu : forest.reset()) {
            for (TypeDeclaration<?> td : cu.findAll(TypeDeclaration.class)) {
                services.forEach((serviceName, service) -> {
                    if (isImplementBy(td, service)) {
                        serviceImpls.put(serviceName, td);
                    }
                });
            }
        }
    }

    private boolean isPublicInterface(TypeDeclaration<?> pt) {
        if (pt.isPublic() && pt.isClassOrInterfaceDeclaration()) {
            return pt.asClassOrInterfaceDeclaration().isInterface();
        }
        return false;
    }

    private boolean isImplementBy(TypeDeclaration<?> td, ClassOrInterfaceDeclaration service) {
        String serviceQualifier = service.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        return false;
    }

    public ClassOrInterfaceDeclaration getService(String serviceName) {
        return services.get(serviceName);
    }

    public Collection<TypeDeclaration<?>> getServiceImpl(String serviceName) {
        return serviceImpls.get(serviceName);
    }

    public CompilationUnit getCu(String serviceName) {
        return cus.get(serviceName);
    }

}
