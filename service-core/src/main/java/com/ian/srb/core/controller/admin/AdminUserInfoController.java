package com.ian.srb.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.UserInfo;
import com.ian.srb.core.pojo.UserInfoQuery;
import com.ian.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */


@Api(tags = "会员管理")
@RestController
@RequestMapping("/admin/core/userInfo")
@Slf4j
//@CrossOrigin
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Rs pagelist(@ApiParam(value = "当前页码", required = true)
                       @PathVariable Integer page,
                       @ApiParam(value = "每页记录数", required = true)
                       @PathVariable Integer limit,
                       @ApiParam(value = "查询对象", required = false)
                               UserInfoQuery userInfoQuery
    ) {

        Page<UserInfo> pageParam = new Page<>(page, limit);

        IPage<UserInfo> pageModel = userInfoService.listpage(pageParam, userInfoQuery);

        return Rs.success().data("pageModel", pageModel);
    }

    @ApiOperation("操作用户状态")
    @PutMapping("/lock/{id}/{status}")
    public Rs lock(
            @ApiParam(value = "传入id", required = true)
            @PathVariable(value = "id") Long id,
            @ApiParam(value = "锁定状态（0：锁定 1：解锁）", required = true)
            @PathVariable(value = "status") Integer status
    ) {
        userInfoService.lock(id, status);

        return Rs.success().message(status == 1 ? "解锁成功" : "锁定成功");
    }




}

