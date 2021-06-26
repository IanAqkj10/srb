package com.ian.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.ian.srb.core.enums.BorrowerStatusEnum;
import com.ian.srb.core.enums.IntegralEnum;
import com.ian.srb.core.mapper.*;
import com.ian.srb.core.pojo.*;
import com.ian.srb.core.service.BorrowerAttachService;
import com.ian.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ian.srb.core.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {





    @Resource
    private UserIntegralMapper userIntegralMapper;

    @Resource
    private BorrowerAttachService borrowerAttachService;
    @Resource
    private DictService dictService;

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());//认证中
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        //更新会员状态，更新为认证中
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }



    @Override
    public Integer getStatus(Long userId) {

        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);

        if(objects.size() == 0){
            //借款人尚未提交信息
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer)objects.get(0);
        return status;


    }

    @Override
    public IPage<Borrower> listByPage(Page<Borrower> pageSet, String keyword) {

        if (StringUtils.isEmpty(keyword)) {
            return baseMapper.selectPage(pageSet, null);
        }
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.like("user_id", keyword)
                .or().like("name", keyword)
                .or().like("id_card", keyword)
                .orderByDesc("id");
        ;

        return baseMapper.selectPage(pageSet, borrowerQueryWrapper);


    }

    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {

        //借款人认证状态,根据传入status更新borrower的Status字段
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        Borrower borrower = baseMapper.selectById(borrowerId);
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);

        //根据borrower查出userId，赋值给userIntegral的userId
        Long userId = borrower.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        int curIntegral = userInfo.getIntegral() + borrowerApprovalVO.getInfoIntegral();


        //userIntegral表是一对多关系，首先直接插入基本信息的分数
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);


        if (borrowerApprovalVO.getIsCarOk()) {
            curIntegral = curIntegral + IntegralEnum.BORROWER_CAR.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        if (borrowerApprovalVO.getIsHouseOk()) {
            curIntegral = curIntegral + IntegralEnum.BORROWER_HOUSE.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
        }
        if (borrowerApprovalVO.getIsIdCardOk()) {
            curIntegral = curIntegral + IntegralEnum.BORROWER_IDCARD.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

        userInfo.setIntegral(curIntegral);
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        userInfoMapper.updateById(userInfo);

    }

    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {
        Borrower borrower = baseMapper.selectById(id);

        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower,borrowerDetailVO);

        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry() ? "是" : "否");
        //性别
        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男" : "女");

//        Integer education = borrower.getEducation();
//        Integer income = borrower.getIncome();
//        Integer returnSource = borrower.getReturnSource();
//        Integer industry = borrower.getIndustry();
//        Integer contactsRelation = borrower.getContactsRelation();

        String education = dictService.getNameByDictCodeAndValue("education", borrower.getEducation());
        String industry = dictService.getNameByDictCodeAndValue("moneyUse", borrower.getIndustry());
        String income = dictService.getNameByDictCodeAndValue("income", borrower.getIncome());
        String returnSource = dictService.getNameByDictCodeAndValue("returnSource", borrower.getReturnSource());
        String contactsRelation = dictService.getNameByDictCodeAndValue("relation", borrower.getContactsRelation());

        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(contactsRelation);


        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        //获取附件VO列表
        List<BorrowerAttachVO> borrowerAttachVOList = borrowerAttachService.selectBorrowerAttachVOList(id);
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }
}
