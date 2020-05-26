package com.itzjx.upload.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itzjx.common.enums.ExceptionEnum;
import com.itzjx.common.exception.XmException;
import com.itzjx.upload.config.UploadProperties;
import com.itzjx.upload.controller.UploadController;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhaojiexiong
 * @create 2020/5/24
 * @since 1.0.0
 */
@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    // 支持的文件类型
    private static final List<String> SUFFIXES = Arrays.asList("image/png", "image/jpeg","image/jpg");

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

    public String uploadImage(MultipartFile file) {
        try {
            // 1、图片信息校验
            // 1)校验文件类型
            String type = file.getContentType();
            if (!prop.getAllowTypes().contains(type)) {
                throw new XmException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            // 2)校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new XmException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 2、保存图片（保存到FDFS文件服务器上去）
            // 2.1、获取文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");

            // 2.2、上传
            StorePath storePath = this.storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);

            // 2.3、返回完整路径
            return prop.getBaseUrl() + storePath.getFullPath();

            /*
            //下面是将文件保存到本地的方法
            // 2.1、生成保存目录
            File dir = new File("/opt/developer/IDEA/workspace/xiongmao/fileupload/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2.2、保存图片
            //file.getOriginalFilename() 为获取图片名称，可以使用IP + 时间戳自动生成图片名，以保证上传图片不会重复覆盖
            file.transferTo(new File(dir, file.getOriginalFilename()));

            // 2.3、拼接图片地址
            String url = "http://image.itzjx.com/" + file.getOriginalFilename();
            return url;

             */
        } catch (IOException e) {
            log.error("[文件上传] 上传文件失败",e);
            throw new XmException(ExceptionEnum.UPLOAD_ERROR);
        }


    }
}
