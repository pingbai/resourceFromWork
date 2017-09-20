package com.pachira.dc.keepUp;

import java.util.List;
import java.util.Map;

/**
 * @author baiping
 * @version 1.0
 * @date 2017/8/26
 */
public interface StoreQueue<T> {
    /**
     * 启动时，加载数据
     */
    void load();

    /**
     * 存储数据，状态设为 READY
     * @param t
     * @return
     */
    boolean push(T t);

    /**
     * 取出队首数据，若数据为  READY 则出，并更改状态 POPED
     *
     * @return
     */
    T pop();

    /**
     * 过滤已经处理过，状态未回写的数据，并将更改状态 SUCCEED
     */
    void filter(List<String> dataIdList);

    /**
     * 回写任务状态，将状态更改为 SUCCEED
     */
    void callBack(String id);

    /**
     * 定时清理 清除状态为 SUCCEED 的数据
     */
    void clean();

}
