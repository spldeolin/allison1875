package com.spldeolin.allison1875.handlertransformer;

import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import com.google.common.collect.Lists;

/**
 * Allison1875[handler-transformer]的配置
 *
 * @author Deolin 2020-08-25
 */
public class HandlerTransformerConfig {

    private static final HandlerTransformerConfig instance = new HandlerTransformerConfig();

    /**
     * 控制层 @RequestBody类型所在包的包名
     */
    @NotEmpty
    private String reqDtoPackage;

    /**
     * 控制层 @ResponseBody业务数据部分类型所在包的包名
     */
    @NotEmpty
    private String respDtoPackage;

    /**
     * 业务层 Service接口所在包的包名
     */
    @NotEmpty
    private String servicePackage;

    /**
     * 业务 ServiceImpl类所在包的包名
     */
    @NotEmpty
    private String serviceImplPackage;

    /**
     * handler 方法上的需要生成的注解
     */
    @NotEmpty
    private Collection<@NotEmpty String> handlerAnnotations = Lists.newArrayList();

    /**
     * handler 方法签名的返回类型（使用%s占位符代替业务数据部分的泛型）
     */
    @NotEmpty
    private String result;

    /**
     * handler 当不需要返回业务数据时，方法签名的返回值
     */
    @NotEmpty
    private String resultVoid;

    /**
     * handler方法体的格式（使用%s占位符代替调用service的表达式）
     */
    @NotEmpty
    private String handlerBodyPattern;

    /**
     * handler不需要返回ResponseBody的场景，handler方法体的格式（使用%s占位符代替调用service的表达式）
     */
    @NotEmpty(message = "不能为空，如果不需要返回值则指定为;")
    private String handlerBodyPatternInNoResponseBodySituation;

    /**
     * controller需要确保存在的import
     */
    @NotEmpty
    private Collection<@NotEmpty String> controllerImports = Lists.newArrayList();

    /**
     * 为生成的代码指定作者
     */
    @NotEmpty
    private String author;

    private HandlerTransformerConfig() {
    }

    public static HandlerTransformerConfig getInstance() {
        return HandlerTransformerConfig.instance;
    }

    public String getReqDtoPackage() {
        return this.reqDtoPackage;
    }

    public String getRespDtoPackage() {
        return this.respDtoPackage;
    }

    public String getServicePackage() {
        return this.servicePackage;
    }

    public String getServiceImplPackage() {
        return this.serviceImplPackage;
    }

    public Collection<@NotEmpty String> getHandlerAnnotations() {
        return this.handlerAnnotations;
    }

    public String getResult() {
        return this.result;
    }

    public String getResultVoid() {
        return this.resultVoid;
    }

    public String getHandlerBodyPattern() {
        return this.handlerBodyPattern;
    }

    public String getHandlerBodyPatternInNoResponseBodySituation() {
        return this.handlerBodyPatternInNoResponseBodySituation;
    }

