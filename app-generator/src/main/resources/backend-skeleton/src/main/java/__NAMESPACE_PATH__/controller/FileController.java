package __NAMESPACE__.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.resp.TemporarilyDownloadFileResp;
import __NAMESPACE__.dto.resp.UploadFileResp;
import __NAMESPACE__.service.FileService;

/**
 * 通用文件上传/下载接口
 *
 * @author Deolin 2026-07-02
 */
@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("uploadFile")
    public RequestResult<UploadFileResp> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {
        return RequestResult.success(fileService.upload(file, category));
    }

    @PostMapping("temporarilyDownloadFile")
    public RequestResult<TemporarilyDownloadFileResp> temporarilyDownloadFile(
            @RequestParam("fileKey") String fileKey) {
        String token = fileService.temporarilyDownload(fileKey);
        return RequestResult.success(new TemporarilyDownloadFileResp().setToken(token));
    }

    @GetMapping("downloadFile")
    public void downloadFile(@RequestParam("token") String token, HttpServletResponse response) {
        fileService.download(token, response);
    }

}
