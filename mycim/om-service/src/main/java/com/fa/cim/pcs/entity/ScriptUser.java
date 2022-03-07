package com.fa.cim.pcs.entity;

import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.pcs.annotations.PcsEntity;

/**
 * <p>ScriptUser .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:37
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptUser extends ScriptEntity<CimPerson> {

    ScriptUser(CimPerson bizObject) {
        super(bizObject);
    }

    /**
     * Get identifier of the User.
     *
     * @return user id
     * @version 1.0
     * @author ZQI
     * @date 2019/12/26 13:52
     */
    public String userId() {
        return bizObject.getIdentifier();
    }

}
