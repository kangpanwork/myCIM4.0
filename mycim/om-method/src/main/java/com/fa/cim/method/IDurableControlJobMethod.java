package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;

import java.util.List;

/**
 * <p>IDurableControlJobMethod .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/6/22 13:02
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IDurableControlJobMethod {

    /**
     * Get durable control job list.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/22 13:08
     */
    List<Infos.DurableControlJobListInfo> durableControlJobListGetDR(Infos .ObjCommon objCommon, Inputs.DurableControlJobListGetDRIn paramIn);

    /**
     * Get durable list from durable control job.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/22 14:46
     */
    List<Infos.StartDurable> durableControlJobDurableListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID);

    /**
     * Get start reservation information of durable control job
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/6/24 16:04
     */
    Outputs.DurableControlJobStartReserveInformationGetOut durableControlJobStartReserveInformationGet(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID);

    ObjectIdentifier durableControlJobCreate(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID,String durableCategory,List<Infos.StartDurable> strStartDurables);

    void durableDurableControlJobIDSet(Infos.ObjCommon objCommon,ObjectIdentifier durableID,ObjectIdentifier durableControlJobID ,String durableCategory);

    void durableControlJobStatusChange(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID, String durableControlJobStatus);

    void durableControlJobDelete(Infos.ObjCommon objCommon, ObjectIdentifier durableControlJobID, String actionType);
}
