package com.ian.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ian.srb.core.mapper.DictMapper;
import com.ian.srb.core.pojo.ExcelDict;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:IanJ
 * @date:2021/6/15 20:00
 */

@NoArgsConstructor
public class ExcelDictlisten extends AnalysisEventListener<ExcelDict> {


    private static final int BATCH_COUNT = 5;
    List<ExcelDict> list = new ArrayList();
    private DictMapper dictMapper;

    public ExcelDictlisten(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }


    @Override
    public void invoke(ExcelDict data, AnalysisContext analysisContext) {
      //  log.info("装载记录");
        list.add(data);
        if (list.size() >= BATCH_COUNT) {
            savedata();
            list.clear();
        }
       // log.info("每一条数据：{}",data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            savedata();
    }

    private void savedata() {
        dictMapper.insertBatch(list);
    }
}
