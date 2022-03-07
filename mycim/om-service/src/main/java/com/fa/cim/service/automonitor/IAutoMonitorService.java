package com.fa.cim.service.automonitor;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

public interface IAutoMonitorService {
    Results.AMJObLotDeleteReqResult sxAMJObLotDeleteReq(Infos.ObjCommon objCommon, Params.AMJObLotDeleteReqInParams amjObLotDeleteReqInParams);
    Results.AMJobLotReserveReqResult sxAMJobLotReserveReq(Infos.ObjCommon objCommon, Params.AMJobLotReserveReqInParams params);
    String sxAMJobStatusChangeRpt(Infos.ObjCommon objCommon, Params.AMJobStatusChangeRptInParm params);
    String sxAMRecoverReq(Infos.ObjCommon objCommon, Params.AMRecoverReqParams params);
    String sxAMScheduleChgReq(Infos.ObjCommon objCommon, Params.EqpMonitorScheduleUpdateInParm params);
    void sxAMSetReq(Infos.ObjCommon objCommon, Params.AMSetReqInParm params);

    Results.AMStatusChangeRptResult sxAMStatusChangeRpt(Infos.ObjCommon objCommon, Params.AMStatusChangeRptInParm params);
    Results.AMVerifyReqResult sxAMVerifyReq(Infos.ObjCommon objCommon, Params.AMVerifyReqInParams amVerifyReqInParams);

    void sxEqpMonitorWaferUsedCountUpdateReq(Infos.ObjCommon objCommon, Params.EqpMonitorUsedCountUpdateReqInParam eqpMonitorUsedCountUpdateReqInParam);

}
