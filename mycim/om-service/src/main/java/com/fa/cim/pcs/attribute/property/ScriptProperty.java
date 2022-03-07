package com.fa.cim.pcs.attribute.property;

import com.fa.cim.newcore.bo.abstractgroup.CimPropertyBase;
import com.fa.cim.newcore.enums.PropertyTypeEnum;

/**
 * description:
 * <p>Property .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/30         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/30 16:13
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public abstract class ScriptProperty<B extends CimPropertyBase> {

    protected B property;

    public B get() {
        return property;
    }

    ScriptProperty(B property) {
        this.property = property;
    }

    public PropertyTypeEnum getType() {
        return property.getDataType();
    }

    public void setIdentifier(String identifier) {
        property.setIdentifier(identifier);
    }

    public String getIdentifier() {
        return property.getIdentifier();
    }

    @Override
    public String toString() {
        String simpleName = property.getClass().getSimpleName();
        if(simpleName.contains("$$")){
            simpleName = simpleName.substring(0, simpleName.indexOf("$$"));
        }
        String primaryKey = property.getPrimaryKey();
        return String.format("Property -> [BO : %s , PrimaryKey : %s]", simpleName, primaryKey);
    }
}
