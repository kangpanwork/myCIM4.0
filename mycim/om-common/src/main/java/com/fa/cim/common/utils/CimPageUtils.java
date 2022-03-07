package com.fa.cim.common.utils;

import com.fa.cim.common.support.SortDto;
import org.springframework.data.domain.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Panda
 * @since 2018/5/11
 */

public class CimPageUtils {

    public static final Integer DEFAULT_SIZE = 10;

    private CimPageUtils() {
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param data      dataList
     * @param page      this page
     * @param size      this size
     * @param totalSize totalSize
     * @see #convertListToPage(List, Integer, Integer)
     * @return org.springframework.data.domain.CimPage
     * @author Panda
     * @date: 2018/5/11
     */
    @Deprecated
    public static Page getPage(List data, Integer page, Integer size, Long totalSize) {
        if (null == data || null == totalSize) {
            return null;
        }
        page = null == page ? 0 : page;
        size = null == size ? DEFAULT_SIZE : size;
        return new PageImpl(data, getPageable(page, size), totalSize);
    }

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page this page
     * @param size this size
     * @return org.springframework.data.domain.Pageable
     * @author Panda
     * @date: 2018/5/11
     */
    public static Pageable getPageable(Integer page, Integer size) {
        Integer pageIndex = (null == page) ? 0 : (page - 1 <= 0 ? 0 : page - 1);
        Pageable pageable = new PageRequest(pageIndex, size);
        return pageable;
    }

    /**
     * description:
     * 获取基础分页对象
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page 获取第几页
     * @param size 每页条数
     * @param dtos 排序对象数组
     * @return
     * @author PlayBoy
     * @date 2018/7/31
     */
    public static Pageable basicPage(Integer page, Integer size, SortDto... dtos) {
        Sort sort = SortTools.basicSort(dtos);
        //page start with 1
        page = (page == null || page < 0) ? 0 : (page - 1 <= 0 ? 0 : page - 1);
        size = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        Pageable pageable = new PageRequest(page, size, sort);
        return pageable;
    }

    /**
     * description:
     * 获取基础分页对象，每页条数默认10条
     * - 默认以id降序排序
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page 获取第几页
     * @return
     * @author PlayBoy
     * @date 2018/7/31
     */
    public static Pageable basicPage(Integer page) {
        return basicPage(page, 0, new SortDto(SortTools.DEFAULT_SORT, SortTools.DEFAULT_SORT_COLUMN));
    }

    /**
     * description:
     * 获取基础分页对象，每页条数默认10条
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page 获取第几页
     * @param dtos 排序对象数组
     * @return
     * @author PlayBoy
     * @date 2018/7/31
     */
    public static Pageable basicPage(Integer page, SortDto... dtos) {
        return basicPage(page, 0, dtos);
    }

    /**
     * description:
     * 获取基础分页对象，排序方式默认降序
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page       获取第几页
     * @param size       每页条数
     * @param orderField 排序字段
     * @return
     * @author PlayBoy
     * @date 2018/7/31
     */
    public static Pageable basicPage(Integer page, Integer size, String orderField) {
        return basicPage(page, size, new SortDto(SortTools.DEFAULT_SORT, orderField));
    }

    /**
     * description:
     * 获取基础分页对象
     * - 每页条数默认10条
     * - 排序方式默认降序
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param page       获取第几页
     * @param orderField 排序字段
     * @return -
     * @author PlayBoy
     * @since 2018/7/31
     */
    public static Pageable basicPage(Integer page, String orderField) {
        return basicPage(page, 0, new SortDto(SortTools.DEFAULT_SORT, orderField));
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/10/18       ######              Neko              Update deprecated PageRequest constructor
     *
     * @author Nyx
     * @since 2019/1/30 16:54
     * @param content - whole list
     * @param page the page number currently on
     * @param pageSize -
     * @return org.springframework.data.domain.Page
     */
    public static Page convertListToPage(List content, Integer page, Integer pageSize) {
        page = null == page || page < 2 ? 0 : page - 1;
        pageSize = null == pageSize ? CimPageUtils.DEFAULT_SIZE : pageSize;
        return new PageImpl(CimArrayUtils.getPageList(content, page, pageSize), PageRequest.of(page, pageSize), content.size());
    }

    public static Page convertListToPage(List content, Integer page, Integer pageSize,long total) {
        page = null == page || page < 2 ? 0 : page - 1;
        pageSize = null == pageSize ? CimPageUtils.DEFAULT_SIZE : pageSize;
        return new PageImpl(CimArrayUtils.getPageList(content, page, pageSize), PageRequest.of(page, pageSize), total);
    }

}
