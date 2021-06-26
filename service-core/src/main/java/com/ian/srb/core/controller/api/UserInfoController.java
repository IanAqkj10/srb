package com.ian.srb.core.controller.api;


import com.baomidou.mybatisplus.extension.api.R;
import com.ian.srb.base.utils.JwtUtils;
import com.ian.srb.common.exception.Assert;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.LoginVO;
import com.ian.srb.core.pojo.RegisterVO;
import com.ian.srb.core.pojo.UserIndexVO;
import com.ian.srb.core.pojo.UserInfoVO;
import com.ian.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */


@Api(tags = "会员接口")
@RestController
@RequestMapping("/api/core/userInfo")
@Slf4j
//@CrossOrigin
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;
    @Resource
    private RedisTemplate redisTemplate;

//    @ApiOperation("获取验证码")
//    @GetMapping("/send/{mobile}")
//    public Rs send(
//            @ApiParam(value = "手机号", required = true)
//            @PathVariable String mobile) {
//
//
//        //MOBILE_NULL_ERROR(-202, "手机号不能为空"),
//        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
//        //MOBILE_ERROR(-203, "手机号不正确"),
//        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
//
//        String code = RandomUtils.getFourBitRandom();
//
//        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);
//
//
//        return Rs.success().message("短信发送成功");
//    }


    @PostMapping("/register")
    @ApiOperation("注册功能")
    public Rs register(@RequestBody RegisterVO registerVO) {

        Assert.notNull(registerVO.getMobile(), ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notNull(registerVO.getCode(), ResponseEnum.CODE_NULL_ERROR);
        Assert.notNull(registerVO.getPassword(), ResponseEnum.PASSWORD_NULL_ERROR);

        String code = registerVO.getCode();

        String ocode = (String) redisTemplate.opsForValue().get("srb:sms:code:" + registerVO.getMobile());

        Assert.equals(code, ocode, ResponseEnum.CODE_ERROR);

        userInfoService.register(registerVO);

        return Rs.success().message("注册成功");
    }


    @PostMapping("/login")
    @ApiOperation("登录功能")
    public Rs login(@RequestBody LoginVO loginVO, HttpServletRequest httpServletRequest){

        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);


        String remoteAddr = httpServletRequest.getRemoteAddr();

        UserInfoVO userInfoVO = userInfoService.login(loginVO, remoteAddr);

        return Rs.success().data("userInfo",userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public Rs checkToken(HttpServletRequest request) {

        String token = request.getHeader("token");
        boolean result = JwtUtils.checkToken(token);

        if(result){
            return Rs.success();
        }else{
            //LOGIN_AUTH_ERROR(-211, "未登录"),
            return Rs.myrs(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@PathVariable String mobile){

        return userInfoService.checkMobile(mobile);

    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public Rs getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return Rs.success().data("userIndexVO", userIndexVO);
    }


}

