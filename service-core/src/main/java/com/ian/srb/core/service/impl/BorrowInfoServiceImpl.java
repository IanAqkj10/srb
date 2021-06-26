package com.ian.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ian.srb.common.exception.Assert;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.core.enums.BorrowAuthEnum;
import com.ian.srb.core.enums.BorrowInfoStatusEnum;
import com.ian.srb.core.enums.UserBindEnum;
import com.ian.srb.core.mapper.BorrowerMapper;
import com.ian.srb.core.mapper.IntegralGradeMapper;
import com.ian.srb.core.mapper.UserInfoMapper;
import com.ian.srb.core.pojo.*;
import com.ian.srb.core.mapper.BorrowInfoMapper;
import com.ian.srb.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ian.srb.core.service.BorrowerService;
import com.ian.srb.core.service.DictService;
import com.ian.srb.core.service.LendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private LendService lendService;

    @Resource
    private BorrowerService borrowerService;
    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private DictService dictService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        //首先判断是否绑定和是否通过申请
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //判断bindStatus为1
        Assert.isTrue(userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);
        //判断borrow_auth_status是否为1
        Assert.isTrue(userInfo.getBorrowAuthStatus().intValue() == BorrowAuthEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);

        BigDecimal amount = this.getAmount(userId);

        Assert.isTrue(borrowInfo.getAmount().doubleValue() <= amount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);

        borrowInfo.setUserId(userId);
        //百分比转成小数
        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);



    }

    @Override
    public Integer getStatus(Long userId) {

        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.eq("user_id",userId)
        .select("status");
        List<Object> objects = baseMapper.selectObjs(borrowInfoQueryWrapper);

        if(objects.size() == 0){

            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }

        Integer status = (Integer) objects.get(0);

        return status;


    }

    @Override
    public List<BorrowInfo> getList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectList();
        borrowInfoList.forEach(borrowInfo -> {
            String returnMethod = dictService.getNameByDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            borrowInfo.getParam().put("returnMethod",returnMethod);
            borrowInfo.getParam().put("moneyUse",moneyUse);
            borrowInfo.getParam().put("status",status);
        });
        return borrowInfoList;
    }

    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);

        if(borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()){
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);

        }


    }

    @Override
    public Map<String, Object> detailList(Long id) {

        //查询借款对象
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        //组装数据
        String returnMethod = dictService.getNameByDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        //根据user_id获取借款人对象
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<Borrower>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        //组装借款人对象
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        //组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;

    }


    @Override
    public BigDecimal getAmount(Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);

        Integer integral = userInfo.getIntegral();

        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper.le("integral_start", integral)
                .ge("integral_end", integral);

        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }
        return integralGrade.getBorrowAmount();


    }
}
