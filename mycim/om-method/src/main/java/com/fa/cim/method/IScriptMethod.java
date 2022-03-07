package com.fa.cim.method;

import com.fa.cim.dto.Infos;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/1/8                             Wind               create file
 *
 * @author Wind
 * @since 2019/1/8 09:28
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IScriptMethod {

    /**
     * Gets the Script parameters by specify class.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/14 13:57
     */
    List<Infos.UserParameterValue> scriptGetUserParameter(Infos.ObjCommon objCommon, String parameterClass, String identifier);

    /**
     * Change the Script parameters by specify class.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/5/14 13:57
     */
    void scriptSetUserParameter(Infos.ObjCommon objCommon, String parameterClass, String identifier, List<Infos.UserParameterValue> parameters);

}
