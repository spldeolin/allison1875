package com.spldeolin.allison1875.si.statute;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 所有规约注册在此处
 *
 * @author Deolin 2020-02-23
 */
@AllArgsConstructor
@Getter
public enum StatuteEnum {

    /**
     * 出未commit的被修改的文件
     */
    UNCOMMITTED_MODIFIED_FILE("cv5c", new UncommittedModifiedFileStatute()),

    /**
     * 必须有@author
     */
    AUTHOR_MUST_EXIST("cw84", new AuthorMustExistStatute()),

    /**
     * 某些类下的field必须有Javadoc
     */
    COMMENT_ABSENT_FILED("wftc", new CommentAbsentFiledStatute()),

    /**
     * 声明了@Controller的和没有声明@RequestMapping的控制器
     */
    NORMAL_CONTROLLER("54u7", new NormalControllerStatute()),

    /**
     * 只能使用POST方式的请求
     */
    ONLY_POST_MAPPING("sx8k", new OnlyPostMappingStatute()),

    /**
     * url必须与handler方法名一致
     */
    HANDLER_NAME_EQUALS_PATH("5g4p", new HandlerNameEqualsPathStatute()),

    /**
     * 多次new ResponseInfo对象的方法
     */
    MULTI_NEW_RESPONSE_INFO("hfvk", new MultiNewResponseInfoStatute()),

    /**
     * handler参数相关
     */
    HANDLER_PARAMETER("h33j", new HandlerParameterStatute()),

    /**
     * handler返回相关
     */
    HANDLER_RETURN("w74v", new HandlerReturnStatute()),

    /**
     * service/api参数相关
     */
    SERVICE_OR_API_PARAMETER("vpx7", new ServiceOrApiParameterStatute()),

    /**
     * DTO禁止使用全大写DTO结尾
     */
    FORBID_ALL_UPPER_DTO("k84s", new ForbidAllUpperDtoStatute()),

    /**
     * 禁止使用宽泛的单词
     */
    FORBID_BROAD_WORD("jfat", new ForbidBroadWordStatute()),

    /**
     * 禁止出现单词State
     */
    FORBID_WORD_STATE("hch5", new ForbidWordStateStatute()),

    /**
     * if或三目表达式中的条件部分，禁止3个及以上逻辑运算符
     */
    LIMIT_CONDITION_LOGICAL_OPERATOR("p5jv", new LimitConditionLogicalOperatorStatute()),

    /**
     * 超过n行的方法
     */
    METHOD_LINE_NUMBER("5yya", new MethodLineNumberStatute()),

    /**
     * 禁止数字魔法值
     */
    FORBID_MAGIC_NUMBER("uyjf", new ForbidMagicNumberStatute()),

    /**
     * 禁止e.printStackTrace
     */
    FORBID_PRINT_STACK_TRACE("aj3p", new ForbidPrintStackTraceStatus()),

    /**
     * ServiceImpl中的方法禁止调用需要Wrapper的BaseMapper方法
     */
    MYBATIS_WRAPPER_SELECT("uv4w", new MybatisWrapperSelectStatute()),

    /**
     * 登录态相关
     */
    SIGN_TOKEN("43jg", new SignTokenStatute()),

    /**
     * Entity类禁止作为任何类的Field
     */
    FORBID_FIELD_TYPE_ENTITY("ky8y", new ForbidFieldTypeEntityStatute()),

    ;

    private String no;

    private Statute statute;

}
