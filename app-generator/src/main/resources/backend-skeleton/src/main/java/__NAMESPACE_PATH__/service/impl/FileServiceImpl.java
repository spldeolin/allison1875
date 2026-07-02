package __NAMESPACE__.service.impl;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.common.CurrentUser;
import __NAMESPACE__.common.ErrorCode;
import __NAMESPACE__.dto.resp.UploadFileResp;
import __NAMESPACE__.entity.FileRecordEntity;
import __NAMESPACE__.enums.FileCategoryEnum;
import __NAMESPACE__.mapper.FileRecordMapper;
import __NAMESPACE__.property.FileProperties;
import __NAMESPACE__.service.FileService;
import __NAMESPACE__.storage.FileStorage;
import __NAMESPACE__.util.DownloadTokenUtils;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件上传/下载服务实现
 *
 * @author Deolin 2026-07-02
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStorage fileStorage;

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Resource
    private FileProperties fileProperties;

    @Override
    public UploadFileResp upload(MultipartFile file, String category) {
        FileCategoryEnum categoryEnum = FileCategoryEnum.of(category);
        if (categoryEnum == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件类别非法");
        }
        String originFileName = file.getOriginalFilename();
        String ext = extractExtension(originFileName);
        if (!categoryEnum.isExtensionAllowed(ext)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件扩展名不被允许");
        }
        String fileKey = UuidUtils.generateShort() + (ext != null ? "." + ext : "");
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        try {
            fileStorage.store(file.getBytes(), fileKey);
        } catch (Exception e) {
            log.error("文件上传失败 fileKey={}", fileKey, e);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件上传失败");
        }
        FileRecordEntity entity = new FileRecordEntity()
                .setFileKey(fileKey)
                .setOriginFileName(originFileName)
                .setContentType(contentType)
                .setFileSize(file.getSize())
                .setCategory(category)
                .setBucket(fileStorage.getBucket())
                .setCreatedAt(LocalDateTime.now())
                .setCreatedBy(CurrentUser.getUsernameOrDefault("system"));
        fileRecordMapper.insert(entity);
        log.info("文件上传成功 fileKey={} originFileName={} category={}", fileKey, originFileName, category);
        return new UploadFileResp().setFileKey(fileKey).setOriginFileName(originFileName);
    }

    @Override
    public String temporarilyDownload(String fileKey) {
        FileRecordEntity entity = fileRecordMapper.queryByFileKey(fileKey);
        if (entity == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件不存在");
        }
        return DownloadTokenUtils.sign(fileKey, fileProperties.getDownloadTokenSecret(),
                fileProperties.getDownloadTokenTtlSeconds());
    }

    @Override
    public void download(String token, HttpServletResponse response) {
        String fileKey = DownloadTokenUtils.verify(token, fileProperties.getDownloadTokenSecret());
        FileRecordEntity entity = fileRecordMapper.queryByFileKey(fileKey);
        if (entity == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件不存在");
        }
        byte[] bytes;
        try {
            bytes = fileStorage.load(fileKey);
        } catch (Exception e) {
            log.error("文件读取失败 fileKey={}", fileKey, e);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件下载失败");
        }
        response.setContentType(entity.getContentType());
        try {
            String filename = URLEncoder.encode(entity.getOriginFileName(), "UTF-8").replace("+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + filename);
            response.setContentLength(bytes.length);
            OutputStream out = response.getOutputStream();
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            log.error("文件写出失败 fileKey={}", fileKey, e);
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件下载失败");
        }
    }

    private static String extractExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

}
