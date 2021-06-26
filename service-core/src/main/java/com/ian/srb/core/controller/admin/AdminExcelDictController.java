package com.ian.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.ian.srb.common.exception.BusinessException;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.result.Rs;
import com.ian.srb.core.pojo.Dict;
import com.ian.srb.core.pojo.ExcelDict;
import com.ian.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author:IanJ
 * @date:2021/6/15 20:25
 */

@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/admin/core/dict")
//@CrossOrigin
public class AdminExcelDictController {

    @Resource
    DictService dictService;

    @ApiOperation("Excel批量导入数据字典")
    @PostMapping("/import")
    public Rs readExcel(@RequestParam("file")
                        @ApiParam(value = "Excel文件", required = true)
                                MultipartFile file) {
        // InputStream inputStream = null;
        try {
            InputStream inputStream = file.getInputStream();
            dictService.importData(inputStream);
            return Rs.success().message("导入成功");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {


        try {
            // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDict.class).sheet("数据字典").doWrite(dictService.listDictData());

        } catch (IOException e) {
            //EXPORT_DATA_ERROR(104, "数据导出失败"),
            throw new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    @ApiOperation("根据id匹配上级获取字典")
    @GetMapping("/dictlist/{parentid}")
    public Rs getlistBypd(@PathVariable
                          @ApiParam(value = "上级id", required = true)
                                  Long parentid) {
        List<Dict> dictList = dictService.getlistBypd(parentid);
        return Rs.success().data("list", dictList);
    }
}