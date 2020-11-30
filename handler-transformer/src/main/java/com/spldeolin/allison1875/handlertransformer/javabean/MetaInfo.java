package com.spldeolin.allison1875.handlertransformer.javabean;

import java.nio.file.Path;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.ImmutableList;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-26
 */
@Log4j2
public class MetaInfo {

    private final String location;

    private final Path sourceRoot;

    private final ClassOrInterfaceDeclaration controller;

    private final String handlerName;

    private final String handlerDescription;

    private final DtoMetaInfo reqBody;

    private final DtoMetaInfo respBody;

    private final ImmutableList<DtoMetaInfo> dtos;

    MetaInfo(String location, Path sourceRoot, ClassOrInterfaceDeclaration controller, String handlerName,
            String handlerDescription, DtoMetaInfo reqBody, DtoMetaInfo respBody, ImmutableList<DtoMetaInfo> dtos) {
        this.location = location;
        this.sourceRoot = sourceRoot;
        this.controller = controller;
        this.handlerName = handlerName;
        this.handlerDescription = handlerDescription;
        this.reqBody = reqBody;
        this.respBody = respBody;
        this.dtos = dtos;
    }

    public static MetaInfoBuilder builder() {
        return new MetaInfoBuilder();
    }

    public boolean isLack() {
        if (StringUtils.isBlank(handlerName)) {
            log.warn("Blueprint[{}]缺少handlerName", location);
            return true;
        }
        if (StringUtils.isBlank(handlerDescription)) {
            log.warn("Blueprint[{}]缺少handlerDescription", location);
            return true;
        }
        return false;
    }

    public boolean isReqAbsent() {
        return reqBody == null || CollectionUtils.isEmpty(reqBody.getVariableDeclarators());
    }

    public boolean isRespAbsent() {
        return respBody == null || CollectionUtils.isEmpty(respBody.getVariableDeclarators());
    }

    public String getLocation() {
        return this.location;
    }

    public Path getSourceRoot() {
        return this.sourceRoot;
    }

    public ClassOrInterfaceDeclaration getController() {
        return this.controller;
    }

    public String getHandlerName() {
        return this.handlerName;
    }

    public String getHandlerDescription() {
        return this.handlerDescription;
    }

    public DtoMetaInfo getReqBody() {
        return this.reqBody;
    }

    public DtoMetaInfo getRespBody() {
        return this.respBody;
    }

