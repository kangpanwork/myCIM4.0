package com.fa.cim.Custom;

import java.util.Arrays;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 17:20:50
 */
public class ArrayList<E> extends java.util.ArrayList<E> implements List<E> {

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
