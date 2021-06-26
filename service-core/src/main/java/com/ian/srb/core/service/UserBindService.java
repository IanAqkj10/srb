package com.ian.srb.core.service;

import com.ian.srb.core.pojo.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ian.srb.core.pojo.UserBindVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
public interface UserBindService extends IService<UserBind> {

    String commitBindUser(UserBindVO userBindVO, Long userId);

    void notify(Map<String, Object> paramMap);

    String getBindCodeByUserId(Long userId);
}
