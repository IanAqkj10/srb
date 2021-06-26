package com.ian.srb.core.mapper;

import com.ian.srb.core.pojo.BorrowInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 借款信息表 Mapper 接口
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface BorrowInfoMapper extends BaseMapper<BorrowInfo> {

        List<BorrowInfo> selectList();
}
