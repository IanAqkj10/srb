package com.ian.srb.core.service;

import com.ian.srb.core.pojo.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    BigDecimal getAmount(Long userId);

    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);

    Integer getStatus(Long userId);


    List<BorrowInfo> getList();

    Map<String, Object> detailList(Long id);

    void approval(BorrowInfoApprovalVO borrowInfoApprovalVO);
}
