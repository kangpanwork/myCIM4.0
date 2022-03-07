package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IEAPMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.middleware.standard.api.caller.RemoteManagerFactory;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.remote.IEAPRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/18                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/18 20:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class EAPMethod implements IEAPMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RemoteManagerFactory remoteManagerFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private MachineManager machineManager;

    public IEAPRemoteManager eapRemoteManager(Infos.ObjCommon objCommon,User user, ObjectIdentifier equipmentID, ObjectIdentifier stockerID, Boolean onlineModeCheck){
        String serverName = null;
        //step1 - check User
        log.info("step1 - eapRemoteManager: check User");
        if (null != user){
            if (ObjectIdentifier.equalsWithValue(BizConstant.SP_TCS_PERSON, user.getUserID())) {
                return null;
            }
        }
        //step2 - check ServerName
        log.info("step2 - eapRemoteManager: check ServerName");
        //step2.1 - check equipmentID or StockerID mean equipmentID
        log.info("step2.1 - eapRemoteManager: check equipmentID or StockerID mean equipmentID");
        if (null != equipmentID){
            Machine machine = null;
            if (ObjectIdentifier.isEmptyWithRefKey(equipmentID)){
                Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID),retCodeConfig.getNotFoundEqp(),"******");
                machine = machineManager.findMachineNamed(equipmentID.getValue());
                if (null == machine){
                    machine = machineManager.findStorageMachineNamed(equipmentID.getValue());
                }
            }else {
                machine = baseCoreFactory.getBO(CimMachine.class, equipmentID.getReferenceKey());
            }
            Validations.check(null == machine,retCodeConfig.getNotFoundMachine(),ObjectIdentifier.fetchValue(equipmentID));
            Boolean storageMachine = machine.isStorageMachine();
            if (CimBooleanUtils.isTrue(storageMachine)){
                //stocker
                CimStorageMachine stocker = (CimStorageMachine) machine;
                Validations.check(null == stocker,retCodeConfigEx.getUnexpectedNilObject());
                String stockerType = stocker.getStockerType();
                Validations.check(!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE,stockerType),new OmCode(retCodeConfigEx.getStkTypeDifferent(),stockerType));
                serverName = stocker.getCellController();
            }else {
                //equipment
                CimMachine equipment = (CimMachine) machine;
                Validations.check(null == equipment,retCodeConfigEx.getUnexpectedNilObject());
                serverName = equipment.getCellController();
            }
        }
        //step2.2 - check stockerID
        log.info("step2.2 - eapRemoteManager: check stockerID");
        if (null != stockerID){
            CimStorageMachine stocker = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
            Validations.check(null == stocker,retCodeConfigEx.getUnexpectedNilObject());
            serverName = stocker.getCellController();
        }
        if (CimStringUtils.isEmpty(serverName)){
            return null;
        }
        //step3 - check online-mode
        log.info("step3 - eapRemoteManager: check online-mode");
        if (CimBooleanUtils.isTrue(onlineModeCheck)){
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
            if (CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
                String sendToEAPFlg = StandardProperties.OM_EAP_SEND_IN_OFFLINE.getValue();
                if (!CimStringUtils.equals(BizConstant.VALUE_ONE,sendToEAPFlg)){
                    return null;
                }
            }
        }
        //check ok and connect to EAP
        log.info("step4 - eqpRemoteManager: connect to EAP manager");
        IEAPRemoteManager manager = null;
        try {
            manager = remoteManagerFactory.connect(IEAPRemoteManager.class, serverName).manager();
        } catch (CimIntegrationException e) {
            Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
        }
        return manager;
    }
}
