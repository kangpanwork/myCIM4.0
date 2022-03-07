package com.fa.cim.common.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/5/25        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/5/25 15:31
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class OmPage<T>  implements Serializable {
    private static final Long serialVersionUID = -12121021232344459L;
    private long totalElements;
    private int totalPages;
    private int number;
    private int numberOfElements;
    private int size;
    private Sort sort;
    private List<T> content;

    public OmPage(org.springframework.data.domain.Page page) {
        init(page);
    }

    public OmPage init(org.springframework.data.domain.Page page) {
        if (null == page) {
            return this;
        }
        this.setNumber(page.getNumber());
        this.setNumberOfElements(page.getNumberOfElements());
        this.setSize((int)page.getSize());
        this.setSort(page.getSort());
        this.setTotalElements(page.getTotalElements());
        this.setTotalPages(page.getTotalPages());
        this.setContent(page.getContent());        // don't init the content, because it usually be set later.
        return this;
    }
}
