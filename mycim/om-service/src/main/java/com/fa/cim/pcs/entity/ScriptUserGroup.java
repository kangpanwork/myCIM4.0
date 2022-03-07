package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.person.CimUserGroup;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptUserGroup .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:38
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptUserGroup extends ScriptEntity<CimUserGroup> {

    ScriptUserGroup(CimUserGroup bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the UserGroup.
     *
     * @return user group id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:53
     */
    public String userGroupId() {
        return bizObject.getIdentifier();
    }

}
