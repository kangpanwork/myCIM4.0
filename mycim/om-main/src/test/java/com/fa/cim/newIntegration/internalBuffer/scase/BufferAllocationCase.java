package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/11/26 10:40
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class BufferAllocationCase {

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    EquipmentInqController equipmentInqController;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private IAutoMonitorInqController autoMonitorInqController;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public Response addAllBufferSize() {
        ObjectIdentifier eqpID = new ObjectIdentifier("1ASH03");
        //[step1]run into eqp info screen
        //[step1-1]call einfo/eqp_info_for_ib/inq
        Params.EqpInfoForIBInqParams eqpInfoForIBInqParams = new Params.EqpInfoForIBInqParams();
        eqpInfoForIBInqParams.setUser(getUser());
        eqpInfoForIBInqParams.setEquipmentID(eqpID);
        eqpInfoForIBInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForChamberInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInprocessingLotInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInternalBufferInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPMInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForRSPPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForReservedControlJobInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStockerInfo(true);
        Response response = equipmentInqController.eqpInfoForIBInq(eqpInfoForIBInqParams);
        Validations.isSuccessWithException(response);

        //[step1-2]call eqp/eqp_monitor_list/inq
        Params.AMListInqParams amListInqParams = new Params.AMListInqParams();
        amListInqParams.setUser(getUser());
        amListInqParams.setEquipmentID(eqpID);
        amListInqParams.setEqpMonitorID(null);
        Response response1 = autoMonitorInqController.amListInq(amListInqParams);
        Validations.isSuccessWithException(response1);

        //[step2]run into buffer allocation screen
        // call eqp/eqp_buffer_resource_info/inq
        Params.EqpBufferInfoInqInParm eqpBufferInfoInqInParm = new Params.EqpBufferInfoInqInParm();
        eqpBufferInfoInqInParm.setUser(getUser());
        eqpBufferInfoInqInParm.setEquipmentID(eqpID);
        Response response2 = equipmentInqController.eqpBufferInfoInq(eqpBufferInfoInqInParm);
        List<Infos.BufferResourceInfo> bufferResourceInfoList = ((Results.EqpBufferInfoInqResult) response2.getBody()).getStrBufferResourceInfoSeq();
        Validations.isSuccessWithException(response2);
        Validations.check(CimArrayUtils.isEmpty(bufferResourceInfoList), "the bufferResourceInfoList is null!");

        //get not allocated buffer size.
        Predicate<Infos.BufferResourceInfo> predicate = t -> CimStringUtils.equals("Any Process Lot", t.getBufferCategory());
        Optional<Infos.BufferResourceInfo> anyProcessLotBufferResourceInfo = bufferResourceInfoList.stream().filter(predicate).findFirst();
        Infos.BufferResourceInfo bufferResourceInfo1 = anyProcessLotBufferResourceInfo.orElse(null);
        long smInUseCapacity = 0;
        if (bufferResourceInfo1 != null) {
            smInUseCapacity = bufferResourceInfo1.getSmInUseCapacity();
        }
        long smCapacity = anyProcessLotBufferResourceInfo.get().getSmCapacity();
        long notAllocatedCapacity = smCapacity - smInUseCapacity;

        //[step3]buffer allocation
        //call eqp/eqp_buffer_type_modify/req
        Params.EqpBufferTypeModifyReqInParm eqpBufferTypeModifyReqInParm = new Params.EqpBufferTypeModifyReqInParm();
        eqpBufferTypeModifyReqInParm.setUser(getUser());
        eqpBufferTypeModifyReqInParm.setEquipmentID(eqpID);
        List<Infos.BufferResourceUpdateInfo> bufferResourceUpdateInfoSeq = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(bufferResourceInfoList) && notAllocatedCapacity > 0; i++, notAllocatedCapacity--) {
            Infos.BufferResourceInfo bufferResourceInfo = bufferResourceInfoList.get(i);
            Infos.BufferResourceUpdateInfo bufferResourceUpdateInfo = new Infos.BufferResourceUpdateInfo();
            bufferResourceUpdateInfo.setSmCapacity(bufferResourceInfo.getSmCapacity());
            bufferResourceUpdateInfo.setDynamicCapacity(bufferResourceInfo.getDynamicCapacity());
            bufferResourceUpdateInfo.setBufferCategory(bufferResourceInfo.getBufferCategory());
            bufferResourceUpdateInfo.setNewCapacity(1L);
            bufferResourceUpdateInfoSeq.add(bufferResourceUpdateInfo);
        }

        eqpBufferTypeModifyReqInParm.setStrBufferResourceUpdateInfoSeq(bufferResourceUpdateInfoSeq);
        Response response3 = equipmentController.eqpBufferTypeModifyReq(eqpBufferTypeModifyReqInParm);
        Validations.isSuccessWithException(response3);
        return response3;
    }

    public Response addProcessLotBufferSize() {
        ObjectIdentifier eqpID = new ObjectIdentifier("1ASH03");
        //[step1]run into eqp info screen
        //[step1-1]call einfo/eqp_info_for_ib/inq
        Params.EqpInfoForIBInqParams eqpInfoForIBInqParams = new Params.EqpInfoForIBInqParams();
        eqpInfoForIBInqParams.setUser(getUser());
        eqpInfoForIBInqParams.setEquipmentID(eqpID);
        eqpInfoForIBInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForChamberInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInprocessingLotInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInternalBufferInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPMInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForRSPPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForReservedControlJobInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStockerInfo(true);
        Response response = equipmentInqController.eqpInfoForIBInq(eqpInfoForIBInqParams);
        Validations.isSuccessWithException(response);

        //[step1-2]call eqp/eqp_monitor_list/inq
        Params.AMListInqParams amListInqParams = new Params.AMListInqParams();
        amListInqParams.setUser(getUser());
        amListInqParams.setEquipmentID(eqpID);
        amListInqParams.setEqpMonitorID(null);
        Response response1 = autoMonitorInqController.amListInq(amListInqParams);
        Validations.isSuccessWithException(response1);

        //[step2]run into buffer allocation screen
        // call eqp/eqp_buffer_resource_info/inq
        Params.EqpBufferInfoInqInParm eqpBufferInfoInqInParm = new Params.EqpBufferInfoInqInParm();
        eqpBufferInfoInqInParm.setUser(getUser());
        eqpBufferInfoInqInParm.setEquipmentID(eqpID);
        Response response2 = equipmentInqController.eqpBufferInfoInq(eqpBufferInfoInqInParm);
        List<Infos.BufferResourceInfo> bufferResourceInfoList = ((Results.EqpBufferInfoInqResult) response2.getBody()).getStrBufferResourceInfoSeq();
        Validations.isSuccessWithException(response2);
        Validations.check(CimArrayUtils.isEmpty(bufferResourceInfoList), "the bufferResourceInfoList is null!");

        //get not allocated buffer size.
        Predicate<Infos.BufferResourceInfo> predicate = t -> CimStringUtils.equals("Any Process Lot", t.getBufferCategory());
        Optional<Infos.BufferResourceInfo> anyProcessLotBufferResourceInfo = bufferResourceInfoList.stream().filter(predicate).findFirst();
        Infos.BufferResourceInfo bufferResourceInfo1 = anyProcessLotBufferResourceInfo.orElse(null);
        long smInUseCapacity = 0;
        if (bufferResourceInfo1 != null) {
            smInUseCapacity = bufferResourceInfo1.getSmInUseCapacity();
        }
        long smCapacity = anyProcessLotBufferResourceInfo.get().getSmCapacity();
        long notAllocatedCapacity = smCapacity - smInUseCapacity;

        //[step3]buffer allocation
        //call eqp/eqp_buffer_type_modify/req
        Params.EqpBufferTypeModifyReqInParm eqpBufferTypeModifyReqInParm = new Params.EqpBufferTypeModifyReqInParm();
        eqpBufferTypeModifyReqInParm.setUser(getUser());
        eqpBufferTypeModifyReqInParm.setEquipmentID(eqpID);
        List<Infos.BufferResourceUpdateInfo> bufferResourceUpdateInfoSeq = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(bufferResourceInfoList) && smInUseCapacity > 0; i++, smInUseCapacity--) {
            Infos.BufferResourceInfo bufferResourceInfo = bufferResourceInfoList.get(i);
            Infos.BufferResourceUpdateInfo bufferResourceUpdateInfo = new Infos.BufferResourceUpdateInfo();
            bufferResourceUpdateInfo.setSmCapacity(bufferResourceInfo.getSmCapacity());
            bufferResourceUpdateInfo.setDynamicCapacity(bufferResourceInfo.getDynamicCapacity());
            bufferResourceUpdateInfo.setBufferCategory(bufferResourceInfo.getBufferCategory());
            if (CimStringUtils.equals(bufferResourceInfo.getBufferCategory(), "Process Lot")) {
                bufferResourceUpdateInfo.setNewCapacity(notAllocatedCapacity);
            } else {
                bufferResourceUpdateInfo.setNewCapacity(0L);
            }
            bufferResourceUpdateInfoSeq.add(bufferResourceUpdateInfo);
        }

        eqpBufferTypeModifyReqInParm.setStrBufferResourceUpdateInfoSeq(bufferResourceUpdateInfoSeq);
        Response response3 = equipmentController.eqpBufferTypeModifyReq(eqpBufferTypeModifyReqInParm);
        Validations.isSuccessWithException(response3);
        return response3;
    }


    public Response reduceProcessLotBufferSize() {
        ObjectIdentifier eqpID = new ObjectIdentifier("1ASH03");

        //[step1]first, call addProcessLotBufferSize() to make some test data
        this.addProcessLotBufferSize();

        //[step2]run into buffer allocation screen, Process Lot Buffer Dynamic: x -> 0
        // call eqp/eqp_buffer_resource_info/inq
        Params.EqpBufferInfoInqInParm eqpBufferInfoInqInParm = new Params.EqpBufferInfoInqInParm();
        eqpBufferInfoInqInParm.setUser(getUser());
        eqpBufferInfoInqInParm.setEquipmentID(eqpID);
        Response response2 = equipmentInqController.eqpBufferInfoInq(eqpBufferInfoInqInParm);
        List<Infos.BufferResourceInfo> bufferResourceInfoList = ((Results.EqpBufferInfoInqResult) response2.getBody()).getStrBufferResourceInfoSeq();
        Validations.isSuccessWithException(response2);
        Validations.check(CimArrayUtils.isEmpty(bufferResourceInfoList), "the bufferResourceInfoList is null!");
        //[step3]buffer allocation
        //call eqp/eqp_buffer_type_modify/req
        Params.EqpBufferTypeModifyReqInParm eqpBufferTypeModifyReqInParm = new Params.EqpBufferTypeModifyReqInParm();
        eqpBufferTypeModifyReqInParm.setUser(getUser());
        eqpBufferTypeModifyReqInParm.setEquipmentID(eqpID);
        List<Infos.BufferResourceUpdateInfo> bufferResourceUpdateInfoSeq = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(bufferResourceInfoList); i++) {
            Infos.BufferResourceInfo bufferResourceInfo = bufferResourceInfoList.get(i);
            Infos.BufferResourceUpdateInfo bufferResourceUpdateInfo = new Infos.BufferResourceUpdateInfo();
            bufferResourceUpdateInfo.setSmCapacity(bufferResourceInfo.getSmCapacity());
            bufferResourceUpdateInfo.setDynamicCapacity(bufferResourceInfo.getDynamicCapacity());
            bufferResourceUpdateInfo.setBufferCategory(bufferResourceInfo.getBufferCategory());
            bufferResourceUpdateInfo.setNewCapacity(0L);
            bufferResourceUpdateInfoSeq.add(bufferResourceUpdateInfo);
        }
        eqpBufferTypeModifyReqInParm.setStrBufferResourceUpdateInfoSeq(bufferResourceUpdateInfoSeq);
        Response response3 = equipmentController.eqpBufferTypeModifyReq(eqpBufferTypeModifyReqInParm);
        Validations.isSuccessWithException(response3);
        return response3;
    }
}