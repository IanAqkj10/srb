package com.ian.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ian.srb.core.listener.ExcelDictlisten;
import com.ian.srb.core.mapper.DictMapper;
import com.ian.srb.core.pojo.Dict;
import com.ian.srb.core.pojo.ExcelDict;
import com.ian.srb.core.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Ian
 * @since 2021-06-14
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDict.class, new ExcelDictlisten(baseMapper)).sheet().doRead();
        // log.info("importData finished");
    }

    @Override
    public List<ExcelDict> listDictData() {
        List<Dict> dictList = baseMapper.selectList(null);
        //创建ExcelDict列表，将Dict列表转换成ExcelDict列表
        ArrayList<ExcelDict> excelDicList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {

            ExcelDict excelDict = new ExcelDict();
            BeanUtils.copyProperties(dict, excelDict);
            excelDicList.add(excelDict);
        });
        return excelDicList;
    }


    @Override
    public List<Dict> getlistBypd(Long parentId) {

        List<Dict> dictList = null;
        // 首先判断redis有没有， 如果有直接返回。
        try {
            dictList = (List<Dict>) redisTemplate.opsForValue().get("srb:core:dictList:" + parentId);
            if (dictList != null) {
                log.info("从redis获取");
                return dictList;
            }
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }
        log.info("从数据库中取值");
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", parentId);
        //根据where parent_id = 传进来的id，得出一个list
        dictList = baseMapper.selectList(dictQueryWrapper);
        //检查每一条有没有子节点，如果有，dict的haschildren就赋值为true
        dictList.forEach(dict -> {
            boolean haschildren = this.haschildren(dict.getId());
            dict.setHasChildren(haschildren);
        });

        try {
            redisTemplate.opsForValue().set("srb:core:dictList" + parentId, dictList, 5, TimeUnit.MINUTES);
            log.info("数据存入redis");
        } catch (Exception e) {
            // ExceptionUtils.getStackTrace(e);
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));//此处不抛出异常，继续执行后面的代码
        }

        return dictList;
    }


    @Override
    public List<Dict> findByDictCode(String dictCode) {

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);
        return this.getlistBypd(dict.getId());

    }

    @Override
    public String getNameByDictCodeAndValue(String dictCode, Integer value) {


        //首先根据dict_code查出行业，学习这种大类，再根据这大类的id放进parent_id和值查出小类的名字
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("dict_code", dictCode);
        Dict dict = baseMapper.selectOne(dictQueryWrapper);

        if (dict == null) {
            return "";
        }
        //select name from dict where parent_id = dict.getId() and value = value;
        dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("value", value)
                .eq("parent_id", dict.getId())
                .select("name");
        List<Object> objects = baseMapper.selectObjs(dictQueryWrapper);

        if (objects == null) {
            return "";
        }
        String name = (String) objects.get(0);

        return name;

    }

    //这个传参id是每个对象的id
    private boolean haschildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id", id);
        //把对象的id与parent_id匹配，查出大于一条则为子节点
        Integer count = baseMapper.selectCount(dictQueryWrapper);
        if (count > 0) {
            return true;
        }
        return false;
    }
}
