package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IAutoDispatchControlMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/25       ********              lightyh             create file
 *
 * @author lightyh
 * @since  2019/7/25 9:50
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
public class AutoDispatchControlMethod implements IAutoDispatchControlMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public void autoDispatchControlInfoCheck(Infos.ObjCommon objCommon, Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo) {
        List<Infos.AutoDispatchControlUpdateInfo> autoDispatchControlUpdateInfoList = lotAutoDispatchControlUpdateInfo.getAutoDispatchControlUpdateInfoList();
        Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
        objAutoDispatchControlInfoGetDRIn.setLotID(lotAutoDispatchControlUpdateInfo.getLotID());
        List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfoList = this.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);

        //----------------------------//
        //  Check Delete Information  //
        //----------------------------//
        List<Infos.LotAutoDispatchControlInfo> tmpLotAutoDispatchControlInfoList = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(lotAutoDispatchControlInfoList)){
            for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : lotAutoDispatchControlInfoList){
                boolean bDeleteFlag = false;
                for (Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo : autoDispatchControlUpdateInfoList){
                    if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_DELETE)){
                        if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), autoDispatchControlUpdateInfo.getRouteID())
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), autoDispatchControlUpdateInfo.getOperationNumber())){
                            bDeleteFlag = true;
                            break;
                        }
                    }
                }
                if (!bDeleteFlag){
                    tmpLotAutoDispatchControlInfoList.add(lotAutoDispatchControlInfo);
                }
            }
        }
        //----------------------------//
        //  Check Create Information  //
        //----------------------------//
        for (Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo : autoDispatchControlUpdateInfoList){
            if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_CREATE)){
                if (ObjectIdentifier.equalsWithValue(autoDispatchControlUpdateInfo.getRouteID(), "*")
                        && CimStringUtils.equals(autoDispatchControlUpdateInfo.getOperationNumber(), "*")){
                    for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : tmpLotAutoDispatchControlInfoList){
                        if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (!ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (!ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                                && !CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        }
                    }
                } else if (!ObjectIdentifier.equalsWithValue(autoDispatchControlUpdateInfo.getRouteID(), "*")
                                && CimStringUtils.equals(autoDispatchControlUpdateInfo.getOperationNumber(), "*")){
                    Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
                    strProcessOperationListForRouteIn.setRouteID(autoDispatchControlUpdateInfo.getRouteID());
                    strProcessOperationListForRouteIn.setOperationID(new ObjectIdentifier(""));
                    strProcessOperationListForRouteIn.setOperationNumber("");
                    strProcessOperationListForRouteIn.setPdType("");
                    strProcessOperationListForRouteIn.setSearchCount(1L);
                    processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
                    for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : tmpLotAutoDispatchControlInfoList) {
                        if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), autoDispatchControlUpdateInfo.getRouteID())
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), autoDispatchControlUpdateInfo.getRouteID())
                                && !CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        }
                    }
                } else if (!ObjectIdentifier.equalsWithValue(autoDispatchControlUpdateInfo.getRouteID(), "*")
                                && !CimStringUtils.equals(autoDispatchControlUpdateInfo.getOperationNumber(), "*")){
                    Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
                    strProcessOperationListForRouteIn.setRouteID(autoDispatchControlUpdateInfo.getRouteID());
                    strProcessOperationListForRouteIn.setOperationID(new ObjectIdentifier(""));
                    strProcessOperationListForRouteIn.setOperationNumber(autoDispatchControlUpdateInfo.getOperationNumber());
                    strProcessOperationListForRouteIn.setPdType("");
                    strProcessOperationListForRouteIn.setSearchCount(1L);
                    processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
                    for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : tmpLotAutoDispatchControlInfoList) {
                        if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), "*")
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), autoDispatchControlUpdateInfo.getRouteID())
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), "*")){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        } else if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), autoDispatchControlUpdateInfo.getRouteID())
                                && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), autoDispatchControlUpdateInfo.getOperationNumber())){
                            throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                        }
                    }
                } else {
                    throw new ServiceException(retCodeConfigEx.getInvalidDispatchControlInformation());
                }
                Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo = new Infos.LotAutoDispatchControlInfo();
                lotAutoDispatchControlInfo.setLotID(lotAutoDispatchControlUpdateInfo.getLotID());
                lotAutoDispatchControlInfo.setRouteID(autoDispatchControlUpdateInfo.getRouteID());
                lotAutoDispatchControlInfo.setOperationNumber(autoDispatchControlUpdateInfo.getOperationNumber());
                lotAutoDispatchControlInfo.setSingleTriggerFlag(autoDispatchControlUpdateInfo.isSingleTriggerFlag());
                lotAutoDispatchControlInfo.setDescription(autoDispatchControlUpdateInfo.getDescription());
                tmpLotAutoDispatchControlInfoList.add(lotAutoDispatchControlInfo);
            }
        }
    }

    @Override
    public void autoDispatchControlInfoUpdate(Infos.ObjCommon objCommon, Inputs.AutoDispatchControlInfoUpdateIn autoDispatchControlInfoUpdateIn) {
        ObjectIdentifier lotID = autoDispatchControlInfoUpdateIn.getLotID();
        Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo = autoDispatchControlInfoUpdateIn.getAutoDispatchControlUpdateInfo();
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        /*-----   Check Input Parameter   -----*/
        if (CimStringUtils.isEmpty(autoDispatchControlUpdateInfo.getUpdateMode())){
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(autoDispatchControlUpdateInfo.getRouteID())){
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        if (CimStringUtils.isEmpty(autoDispatchControlUpdateInfo.getOperationNumber())){
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        ProductDTO.AutoDispatchControlRecord aAutoDispatchControlRecord = new ProductDTO.AutoDispatchControlRecord();
        aAutoDispatchControlRecord.setRouteID(autoDispatchControlUpdateInfo.getRouteID());
        aAutoDispatchControlRecord.setOperationNumber(autoDispatchControlUpdateInfo.getOperationNumber());
        aAutoDispatchControlRecord.setSingleTriggerFlag(autoDispatchControlUpdateInfo.isSingleTriggerFlag());
        aAutoDispatchControlRecord.setDescription(autoDispatchControlUpdateInfo.getDescription());
        aAutoDispatchControlRecord.setUpdateUserID(objCommon.getUser().getUserID());
        aAutoDispatchControlRecord.setUpdateTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_CREATE)){
            aLot.addAutoDispatchControlRecord(aAutoDispatchControlRecord);
        } else if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_UPDATE)){
            aLot.removeAutoDispatchControlRecord(aAutoDispatchControlRecord);
            aLot.addAutoDispatchControlRecord(aAutoDispatchControlRecord);
        } else if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_DELETE)){
            aLot.removeAutoDispatchControlRecord(aAutoDispatchControlRecord);
        } else if (CimStringUtils.equals(autoDispatchControlUpdateInfo.getUpdateMode(), BizConstant.SP_AUTODISPATCHCONTROL_AUTODELETE)){
            if (autoDispatchControlUpdateInfo.isSingleTriggerFlag()){
                aLot.removeAutoDispatchControlRecord(aAutoDispatchControlRecord);
            } else {
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
        } else {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
    }

    @Override
    public List<Infos.LotAutoDispatchControlInfo> autoDispatchControlInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn) {
        /* *****************************/
        /* Convert input parameters   */
        /* *****************************/
        ObjectIdentifier lotID = objAutoDispatchControlInfoGetDRIn.getLotID();
        ObjectIdentifier routeID = objAutoDispatchControlInfoGetDRIn.getRouteID();
        String operationNumber = objAutoDispatchControlInfoGetDRIn.getOperationNumber();
        String wildCardPos;
        String sql = "  SELECT  A.LOT_ID, " +
                "               A.ID, " +
                "               B.MAIN_PROCESS_ID, " +
                "               B.MAIN_PROCESS_RKEY, " +
                "               B.OPE_NO, " +
                "               B.SINGLE_TRIGGER_MODE, " +
                "               B.DESCRIPTION, " +
                "               B.MODIFY_USER_ID, " +
                "               B.MODIFY_USER_RKEY, " +
                "               B.LAST_MODIFY_TIME " +
                "       FROM    OMLOT A, OMLOT_DISPCTRL B " +
                "       WHERE   A.ID = B.REFKEY";
        List<Object> params = new ArrayList<>();
        if (!ObjectIdentifier.isEmptyWithValue(lotID)){
            wildCardPos = BaseStaticMethod.strrchr(lotID.getValue(), "%");
            if (wildCardPos != null){
                sql = sql + " AND B.REFKEY IN ( SELECT ID FROM OMLOT WHERE LOT_ID LIKE ? )";
            } else {
                sql = sql + " AND B.REFKEY IN ( SELECT ID FROM OMLOT WHERE LOT_ID = ? ) ";
            }
            params.add(lotID.getValue());
        }
        if (!ObjectIdentifier.isEmptyWithValue(routeID)){
            wildCardPos = BaseStaticMethod.strrchr(routeID.getValue(), "%");
            if (wildCardPos != null){
                sql = sql + " AND B.MAIN_PROCESS_ID LIKE ?";
            } else {
                sql = sql + " AND B.MAIN_PROCESS_ID = ? ";
            }
            params.add(routeID.getValue());
        }
        if (!CimStringUtils.isEmpty(operationNumber)){
            wildCardPos = BaseStaticMethod.strrchr(operationNumber, "%");
            if (wildCardPos != null){
                sql = sql + " AND B.OPE_NO LIKE ?";
            } else {
                sql = sql  + " AND B.OPE_NO = ? ";
            }
            params.add(operationNumber);
        }
        List<Object[]> queryResult = cimJpaRepository.query(sql, params.toArray());
        List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfoList = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(queryResult)){
            for (Object[] object : queryResult){
                Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo = new Infos.LotAutoDispatchControlInfo();
                lotAutoDispatchControlInfoList.add(lotAutoDispatchControlInfo);
                lotAutoDispatchControlInfo.setLotID(new ObjectIdentifier((String) object[0],(String) object[1]));
                lotAutoDispatchControlInfo.setRouteID(new ObjectIdentifier((String)object[2], (String)object[3]));
                lotAutoDispatchControlInfo.setOperationNumber((String) object[4]);
                lotAutoDispatchControlInfo.setSingleTriggerFlag(CimNumberUtils.intValue((Number) object[5]) > 0);
                lotAutoDispatchControlInfo.setDescription((String) object[6]);
                lotAutoDispatchControlInfo.setUpdateUserID(new ObjectIdentifier((String)object[7],(String)object[8]));
                lotAutoDispatchControlInfo.setUpdateTimeStamp(CimDateUtils.convertToSpecString((Timestamp)object[9]));
            }
        }
        return lotAutoDispatchControlInfoList;
    }
}
