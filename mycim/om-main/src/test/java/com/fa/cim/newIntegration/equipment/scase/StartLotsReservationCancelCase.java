package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * description:
 * <p><br/></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @return
 * @exception
 * @author HO
 * @date 2019/11/27 15:52
 */
@Service
@Slf4j
public class StartLotsReservationCancelCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    public Results.MoveInReserveCancelReqResult startLotsReservationCancel_normal(){
        startLotsReservationCase.startLotsReservation();
        ObjectIdentifier equipmentID=testCommonData.getEQUIPMENTID();
        return this.startLotsReservationCancel_normal(equipmentID);
    }

    public Results.MoveInReserveCancelReqResult startLotsReservationCancel_normal(ObjectIdentifier equipmentID){
        Results.EqpInfoInqResult eqpInfo= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        List<ObjectIdentifier> controlJobs=eqpInfo.getEquipmentReservedControlJobInfo().getMoveInReservedControlJobInfoList().stream().map(
                startReservedControlJobInfo -> startReservedControlJobInfo.getControlJobID()).collect(Collectors.toList());

        Params.LotListByCJInqParams lotListByCJInqParams= new Params.LotListByCJInqParams();
        lotListByCJInqParams.setControlJobIDs(controlJobs);
        lotListByCJInqParams.setUser(testCommonData.getUSER());
        List<Infos.ControlJobInfo> lotController = electronicInformationTestCase.lotListByCJInqCase(lotListByCJInqParams);
        Params.MoveInReserveCancelReqParams params=new Params.MoveInReserveCancelReqParams();
        params.setControlJobID(controlJobs.get(0));
        params.setEquipmentID(testCommonData.getEQUIPMENTID());
        params.setUser(testCommonData.getUSER());
        return equipmentTestCase.startLotsReservationCancel(params);
    }


}