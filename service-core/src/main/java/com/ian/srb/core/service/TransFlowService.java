package com.ian.srb.core.service;

import com.ian.srb.core.pojo.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.TransFlowBO;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface TransFlowService extends IService<TransFlow> {


    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);

    List<TransFlow> selectByUserId(Long userId);
}
