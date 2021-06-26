package com.ian.srb.core.controller.admin;

import com.baomidou.mybatisplus.extension.api.R;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.Lend;
import com.ian.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author:IanJ
 * @date:2021/6/22 23:49
 */
@Api(tags = "标的管理")
@RestController
@RequestMapping("/admin/core/lend")
@Slf4j
public class AdminLendController {

    @Resource
    private LendService lendService;

    @ApiOperation("标的列表")
    @GetMapping("/list")
    public Rs list() {
        List<Lend> lendList = lendService.selectList();
        return Rs.success().data("list", lendList);
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public Rs show(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        Map<String, Object> result = lendService.getLendDetail(id);
        return Rs.success().data("lendDetail", result);
    }

    @ApiOperation("放款")
    @GetMapping("/makeLoan/{id}")
    public Rs makeLoan(
            @ApiParam(value = "标的id", required = true)
            @PathVariable("id") Long id) {
        lendService.makeLoan(id);
        return Rs.success().message("放款成功");
    }








}









