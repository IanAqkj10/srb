package com.ian.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.*;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);

    IPage<UserInfo> listpage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery);

    void lock(Long id ,Integer status);

    boolean checkMobile(String mobile);

    UserIndexVO getIndexUserInfo(Long userId);
}
