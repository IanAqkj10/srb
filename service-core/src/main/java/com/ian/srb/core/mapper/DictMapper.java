package com.ian.srb.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ian.srb.core.pojo.Dict;
import com.ian.srb.core.pojo.ExcelDict;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface DictMapper extends BaseMapper<Dict> {

   void insertBatch(List<ExcelDict> list);

}
