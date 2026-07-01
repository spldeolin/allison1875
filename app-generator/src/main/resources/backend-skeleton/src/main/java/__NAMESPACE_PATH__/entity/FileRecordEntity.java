package __NAMESPACE__.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * 文件记录
 * <p>file_record
 * <p>
 * <p>Any modifications may be overwritten by future code generations.
 *
 * @author Deolin 2026-06-30
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FileRecordEntity {

    /**
     * 主键
     * <p>id
     * <p>不能为null
     */
    Long id;

    /**
     * 文件Key（uuid+扩展名），业务唯一键，业务表引用此值
     * <p>file_key
     * <p>长度：255
     * <p>不能为null
     */
    String fileKey;

    /**
     * 上传时的原始文件名
     * <p>origin_file_name
     * <p>长度：255
     * <p>不能为null
     */
    String originFileName;

    /**
     * MIME类型，下载/预览时回填Content-Type
     * <p>content_type
     * <p>长度：128
     * <p>不能为null
     */
    String contentType;

    /**
     * 文件大小（字节）
     * <p>file_size
     * <p>不能为null
     */
    Long fileSize;

    /**
     * 上传时的文件类别（image/document/general等）
     * <p>category
     * <p>长度：32
     * <p>不能为null
     */
    String category;

    /**
     * 存储桶名称，本地存储时为空
     * <p>bucket
     * <p>长度：64
     */
    String bucket;

    /**
     * 创建时间
     * <p>created_at
     * <p>不能为null
     */
    LocalDateTime createdAt;

    /**
     * 创建人
     * <p>created_by
     * <p>长度：32
     */
    String createdBy;
}
