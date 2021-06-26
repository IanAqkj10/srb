package com.ian.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.Dict;
import com.ian.srb.core.pojo.ExcelDict;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface DictService extends IService<Dict> {

    void importData(InputStream inputStream);

    List<ExcelDict> listDictData();

    List<Dict> getlistBypd(Long parentid);

    List<Dict> findByDictCode(String dictCode);

    String getNameByDictCodeAndValue(String dictCode, Integer value);
}
