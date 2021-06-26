package com.ian.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ian.srb.core.pojo.BorrowerAttach;
import com.ian.srb.core.mapper.BorrowerAttachMapper;
import com.ian.srb.core.pojo.BorrowerAttachVO;
import com.ian.srb.core.service.BorrowerAttachService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {


    @Override
    public List<BorrowerAttachVO> selectBorrowerAttachVOList(Long borrowerId) {

        QueryWrapper<BorrowerAttach> borrowerAttachQueryWrapper = new QueryWrapper<>();
        borrowerAttachQueryWrapper.eq("borrower_id",borrowerId);
        List<BorrowerAttach> borrowerAttaches = baseMapper.selectList(borrowerAttachQueryWrapper);

        List<BorrowerAttachVO> borrowerAttachVOList = new ArrayList<>();
            borrowerAttaches.forEach(borrowerAttach -> {
                BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
                borrowerAttachVO.setImageType(borrowerAttach.getImageType());
               borrowerAttachVO.setImageUrl(borrowerAttach.getImageUrl());

               borrowerAttachVOList.add(borrowerAttachVO);
            });
        return borrowerAttachVOList;
    }
}
