package com.fa.cim.pcs.attribute.property;

import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertyInteger;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * description:
 * <p>IntegerProperty .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:15
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@PcsEntity
public class IntegerProperty extends ScriptProperty<CimPropertyInteger> {

    public IntegerProperty(CimPropertyInteger property) {
        super(property);
    }

    public int getValue() {
        return CimNumberUtils.intValue(property.getValue());
    }

    public void setValue(int value) {
        property.setValue(value);
    }
}
