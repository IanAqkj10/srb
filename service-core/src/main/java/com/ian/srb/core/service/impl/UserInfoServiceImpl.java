package com.ian.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ian.srb.base.utils.JwtUtils;
import com.ian.srb.common.exception.Assert;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.common.utils.MD5;
import com.ian.srb.core.mapper.UserAccountMapper;
import com.ian.srb.core.mapper.UserInfoMapper;
import com.ian.srb.core.mapper.UserLoginRecordMapper;
import com.ian.srb.core.pojo.*;
import com.ian.srb.core.service.UserInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserLoginRecordMapper userLoginRecordMapper;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(RegisterVO registerVO) {

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("mobile", registerVO.getMobile());
        Integer count = baseMapper.selectCount(userInfoQueryWrapper);
        Assert.isTrue(count == 0, ResponseEnum.MOBILE_EXIST_ERROR);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setName(registerVO.getMobile());
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setStatus(UserInfo.STATUS_NORMAL); //正常
        userInfo.setHeadImg("https://iansave.oss-cn-guangzhou.aliyuncs.com/qaq/wallhaven-pk997m.jpg");

        baseMapper.insert(userInfo);


        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);


    }

    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {

        Integer userType = loginVO.getUserType();
        String password = loginVO.getPassword();
        String mobile = loginVO.getMobile();

        //首先判断手机号和类型是否存在
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        userInfoQueryWrapper.eq("mobile", mobile)
                .eq("user_type", userType);

        UserInfo userInfo = baseMapper.selectOne(userInfoQueryWrapper);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);

        //判断密码是否正确
        String encrypt = MD5.encrypt(password);
        Assert.equals(encrypt, userInfo.getPassword(), ResponseEnum.LOGIN_PASSWORD_ERROR);


        //判断账号是否被禁用
        Integer status = userInfo.getStatus();
        Assert.equals(status, UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);

        //记录用户登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(ip);
        userLoginRecordMapper.insert(userLoginRecord);


        //设置返回的已被封装的（UserInfoVO）用户信息，其中字段tocker为携带的tocken信息
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setToken(token);
        userInfoVO.setName(userInfo.getName());
        userInfoVO.setNickName(userInfo.getNickName());
        userInfoVO.setHeadImg(userInfo.getHeadImg());
        userInfoVO.setMobile(userInfo.getMobile());
        userInfoVO.setUserType(userType);

        return userInfoVO;
    }

    @Override
    public IPage<UserInfo> listpage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery) {

        Integer userType = userInfoQuery.getUserType();
        Integer status = userInfoQuery.getStatus();
        String mobile = userInfoQuery.getMobile();


        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        if (userInfoQuery == null) {
            return baseMapper.selectPage(pageParam, null);
        }

        userInfoQueryWrapper.eq(status != null, "status", status)
                .eq(userType != null, "user_type", userType)
                .eq(StringUtils.isNotBlank(mobile), "mobile", mobile);

        return baseMapper.selectPage(pageParam, userInfoQueryWrapper);

    }

    @Override
    public void lock(Long id, Integer status) {

        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);

    }

    @Override
    public boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        userInfoQueryWrapper.eq("mobile", mobile);

        Integer count = baseMapper.selectCount(userInfoQueryWrapper);

        return count > 0;
    }

    @Override
    public UserIndexVO getIndexUserInfo(Long userId) {

        //用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //账户信息
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = userAccountMapper.selectOne(userAccountQueryWrapper);

        //登录信息
        QueryWrapper<UserLoginRecord> userLoginRecordQueryWrapper = new QueryWrapper<>();
        userLoginRecordQueryWrapper
                .eq("user_id", userId)
                .orderByDesc("id")
                .last("limit 1");
        UserLoginRecord userLoginRecord = userLoginRecordMapper.selectOne(userLoginRecordQueryWrapper);
      //  result.put("userLoginRecord", userLoginRecord);

        //组装结果数据
        UserIndexVO userIndexVO = new UserIndexVO();
        userIndexVO.setUserId(userInfo.getId());
        userIndexVO.setUserType(userInfo.getUserType());
        userIndexVO.setName(userInfo.getName());
        userIndexVO.setNickName(userInfo.getNickName());
        userIndexVO.setHeadImg(userInfo.getHeadImg());
        userIndexVO.setBindStatus(userInfo.getBindStatus());
        userIndexVO.setAmount(userAccount.getAmount());
        userIndexVO.setFreezeAmount(userAccount.getFreezeAmount());
        userIndexVO.setLastLoginTime(userLoginRecord.getCreateTime());

        return userIndexVO;
    }
}
