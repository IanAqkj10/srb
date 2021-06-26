package com.ian.srb.sms.controller.api;

import com.ian.srb.common.exception.Assert;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.result.Rs;
import com.ian.srb.common.utils.RandomUtils;
import com.ian.srb.common.utils.RegexValidateUtils;
import com.ian.srb.sms.client.CoreUserInfoClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author:IanJ
 * @date:2021/6/19 0:36
 */


@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
//@CrossOrigin
@Slf4j
public class ApiSmsController {

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{mobile}")
    public Rs send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile) {


        //MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        //MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);

        boolean b = coreUserInfoClient.checkMobile(mobile);
        Assert.isTrue(b == false,ResponseEnum.MOBILE_EXIST_ERROR);

        String code = RandomUtils.getFourBitRandom();

        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);


        return Rs.success().message("短信发送成功");
    }

}
