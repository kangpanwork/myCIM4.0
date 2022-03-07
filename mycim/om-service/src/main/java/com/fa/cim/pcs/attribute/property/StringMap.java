package com.fa.cim.pcs.attribute.property;

import com.fa.cim.newcore.bo.abstractgroup.CimPropertyTableSS;
import com.fa.cim.newcore.dto.property.Property;
import com.fa.cim.pcs.annotations.PcsEntity;

import java.util.List;

/**
 * description:
 * <p>TableSSProperty .<br/></p>
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
public class StringMap extends ScriptProperty<CimPropertyTableSS> {

    public StringMap(CimPropertyTableSS property) {
        super(property);
    }

    public String getValueNamed(String keyValue) {
        return property.getValueNamed(keyValue);
    }

    public void setValueNamed(String value, String keyValue) {
        property.setValueNamed(value, keyValue);
    }

    public void removeValueNamed(String keyValue) {
        property.removeValueNamed(keyValue);
    }

    public List<Property.TableSSData> allValues() {
        return property.allValues();
    }
}
