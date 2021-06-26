package com.ian.srb.oss.controller.api;

import com.ian.srb.common.exception.BusinessException;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.result.Rs;
import com.ian.srb.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author:IanJ
 * @date:2021/6/17 17:28
 */

@Api(tags = "阿里云文件管理")
//@CrossOrigin 跨域
@RestController
@RequestMapping("/api/oss/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Rs upload(@ApiParam(value = "文件", required = true)
                     @RequestParam("file") MultipartFile file,
                     @ApiParam(value = "模块", required = true)
                     @RequestParam("module") String model) {

        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String url = fileService.upload(inputStream, model, originalFilename);
            return Rs.success().message("文件上次成功").data("url", url);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("文件删除")
    @DeleteMapping("/remove")
    public Rs remove(@ApiParam(value = "要删除的文件路径", required = true)
                     @RequestParam("url") String url) {

        fileService.remove(url);

        return Rs.success().message("删除文件成功");


    }

}
