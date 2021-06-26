package com.ian.srb.core.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.*;
import com.ian.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:IanJ
 * @date:2021/6/21 15:20
 */

@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人列表")
    @GetMapping("/list/{page}/{limit}")
    public Rs listByPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(value = "查询关键字", required = false)
            @RequestParam String keyword) {

        Page<Borrower> pageSet = new Page<>(page, limit);

        IPage<Borrower> pageModel = borrowerService.listByPage(pageSet, keyword);

        return Rs.success().data("pageModel", pageModel);
    }


    @ApiOperation("获取借款人详细信息")
    @GetMapping("/show/{id}")
    public Rs show(
            @ApiParam(value = "借款人的id", required = true)
            @PathVariable Long id) {

        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);


        return Rs.success().data("borrowerDetailVO", borrowerDetailVO);

    }


    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public Rs approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO) {

        borrowerService.approval(borrowerApprovalVO);


        return Rs.success().message("审批成功");
    }


}
