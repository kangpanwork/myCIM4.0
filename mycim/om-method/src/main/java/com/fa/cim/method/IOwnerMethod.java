package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.newcore.bo.CimBO;

import java.util.List;

/**
 * description: This file use to define the IOwnerMethod interface.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/7/29 11:06
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOwnerMethod {

    /**
     * description: This function gets the definition information on Owner Change.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 11:21
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Outputs.ObjOwnerChangeDefinitionGetDROut ownerChangeDefinitionGetDR(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 13:39
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<CimBO> ownerChangeObjectListGet(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeObjectListGetIn in);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/29 17:30
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<CimBO> ownerChangeObjectListGetDR(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeObjectListGetDRIn input);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/30 14:27
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    <T extends CimBO> Boolean ownerChangeUpdate(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeUpdateIn in,T bo);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/30                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/30 14:45
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    List<Infos.OwnerChangeObject> ownerChangeDefObjUpdateDR(Infos.ObjCommon objCommon, Inputs.ObjOwnerChangeDefObjUpdateDRIn input);
}
