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
    UNCOMMITTED_MODIFIED_FILE_STATUTE("cv5c", new UncommittedModifiedFileStatute()),

    /**
     * 必须有@author
     */
    AUTHOR_MUST_EXIST_STATUTE("cw84", new AuthorMustExistStatute()),

    /**
     * 某些类下的field必须有Javadoc
     */
    COMMENT_ABSENT_FILED_STATUTE("wftc", new CommentAbsentFiledStatute()),

    /**
     * 声明了@Controller的和没有声明@RequestMapping的控制器
     */
    NORMAL_CONTROLLER_STATUTE("54u7", new NormalControllerStatute()),

    /**
     * 只能使用POST方式的请求
     */
    ONLY_POST_MAPPING_STATUTE("sx8k", new OnlyPostMappingStatute()),

    /**
     * url必须与handler方法名一致
     */
    HANDLER_NAME_EQUALS_PATH_STATUTE("5g4p", new HandlerNameEqualsPathStatute()),

    /**
     * 超过n行的方法
     */
    METHOD_LINE_NUMBER_STATUTE("5yya", new MethodLineNumberStatute()),

    /**
     * 多次new ResponseInfo对象的方法
     */
    MULTI_NEW_RESPONSE_INFO_STATUTE("hfvk", new MultiNewResponseInfoStatute()),

    /**
     * DTO禁止使用全大写DTO结尾
     */
    FORBID_ALL_UPPER_DTO_STATUTE("j3gt", new ForbidAllUpperDtoStatute()),

    /**
     * 禁止使用宽泛的单词
     */
    FORBID_BROAD_WORD_STATUTE("jfat", new ForbidBroadWordStatute()),

    /**
     * 禁止出现单词State
     */
    FORBID_WORD_STATE_STATUTE("hch5", new ForbidWordStateStatute()),

    /**
     * if或三目表达式中的条件部分，禁止3个及以上逻辑运算符
     */
    LimitConditionLogicalOperatorStatute("p5jv", new LimitConditionLogicalOperatorStatute());;

    private String no;

    private Statute statute;

}