    public ImmutableList<DtoMetaInfo> getDtos() {
        return this.dtos;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MetaInfo)) {
            return false;
        }
        final MetaInfo other = (MetaInfo) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$location = this.getLocation();
        final Object other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) {
            return false;
        }
        final Object this$sourceRoot = this.getSourceRoot();
        final Object other$sourceRoot = other.getSourceRoot();
        if (this$sourceRoot == null ? other$sourceRoot != null : !this$sourceRoot.equals(other$sourceRoot)) {
            return false;
        }
        final Object this$controller = this.getController();
        final Object other$controller = other.getController();
        if (this$controller == null ? other$controller != null : !this$controller.equals(other$controller)) {
            return false;
        }
        final Object this$handlerName = this.getHandlerName();
        final Object other$handlerName = other.getHandlerName();
        if (this$handlerName == null ? other$handlerName != null : !this$handlerName.equals(other$handlerName)) {
            return false;
        }
        final Object this$handlerDescription = this.getHandlerDescription();
        final Object other$handlerDescription = other.getHandlerDescription();
        if (this$handlerDescription == null ? other$handlerDescription != null
                : !this$handlerDescription.equals(other$handlerDescription)) {
            return false;
        }
        final Object this$reqBody = this.getReqBody();
        final Object other$reqBody = other.getReqBody();
        if (this$reqBody == null ? other$reqBody != null : !this$reqBody.equals(other$reqBody)) {
            return false;
        }
        final Object this$respBody = this.getRespBody();
        final Object other$respBody = other.getRespBody();
        if (this$respBody == null ? other$respBody != null : !this$respBody.equals(other$respBody)) {
            return false;
        }
        final Object this$dtos = this.getDtos();
        final Object other$dtos = other.getDtos();
        return this$dtos == null ? other$dtos == null : this$dtos.equals(other$dtos);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MetaInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $location = this.getLocation();
        result = result * PRIME + ($location == null ? 43 : $location.hashCode());
        final Object $sourceRoot = this.getSourceRoot();
        result = result * PRIME + ($sourceRoot == null ? 43 : $sourceRoot.hashCode());
        final Object $controller = this.getController();
        result = result * PRIME + ($controller == null ? 43 : $controller.hashCode());
        final Object $handlerName = this.getHandlerName();
        result = result * PRIME + ($handlerName == null ? 43 : $handlerName.hashCode());
        final Object $handlerDescription = this.getHandlerDescription();
        result = result * PRIME + ($handlerDescription == null ? 43 : $handlerDescription.hashCode());
        final Object $reqBody = this.getReqBody();
        result = result * PRIME + ($reqBody == null ? 43 : $reqBody.hashCode());
        final Object $respBody = this.getRespBody();
        result = result * PRIME + ($respBody == null ? 43 : $respBody.hashCode());
        final Object $dtos = this.getDtos();
        result = result * PRIME + ($dtos == null ? 43 : $dtos.hashCode());
        return result;
    }

    public String toString() {
        return "MetaInfo(location=" + this.getLocation() + ", sourceRoot=" + this.getSourceRoot() + ", controller="
                + this.getController() + ", handlerName=" + this.getHandlerName() + ", handlerDescription=" + this
                .getHandlerDescription() + ", reqBody=" + this.getReqBody() + ", respBody=" + this.getRespBody()
                + ", dtos=" + this.getDtos() + ")";
    }

    public static class MetaInfoBuilder {

        private String location;

        private Path sourceRoot;

        private ClassOrInterfaceDeclaration controller;

        private String handlerName;

        private String handlerDescription;

        private DtoMetaInfo reqBody;

        private DtoMetaInfo respBody;

        private ImmutableList<DtoMetaInfo> dtos;

        MetaInfoBuilder() {
        }

        public MetaInfoBuilder location(String location) {
            this.location = location;
            return this;
        }

        public MetaInfoBuilder sourceRoot(Path sourceRoot) {
            this.sourceRoot = sourceRoot;
            return this;
        }

        public MetaInfoBuilder controller(ClassOrInterfaceDeclaration controller) {
            this.controller = controller;
            return this;
        }

        public MetaInfoBuilder handlerName(String handlerName) {
            this.handlerName = handlerName;
            return this;
        }

        public MetaInfoBuilder handlerDescription(String handlerDescription) {
            this.handlerDescription = handlerDescription;
            return this;
        }

        public MetaInfoBuilder reqBody(DtoMetaInfo reqBody) {
            this.reqBody = reqBody;
            return this;
        }

        public MetaInfoBuilder respBody(DtoMetaInfo respBody) {
            this.respBody = respBody;
            return this;
        }

        public MetaInfoBuilder dtos(ImmutableList<DtoMetaInfo> dtos) {
            this.dtos = dtos;
            return this;
        }

        public MetaInfo build() {
            return new MetaInfo(location, sourceRoot, controller, handlerName, handlerDescription, reqBody, respBody,
                    dtos);
        }

        public String toString() {
            return "MetaInfo.MetaInfoBuilder(location=" + this.location + ", sourceRoot=" + this.sourceRoot
                    + ", controller=" + this.controller + ", handlerName=" + this.handlerName + ", handlerDescription="
                    + this.handlerDescription + ", reqBody=" + this.reqBody + ", respBody=" + this.respBody + ", dtos="
                    + this.dtos + ")";
        }

    }

}