package com.ian.srb.core.service;

import com.ian.srb.core.pojo.BorrowerAttach;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.BorrowerAttachVO;

import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface BorrowerAttachService extends IService<BorrowerAttach> {

    List<BorrowerAttachVO> selectBorrowerAttachVOList(Long borrowerId);
}
