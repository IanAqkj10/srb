package com.ian.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ian.srb.common.exception.BusinessException;
import com.ian.srb.core.enums.LendStatusEnum;
import com.ian.srb.core.enums.ReturnMethodEnum;
import com.ian.srb.core.enums.TransTypeEnum;
import com.ian.srb.core.hfb.HfbConst;
import com.ian.srb.core.hfb.RequestHelper;
import com.ian.srb.core.mapper.BorrowerMapper;
import com.ian.srb.core.mapper.UserAccountMapper;
import com.ian.srb.core.mapper.UserInfoMapper;
import com.ian.srb.core.pojo.*;
import com.ian.srb.core.mapper.LendMapper;
import com.ian.srb.core.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ian.srb.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {


    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;


    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtils.getLendNo());//生成编号
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));//从审批对象中获取
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));//从审批对象中获取
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);
        lend.setPublishDate(LocalDateTime.now());

        //起息日期
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
        lend.setLendStartDate(lendStartDate);
        //结束日期
        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());//描述

        //平台预期收益
        //        月年化 = 年化 / 12
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //        平台收益 = 标的金额 * 月年化 * 期数
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        lend.setExpectAmount(expectAmount);

        //实际收益
        lend.setRealAmount(new BigDecimal(0));
        //状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        //审核时间
        lend.setCheckTime(LocalDateTime.now());
        //审核人
        lend.setCheckAdminId(1L);

        baseMapper.insert(lend);
    }

    @Override
    public List<Lend> selectList() {

        List<Lend> lendList = baseMapper.selectList(null);

        //组装lendList，把还款方式放在map里面，前端获取即可
        lendList.forEach(lend -> {
            String returnMethod = dictService.getNameByDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        });
        return lendList;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {
        HashMap<String, Object> result = new HashMap<>();
        Lend lend = baseMapper.selectById(id);

        String returnMethod = dictService.getNameByDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);


        Long userId = lend.getUserId();
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", userId);
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        result.put("borrower", borrowerDetailVO);
        result.put("lend", lend);
        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {

        BigDecimal interestCount;

        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }
        return interestCount;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long lendId) {
        log.info("进入接口");
        //获取标的信息
        Lend lend = baseMapper.selectById(lendId);

        //放款接口调用
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode", lend.getLendNo());//标的编号
        String agentBillNo = LendNoUtils.getLoanNo();//放款编号
        paramMap.put("agentBillNo", agentBillNo);

        //平台收益，放款扣除，借款人借款实际金额=借款金额-平台收益
        //月年化
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台实际收益 = 已投金额 * 月年化 * 标的期数
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));


        paramMap.put("mchFee", realAmount); //商户手续费(平台实际收益)
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        log.info("放款参数：" + JSONObject.toJSONString(paramMap));
        //发送同步远程调用
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + result.toJSONString());

        //放款失败
        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }

        //更新标的信息
        lend.setRealAmount(realAmount);
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(lend);

        //获取借款人信息
        Long userId = lend.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();

        //给借款账号转入金额
        BigDecimal total = new BigDecimal(result.getString("voteAmt"));
        userAccountMapper.updateAccount(bindCode, total, new BigDecimal(0));

        //新增借款人交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                total,
                TransTypeEnum.BORROW_BACK,
                "借款放款到账，编号：" + lend.getLendNo());//项目编号
        transFlowService.saveTransFlow(transFlowBO);

        //获取投资列表信息
        List<LendItem> lendItemList = lendItemService.selectByLendId(lendId, 1);
        lendItemList.stream().forEach(item -> {

            //获取投资人信息
            Long investUserId = item.getInvestUserId();
            UserInfo investUserInfo = userInfoMapper.selectById(investUserId);
            String investBindCode = investUserInfo.getBindCode();

            //投资人账号冻结金额转出
            BigDecimal investAmount = item.getInvestAmount(); //投资金额
            userAccountMapper.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());

            //新增投资人交易流水
            TransFlowBO investTransFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出，出借放款，编号：" + lend.getLendNo());//项目编号
            transFlowService.saveTransFlow(investTransFlowBO);
        });
        log.info("之前可以");
        //放款成功生成借款人还款计划和投资人回款计划
        this.repaymentPlan(lend);
        log.info("能到这里吗？");
    }

    /**
     * 还款计划
     *
     * @param lend
     */
    private void repaymentPlan(Lend lend) {

        //创建还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();
        for (int i = 1; i <= len; i++) {
            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            //填充基本属性
            //创建还款计划对象
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());

            lendReturn.setFee(new BigDecimal("0"));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);

            //判断是否是最后一期还款
            if(i == len){//最后一期
                lendReturn.setLast(true);
            }else{
                lendReturn.setLast(false);
            }

            //设置还款状态
            lendReturn.setStatus(0);

            //将还款对象加入还款计划列表
            lendReturnList.add(lendReturn);
        }

        //批量保存还款计划
        lendReturnService.saveBatch(lendReturnList);


        //生成期数和还款记录的id对应的键值对集合
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        //创建所有投资的所有回款记录列表
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();

        //获取当前标的下的所有的已支付的投资
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);

        for (LendItem lendItem : lendItemList) {
            //根据投资记录的id调用回款计划生成的方法，得到当前这笔投资的回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            // 将当前这笔投资的回款计划列表  放入  所有投资的所有回款记录列表
            lendItemReturnAllList.addAll(lendItemReturnList);
        }



        //遍历还款记录列表
        for (LendReturn lendReturn : lendReturnList) {

            //通过filter、map、reduce将相关期数的回款数据过滤出来
            //将当前期数的所有投资人的数据相加，就是当前期数的所有投资人的回款数据（本金、利息、总金额）
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            //将计算出的数据填充入还款计划记录：设置本金、利息、总金额
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }

        //批量更新还款计划列表
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划
     *
     * @param lendItemId
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {

        //获取当前投资记录信息
        LendItem lendItem = lendItemService.getById(lendItemId);

        //调用工具类计算还款本金和利息，存储为集合
        // {key：value}
        // {期数：本金|利息}
        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();//当前期数
            // 根据当前期数，获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            //创建回款计划记录
            LendItemReturn lendItemReturn = new LendItemReturn();
            //将还款记录关联到回款记录
            lendItemReturn.setLendReturnId(lendReturnId);
            //设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            //计算回款本金、利息和总额（注意最后一个月的计算）
            if(currentPeriod.intValue() == lend.getPeriod().intValue()){//最后一期
                //本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);

            }else{//非最后一期
                //本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                //利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            //回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));

            //设置回款状态和是否逾期等其他属性
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            //将回款记录放入回款列表
            lendItemReturnList.add(lendItemReturn);
        }

        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }

}
