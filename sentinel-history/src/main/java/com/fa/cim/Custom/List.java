package com.fa.cim.Custom;

import com.fa.cim.utils.BaseUtils;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 14:15:39
 */
public interface List<T> extends java.util.List<T> {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param
     * @return int
     * @author Ho
     * @date 2019/2/26 14:15:45
     */
    default Integer length(){
        return BaseUtils.length(this);
    }

}
