package com.fa.cim.tms.method;

import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IEquipmentMethod {

    void checkEqpSendCOMPM3(Infos.ObjCommon strObjCommonIn, List<Infos.TransferJobInfo> seqTransferJobInfo, String transferJobStatus);

    void checkEqpSendCOMP(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus);

    void checkEqpTransfer(Infos.ObjCommon objCommon, ObjectIdentifier toMachineID,Boolean tmsFlag);

    void rtmsCheckEqpSendCOMP(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus);

    void rtmsCheckEqpSendCOMPM3(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus);

    Infos.MachineTypeGetDR machineTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier currMachineID);
}
