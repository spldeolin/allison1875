package __NAMESPACE__.dto.resp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-07-02
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadFileResp {

    /**
     * 文件Key（uuid+扩展名），业务表引用此值
     */
    String fileKey;

    /**
     * 上传时的原始文件名
     */
    String originFileName;

}
