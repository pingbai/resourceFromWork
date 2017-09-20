package com.pachira.dc.keepUp.impl;

import com.pachira.dc.keepUp.StoreQueue;
import com.pachira.dc.thread.ProcessedBatchMessage;
import com.pachira.psae.common.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author baiping
 * @version 1.0
 * @date 2017/8/26
 */
public class UnpostedMessageStorage implements StoreQueue<ProcessedBatchMessage>{
    private Log log = LogFactory.getLog(UnpostedMessageStorage.class);

    private Map<String,Map<ProcessedBatchMessage,Integer>> map ;

    private boolean filted = false;

    public UnpostedMessageStorage(){
    }

    @Override
    public void load() {
        // TODO: 2017/8/26 从硬盘上加载数据
        this.map = new HashMap<>(64);
    }

    @Override
    public boolean push(ProcessedBatchMessage message) {
        Map<ProcessedBatchMessage,Integer> temp = new HashMap<>();
        temp.put(message,DataStatus.READY);
        map.put(message.getBatchID(),temp);
        // TODO: 2017/8/26 将push的数据持久化到内存中
        return true;
    }

    @Override
    public ProcessedBatchMessage pop() {
        if(filted) {
            if (!CollectionUtils.isAbsEmpty(map)) {
                Set<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> entries = map.entrySet();
                for (Iterator<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> it = entries.iterator(); it.hasNext(); ) {
                    Map.Entry<String, Map<ProcessedBatchMessage, Integer>> entry = it.next();
                    Map<ProcessedBatchMessage, Integer> value = entry.getValue();
                    Integer dataStatus = value.values().iterator().next();
                    if (dataStatus == DataStatus.SUCCEED) {
                        continue;
                    }
                    return value.keySet().iterator().next();
                }
            }
            log.warn("no message need post at the moment");
            return null;
        }
        log.warn("please filter the queue first");
        return null;
    }

    @Override
    public void filter(List<String> dataIdList) {//pop前必须完成
        if(CollectionUtils.isAbsEmpty(dataIdList)){
            log.warn("no posted message");
            return ;
        }
        if (!CollectionUtils.isAbsEmpty(map)) {
            Set<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> entries = map.entrySet();
            for (Iterator<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> it = entries.iterator(); it.hasNext(); ) {
                Map.Entry<String, Map<ProcessedBatchMessage, Integer>> entry = it.next();
                Map<ProcessedBatchMessage, Integer> value = entry.getValue();
                Integer dataStatus = value.values().iterator().next();
                if (dataStatus == DataStatus.READY) {
                     if(dataIdList.contains(entry.getKey())){
                         value.put(value.keySet().iterator().next(),DataStatus.SUCCEED);
                         // TODO: 2017/8/26  将状态写入文件
                     }
                }
            }
        }
        filted = true;
    }

    @Override
    public void callBack(String id) {
        if(map.containsKey(id)){
            Map<ProcessedBatchMessage, Integer> innerMap = map.get(id);
            innerMap.put(innerMap.keySet().iterator().next(),DataStatus.SUCCEED);
            // TODO: 2017/8/26 将消息状态写入文件
        }else {
            log.warn("invalid batchId");
        }
    }

    @Override
    public void clean() {
        if (!CollectionUtils.isAbsEmpty(map)) {
            Set<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> entries = map.entrySet();
            for (Iterator<Map.Entry<String, Map<ProcessedBatchMessage, Integer>>> it = entries.iterator(); it.hasNext(); ) {
                Map.Entry<String, Map<ProcessedBatchMessage, Integer>> entry = it.next();
                Map<ProcessedBatchMessage, Integer> value = entry.getValue();
                Integer dataStatus = value.values().iterator().next();
                if (dataStatus == DataStatus.SUCCEED) {
                    it.remove();
                    // TODO: 2017/8/26  将移除的数据从硬盘中删除
                }
            }
        }
    }

    public Map<String, Map<ProcessedBatchMessage, Integer>> getMap() {
        return map;
    }

    public Set<String> getUnpostedMessageIdLst() {
        return map.keySet();
    }
}
