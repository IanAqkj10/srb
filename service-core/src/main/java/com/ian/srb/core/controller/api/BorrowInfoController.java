package com.ian.srb.core.controller.api;


import com.ian.srb.base.utils.JwtUtils;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.BorrowInfo;
import com.ian.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * <p>
 * 借款信息表 前端控制器
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Api(tags = "借款信息")
@RestController
@RequestMapping("/api/core/borrowInfo")
@Slf4j
public class BorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("获取借款额度")
    @GetMapping("/auth/getBorrowAmount")
    public Rs getAmount(HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        BigDecimal amount = borrowInfoService.getAmount(userId);

        return Rs.success().data("amount", amount);
    }

    @ApiOperation("提交借款具体信息")
    @PostMapping("/auth/save")
    public Rs saveBorrowInfo(@RequestBody BorrowInfo borrowInfo,HttpServletRequest request){


        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        borrowInfoService.saveBorrowInfo(borrowInfo,userId);


        return Rs.success().message("提交成功");
    }


    @ApiOperation("获取借款申请审批状态")
    @GetMapping("/auth/getBorrowInfoStatus")
    public Rs getStatus(HttpServletRequest request){

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        Integer status = borrowInfoService.getStatus(userId);


        return Rs.success().data("borrowInfoStatus",status);
    }

}