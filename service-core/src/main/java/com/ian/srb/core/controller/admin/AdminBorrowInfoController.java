package com.ian.srb.core.controller.admin;


import com.baomidou.mybatisplus.extension.api.R;
import com.ian.srb.base.utils.JwtUtils;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.mapper.BorrowInfoMapper;
import com.ian.srb.core.pojo.BorrowInfo;
import com.ian.srb.core.pojo.BorrowInfoApprovalVO;
import com.ian.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @GetMapping("/list")
    @ApiOperation("获取借款列表信息")
    public Rs list() {

        List<BorrowInfo> list = borrowInfoService.getList();


        //  return Rs.success().data("list",list);
        return Rs.success().data("list", list);
    }


    @GetMapping("/show/{id}")
    @ApiOperation("查看详细借款信息")
    public Rs show(@ApiParam(value = "借款信息ID", required = true)
                   @PathVariable Long id){
        Map<String,Object> borrowInfoDetail = borrowInfoService.detailList(id);
        return Rs.success().data("borrowInfoDetail",borrowInfoDetail);
    }


    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public Rs approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {

        borrowInfoService.approval(borrowInfoApprovalVO);
        return Rs.success().message("审批完成");
    }
}