package __NAMESPACE__.service;

import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.dto.resp.UploadFileResp;

/**
 * 文件上传/下载服务
 *
 * @author Deolin 2026-07-02
 */
public interface FileService {

    /**
     * 上传文件
     */
    UploadFileResp upload(MultipartFile file, String category);

    /**
     * 签发有时效的下载令牌
     */
    String temporarilyDownload(String fileKey);

    /**
     * 校验令牌并流式返回文件字节
     */
    void download(String token, HttpServletResponse response);

}
