package com.ian.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ian.srb.common.exception.Assert;
import com.ian.srb.common.result.ResponseEnum;
import com.ian.srb.core.enums.UserBindEnum;
import com.ian.srb.core.hfb.FormHelper;
import com.ian.srb.core.hfb.HfbConst;
import com.ian.srb.core.hfb.RequestHelper;
import com.ian.srb.core.mapper.UserInfoMapper;
import com.ian.srb.core.pojo.UserBind;
import com.ian.srb.core.mapper.UserBindMapper;
import com.ian.srb.core.pojo.UserBindVO;
import com.ian.srb.core.pojo.UserInfo;
import com.ian.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {

        //验证流程：首先对身份证进行判断，重复就断言抛出异常，在进行id是否重复验证
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        //SELECT * FROM user_bind where id_card =getidcar and user_id !=userid
        userBindQueryWrapper
                     .eq("id_card", userBindVO.getIdCard())
                     .ne("user_id", userId);

        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        //查出来如果有内容，则有身份证号重复了,抛出异常
        Assert.isNull(userBind,ResponseEnum.MOBILE_EXIST_ERROR);


        //查下传进来的id有没有重
        userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
         userBind = baseMapper.selectOne(userBindQueryWrapper);
        //如果为空，则进行绑定，之后存进一条userbind
        if (userBind == null) {
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);
        } else {
            //不为空，就有id，然后覆盖掉内容进行更新。
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);
        }


        Map<String, Object> map = new HashMap<>();

        map.put("agentId", HfbConst.AGENT_ID);
        map.put("agentUserId", userId);
        map.put("idCard", userBindVO.getIdCard());
        map.put("personalName", userBindVO.getName());
        map.put("bankType", userBindVO.getBankType());
        map.put("bankNo", userBindVO.getBankNo());
        map.put("mobile", userBindVO.getMobile());
        map.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        map.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        map.put("timestamp", RequestHelper.getTimestamp());
        map.put("sign", RequestHelper.getSign(map));


        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, map);

        return formStr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {

        //获取map里面的code和userid
        String bindCode = (String) paramMap.get("bindCode");
        String userId = (String) paramMap.get("agentUserId");

        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_Id",userId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);


        //更新userbind表的两个字段
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);


        //更新userinfo表的内容
        UserInfo userInfo = userInfoMapper.selectById(userId);
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public String getBindCodeByUserId(Long userId) {
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id",userId);

        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        return userBind.getBindCode();
    }
}
