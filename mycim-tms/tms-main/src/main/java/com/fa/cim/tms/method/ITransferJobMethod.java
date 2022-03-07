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
 * @date: 2020/10/14 17:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransferJobMethod {


    List<Infos.TransferJobInfo> transferJobGet(Infos.ObjCommon objCommon, String inquiryType, List<ObjectIdentifier> seqCarrierID, ObjectIdentifier toMachineID, ObjectIdentifier fromMachineID, List<String> seqJobID);

    void transferJobDel(Infos.ObjCommon objCommon, String deleteType, Infos.TransferJobDeleteInfo transferJobDeleteInfo);

    void transferJobPut(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobInfoList);

    List<Infos.TransferJobInfo> transferCarrierGet(Infos.ObjCommon objCommon, String jobID, String carrierJobID);

    void transferJobMod(Infos.ObjCommon objCommon, String jobID, String jobStatus, String carrierJobID, ObjectIdentifier carrierID, String carrierJobStatus, Boolean carrierJobRemoveFlag);
}
