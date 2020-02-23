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
     * 某些类下的field必须有注释
     */
    COMMENT_ABSENT_FILED_STATUTE("wftc", new CommentAbsentFiledStatute()),

    /**
     * 超过n行的方法
     */
    METHOD_LINE_NUMBER_STATUTE("5yya", new MethodLineNumberStatute()),

    /**
     * 多次new ResponseInfo对象的方法
     */
    MULTI_NEW_RESPONSE_INFO_STATUTE("hfvk", new MultiNewResponseInfoStatute()),

    /**
     * 声明了@Controller的和没有声明@RequestMapping的控制器
     */
    NORMAL_CONTROLLER_STATUTE("54u7", new NormalControllerStatute()),

    /**
     * 出未commit的被修改的文件
     */
    UNCOMMITTED_MODIFIED_FILE_STATUTE("cv5c", new UncommittedModifiedFileStatute()),

    ;

    private String no;

    private Statute statute;

}
