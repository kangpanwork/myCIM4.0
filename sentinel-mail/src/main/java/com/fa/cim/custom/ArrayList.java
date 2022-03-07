package com.fa.cim.custom;

import java.util.Arrays;
import java.util.List;

/**
 * description:c
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 17:20:50
 */
public class ArrayList<E> extends java.util.ArrayList<E> implements List<E> {

    private static final long serialVersionUID = -6427371700294183413L;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param es
   * @param es
     * @author Ho
     * @date
     */
    public ArrayList(E...es){
        addAll(Arrays.asList(es));
    }

}
