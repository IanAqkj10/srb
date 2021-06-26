package com.ian.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.ian.srb.base.utils.JwtUtils;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.hfb.RequestHelper;
import com.ian.srb.core.pojo.UserBindVO;
import com.ian.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Api(tags = "会员账号绑定")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {

    @Autowired
    private UserBindService userBindService;


    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public Rs bind(@RequestBody UserBindVO userBindVO, HttpServletRequest httpRequestHandler) {

        String token = httpRequestHandler.getHeader("token");

        Long userId = JwtUtils.getUserId(token);

        String formStr = userBindService.commitBindUser(userBindVO, userId);

        return Rs.success().data("formStr", formStr);
    }

    //http://localhost/api/core/userBind/notify
    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {

        //将传进来的参数转化成map
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());

        log.info("用户账号绑定异步回调：" + JSON.toJSONString(paramMap));
        if(!RequestHelper.isSignEquals(paramMap)) {
            log.error("用户账号绑定异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }

        userBindService.notify(paramMap);

        return "success";
    }
}

