package com.fa.cim.pcs.attribute.property;

import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertyTableSR;
import com.fa.cim.newcore.dto.property.Property;
import com.fa.cim.pcs.annotations.PcsEntity;

import java.util.List;

/**
 * description:
 * <p>TableSRProperty .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:18
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@PcsEntity
public class DecimalMap extends ScriptProperty<CimPropertyTableSR> {

    public DecimalMap(CimPropertyTableSR property) {
        super(property);
    }

    public double getValueNamed(String keyValue) {
        return CimNumberUtils.doubleValue(property.getValueNamed(keyValue));
    }

    public void setValueNamed(double value, String keyValue) {
        property.setValueNamed(value, keyValue);
    }

    private void removeValueNamed(String keyValue) {
        property.removeValueNamed(keyValue);
    }

    public List<Property.TableSRData> allValues() {
        return property.allValues();
    }
}
