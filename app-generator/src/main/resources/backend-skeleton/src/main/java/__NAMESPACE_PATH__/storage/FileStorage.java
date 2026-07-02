package __NAMESPACE__.storage;

/**
 * 文件存储抽象，上传/下载代码仅依赖此接口
 *
 * @author Deolin 2026-07-02
 */
public interface FileStorage {

    /**
     * 存储文件字节
     */
    void store(byte[] bytes, String fileKey);

    /**
     * 读取文件字节
     */
    byte[] load(String fileKey);

    /**
     * 当前存储的bucket标识，本地存储返回null
     */
    String getBucket();

}
