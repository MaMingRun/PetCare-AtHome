package com.mr.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    /** 是否有数据 */
    private boolean success;
    /** 列表数据 */
    private List<T> list;
    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private int page;
    /** 每页大小 */
    private int size;

    public PageResult(List<T> list, long total, int page, int size) {
        this.success = true;
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
