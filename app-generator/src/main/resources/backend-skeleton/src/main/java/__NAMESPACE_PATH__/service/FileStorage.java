package __NAMESPACE__.service;

import java.io.InputStream;

/**
 * 文件存储门面。上传/下载代码仅依赖此接口，与具体存储后端（S3/本地）解耦。
 *
 * <p>方法采用流式语义，避免大文件全量驻留内存。
 *
 * @author Deolin 2026-07-02
 */
public interface FileStorage {

    /**
     * 以流式方式存储文件。
     *
     * @param in      文件输入流，由调用方负责关闭
     * @param size    文件字节数
     * @param fileKey 文件Key
     */
    void store(InputStream in, long size, String fileKey);

    /**
     * 以流式方式读取文件。
     *
     * @param fileKey 文件Key
     * @return 文件输入流，调用方使用后必须自行关闭
     */
    InputStream load(String fileKey);

    /**
     * 当前存储的bucket标识，本地存储返回null
     */
    String getBucket();

}
