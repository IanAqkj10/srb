package com.ian.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.ian.srb.oss.service.FileService;
import com.ian.srb.oss.utils.OssPropertites;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author:IanJ
 * @date:2021/6/17 17:11
 */
@Service
public class FileServiceImpl implements FileService {

    @Override
    public String upload(InputStream inputStream, String model, String filename) {

        OSS ossclient = new OSSClientBuilder().build(
                OssPropertites.ENDPOINT,
                OssPropertites.KEY_ID,
                OssPropertites.KEY_SECRET
        );
        if (!ossclient.doesBucketExist(OssPropertites.BUCKET_NAME)) {
            ossclient.createBucket(OssPropertites.BUCKET_NAME);
            ossclient.setBucketAcl(OssPropertites.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }
     //   String folder = new DateTime().toString("yyyy/MM/dd");

        String timeStr1= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String folder = timeStr1.replaceAll("-", "/");

        String ffilename = UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));
        //model/yyyy/mm/dd/suijishu.jpg
        String uurl = model + "/" + folder + "/" + ffilename;
        ossclient.putObject(OssPropertites.BUCKET_NAME, uurl, inputStream);
        // 关闭OSSClient。
        ossclient.shutdown();

        return "https://" + OssPropertites.BUCKET_NAME + "." + OssPropertites.ENDPOINT + "/" + uurl;

    }

    @Override
    public void remove(String url) {

        OSS ossclient = new OSSClientBuilder().build(
                OssPropertites.ENDPOINT,
                OssPropertites.KEY_ID,
                OssPropertites.KEY_SECRET
        );

     //https://iansave.oss-cn-guangzhou.aliyuncs.com
        // /qqq/2021/06/17/e1c72dd9-b497-48d2-8434-88b958a01b2b.jpg

        String host = "https://" + OssPropertites.BUCKET_NAME + "." + OssPropertites.ENDPOINT + "/";

        String objectName = url.substring(host.length());

        ossclient.deleteObject(OssPropertites.BUCKET_NAME,objectName);

        ossclient.shutdown();

    }
}