    public Collection<@NotEmpty String> getControllerImports() {
        return this.controllerImports;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setReqDtoPackage(@NotEmpty String reqDtoPackage) {
        this.reqDtoPackage = reqDtoPackage;
    }

    public void setRespDtoPackage(@NotEmpty String respDtoPackage) {
        this.respDtoPackage = respDtoPackage;
    }

    public void setServicePackage(@NotEmpty String servicePackage) {
        this.servicePackage = servicePackage;
    }

    public void setServiceImplPackage(@NotEmpty String serviceImplPackage) {
        this.serviceImplPackage = serviceImplPackage;
    }

    public void setHandlerAnnotations(@NotEmpty Collection<@NotEmpty String> handlerAnnotations) {
        this.handlerAnnotations = handlerAnnotations;
    }

    public void setResult(@NotEmpty String result) {
        this.result = result;
    }

    public void setResultVoid(@NotEmpty String resultVoid) {
        this.resultVoid = resultVoid;
    }

    public void setHandlerBodyPattern(@NotEmpty String handlerBodyPattern) {
        this.handlerBodyPattern = handlerBodyPattern;
    }

    public void setHandlerBodyPatternInNoResponseBodySituation(
            @NotEmpty(message = "不能为空，如果不需要返回值则指定为;") String handlerBodyPatternInNoResponseBodySituation) {
        this.handlerBodyPatternInNoResponseBodySituation = handlerBodyPatternInNoResponseBodySituation;
    }

    public void setControllerImports(@NotEmpty Collection<@NotEmpty String> controllerImports) {
        this.controllerImports = controllerImports;
    }

    public void setAuthor(@NotEmpty String author) {
        this.author = author;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HandlerTransformerConfig)) {
            return false;
        }
        final HandlerTransformerConfig other = (HandlerTransformerConfig) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$reqDtoPackage = this.getReqDtoPackage();
        final Object other$reqDtoPackage = other.getReqDtoPackage();
        if (this$reqDtoPackage == null ? other$reqDtoPackage != null
                : !this$reqDtoPackage.equals(other$reqDtoPackage)) {
            return false;
        }
        final Object this$respDtoPackage = this.getRespDtoPackage();
        final Object other$respDtoPackage = other.getRespDtoPackage();
        if (this$respDtoPackage == null ? other$respDtoPackage != null
                : !this$respDtoPackage.equals(other$respDtoPackage)) {
            return false;
        }
        final Object this$servicePackage = this.getServicePackage();
        final Object other$servicePackage = other.getServicePackage();
        if (this$servicePackage == null ? other$servicePackage != null
                : !this$servicePackage.equals(other$servicePackage)) {
            return false;
        }
        final Object this$serviceImplPackage = this.getServiceImplPackage();
        final Object other$serviceImplPackage = other.getServiceImplPackage();
        if (this$serviceImplPackage == null ? other$serviceImplPackage != null
                : !this$serviceImplPackage.equals(other$serviceImplPackage)) {
            return false;
        }
        final Object this$handlerAnnotations = this.getHandlerAnnotations();
        final Object other$handlerAnnotations = other.getHandlerAnnotations();
        if (this$handlerAnnotations == null ? other$handlerAnnotations != null
                : !this$handlerAnnotations.equals(other$handlerAnnotations)) {
            return false;
        }
        final Object this$result = this.getResult();
        final Object other$result = other.getResult();
        if (this$result == null ? other$result != null : !this$result.equals(other$result)) {
            return false;
        }
        final Object this$resultVoid = this.getResultVoid();
        final Object other$resultVoid = other.getResultVoid();
        if (this$resultVoid == null ? other$resultVoid != null : !this$resultVoid.equals(other$resultVoid)) {
            return false;
        }
        final Object this$handlerBodyPattern = this.getHandlerBodyPattern();
        final Object other$handlerBodyPattern = other.getHandlerBodyPattern();
        if (this$handlerBodyPattern == null ? other$handlerBodyPattern != null
                : !this$handlerBodyPattern.equals(other$handlerBodyPattern)) {
            return false;
        }
        final Object this$handlerBodyPatternInNoResponseBodySituation = this
                .getHandlerBodyPatternInNoResponseBodySituation();
        final Object other$handlerBodyPatternInNoResponseBodySituation = other
                .getHandlerBodyPatternInNoResponseBodySituation();
        if (this$handlerBodyPatternInNoResponseBodySituation == null ? other$handlerBodyPatternInNoResponseBodySituation
                != null : !this$handlerBodyPatternInNoResponseBodySituation
                .equals(other$handlerBodyPatternInNoResponseBodySituation)) {
            return false;
        }
        final Object this$controllerImports = this.getControllerImports();
        final Object other$controllerImports = other.getControllerImports();
        if (this$controllerImports == null ? other$controllerImports != null
                : !this$controllerImports.equals(other$controllerImports)) {
            return false;
        }
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        return this$author == null ? other$author == null : this$author.equals(other$author);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof HandlerTransformerConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $reqDtoPackage = this.getReqDtoPackage();
        result = result * PRIME + ($reqDtoPackage == null ? 43 : $reqDtoPackage.hashCode());
        final Object $respDtoPackage = this.getRespDtoPackage();
        result = result * PRIME + ($respDtoPackage == null ? 43 : $respDtoPackage.hashCode());
        final Object $servicePackage = this.getServicePackage();
        result = result * PRIME + ($servicePackage == null ? 43 : $servicePackage.hashCode());
        final Object $serviceImplPackage = this.getServiceImplPackage();
        result = result * PRIME + ($serviceImplPackage == null ? 43 : $serviceImplPackage.hashCode());
        final Object $handlerAnnotations = this.getHandlerAnnotations();
        result = result * PRIME + ($handlerAnnotations == null ? 43 : $handlerAnnotations.hashCode());
        final Object $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        final Object $resultVoid = this.getResultVoid();
        result = result * PRIME + ($resultVoid == null ? 43 : $resultVoid.hashCode());
        final Object $handlerBodyPattern = this.getHandlerBodyPattern();
        result = result * PRIME + ($handlerBodyPattern == null ? 43 : $handlerBodyPattern.hashCode());
        final Object $handlerBodyPatternInNoResponseBodySituation = this
                .getHandlerBodyPatternInNoResponseBodySituation();
        result = result * PRIME + ($handlerBodyPatternInNoResponseBodySituation == null ? 43
                : $handlerBodyPatternInNoResponseBodySituation.hashCode());
        final Object $controllerImports = this.getControllerImports();
        result = result * PRIME + ($controllerImports == null ? 43 : $controllerImports.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        return result;
    }

    public String toString() {
        return "HandlerTransformerConfig(reqDtoPackage=" + this.getReqDtoPackage() + ", respDtoPackage=" + this
                .getRespDtoPackage() + ", servicePackage=" + this.getServicePackage() + ", serviceImplPackage=" + this
                .getServiceImplPackage() + ", handlerAnnotations=" + this.getHandlerAnnotations() + ", result=" + this
                .getResult() + ", resultVoid=" + this.getResultVoid() + ", handlerBodyPattern=" + this
                .getHandlerBodyPattern() + ", handlerBodyPatternInNoResponseBodySituation=" + this
                .getHandlerBodyPatternInNoResponseBodySituation() + ", controllerImports=" + this.getControllerImports()
                + ", author=" + this.getAuthor() + ")";
    }

}