package com.ian.srb.core.controller.admin;


import com.ian.srb.common.exception.BusinessException;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.IntegralGrade;
import com.ian.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Slf4j
@Api(tags = "积分管理")
@RestController
@RequestMapping("/admin/core/integralGrade")
//@CrossOrigin
public class AdminIntegralGradeController {

    @Autowired
    private IntegralGradeService integralGradeService;

    @ApiOperation(value = "获取积分列表")
    @GetMapping("/list")
    public Rs list() {

        List<IntegralGrade> list = integralGradeService.list();
           log.info("qaq");
          //  int  a= 1/0;

        return Rs.success().data("list", list);

    }

    @ApiOperation(value = "根据id删除数据", notes = "进行逻辑删除")
    @DeleteMapping("/delete/{id}")
    public Rs delete(@PathVariable Long id) {

        boolean b = integralGradeService.removeById(id);
        if (b) {
            return Rs.success().message("删除成功");
        } else {
            return Rs.error().message("删除失败");
        }

    }

    @PostMapping("/save")
    public Rs save(@RequestBody IntegralGrade integralGrade) {

        boolean save = integralGradeService.save(integralGrade);

        if(integralGrade.getBorrowAmount()==null){
            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        }

      //  Assert.notNull(integralGrade.getBorrowAmount(), ResponseEnum.BORROW_AMOUNT_NULL_ERROR);

        if (save) {
            return Rs.success().message("新增成功咯");
        } else {
            return Rs.error().message("新增失败");
        }
    }


    @GetMapping("/getbyid/{id}")
    public Rs getbyid(@PathVariable("id") Integer id) {

        IntegralGrade integralGrade = integralGradeService.getById(id);

        if (integralGrade != null) {
            return Rs.success().message("查找成功").data("record", integralGrade);
        } else {
            return Rs.error().message("查找失败");
        }
    }

    @PutMapping("/update")
    public Rs update(@RequestBody IntegralGrade integralGrade) {

        boolean b = integralGradeService.updateById(integralGrade);

        if (b) {
            return Rs.success().message("更新成功咯");
        } else {
            return Rs.error().message("更新失败");
        }
    }

}

