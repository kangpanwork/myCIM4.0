package com.fa.cim.pcs.attribute.property;

import com.fa.cim.newcore.bo.abstractgroup.CimPropertyString;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * description:
 * <p>StringProperty .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:14
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@PcsEntity
public class StringProperty extends ScriptProperty<CimPropertyString> {

    public StringProperty(CimPropertyString property) {
        super(property);
    }

    public String getValue() {
        return property.getValue();
    }

    public void setValue(String value) {
        property.setValue(value);
    }
}
