package com.ian.srb.oss.utils;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author:IanJ
 * @date:2021/6/17 17:07
 */

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssPropertites implements InitializingBean {

    private String endpoint;
    private String keyId;
    private String keySecret;
    private String bucketName;

    public static String ENDPOINT;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String BUCKET_NAME;


    @Override
    public void afterPropertiesSet() throws Exception {
        ENDPOINT = endpoint;
        KEY_ID = keyId;
        KEY_SECRET = keySecret;
        BUCKET_NAME = bucketName;
    }
}
