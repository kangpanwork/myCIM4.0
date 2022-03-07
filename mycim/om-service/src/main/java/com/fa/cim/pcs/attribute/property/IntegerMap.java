package com.fa.cim.pcs.attribute.property;

import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertyTableSI;
import com.fa.cim.newcore.dto.property.Property;
import com.fa.cim.pcs.annotations.PcsEntity;

import java.util.List;

/**
 * description:
 * <p>TableSIProperty .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:17
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@PcsEntity
public class IntegerMap extends ScriptProperty<CimPropertyTableSI> {

    public IntegerMap(CimPropertyTableSI property) {
        super(property);
    }

    public int getValueNamed(String keyValue) {
        return CimNumberUtils.intValue(property.getValueNamed(keyValue));
    }

    public void setValueNamed(int value, String keyValue) {
        property.setValueNamed(value, keyValue);
    }

    public void removeValueNamed(String keyValue) {
        property.removeValueNamed(keyValue);
    }

    public List<Property.TableSIData> allValues() {
        return property.allValues();
    }
}
