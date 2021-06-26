package com.ian.srb.oss.service;

import java.io.InputStream;

/**
 * @author:IanJ
 * @date:2021/6/17 17:09
 */
public interface FileService {

    public String upload(InputStream inputStream,String model,String filename);

    void remove(String url);
}
