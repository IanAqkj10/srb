package com.ian.srb.core.pojo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author:IanJ
 * @date:2021/6/23 22:39
 */
@Data
@ApiModel(description = "投标信息")
public class InvestVO {

    private Long lendId;

    //投标金额
    private String investAmount;

    //用户id
    private Long investUserId;

    //用户姓名
    private String investName;
}