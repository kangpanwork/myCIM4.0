package com.fa.cim.pcs.attribute.property;

import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertyReal;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * description:
 * <p>RealProperty .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:16
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@PcsEntity
public class DecimalProperty extends ScriptProperty<CimPropertyReal> {

    public DecimalProperty(CimPropertyReal property) {
        super(property);
    }

    public double getValue() {
        return CimNumberUtils.doubleValue(property.getValue());
    }

    public void setValue(double value) {
        property.setValue(value);
    }
}
