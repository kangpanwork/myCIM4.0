package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBankForDurableMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.factory.CimBank;
import com.fa.cim.newcore.bo.pd.CimDurableProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static com.fa.cim.common.constant.BizConstant.*;

/**
 * description:
 * <p>BankForDurableMethod .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/6/17/017   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/6/17/017 15:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class BankForDurableMethod implements IBankForDurableMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    RetCodeConfigEx retCodeConfigEx;

    @Autowired
    RetCodeConfig retCodeConfig;

    @Autowired
    BaseCoreFactory baseCoreFactory;

    @Override
    public void bankCheckStartBankForDurableRouteDR(Infos.ObjCommon objCommonIn, ObjectIdentifier bankID) {

        String sql = "SELECT COUNT(ID)\n" +
                "     FROM OMPRP\n" +
                "     WHERE START_BANK_ID = ?1 \n" +
                "     AND PRP_LEVEL        = 'Main'\n" +
                "     AND PRF_TYPE       = 'Main'\n" +
                "     AND PRP_TYPE         = 'Durable' ";
        long count = cimJpaRepository.count(sql, bankID.getValue());
        if (count == 0) {
            log.info("count is 0");
            throw new ServiceException(new OmCode(retCodeConfigEx.getNotDurableStartbank(), bankID.getValue()));
        }
    }

    @Override
    public void durableBankIn(Infos.ObjCommon objCommonIn, Boolean onRouteFlag, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier bankID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommonIn.getUser().getUserID());
        CimBank aBank;
        CimProcessDefinition aMainProcessDefinition;
        CimDurableProcessOperation aDurablePO;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            if (!onRouteFlag) {
                log.info("onRouteFlag is FALSE");
                aBank = baseCoreFactory.getBO(CimBank.class, bankID);
            } else {
                log.info("onRouteFlag is TRUE");
                aDurablePO = aCassette.getDurableProcessOperation();
                Validations.check(CimObjectUtils.isEmpty(aDurablePO), new OmCode(retCodeConfigEx.getNotFoundDurablepo(), durableID.getValue()));
                aMainProcessDefinition = aDurablePO.getMainProcessDefinition();
                Validations.check(CimObjectUtils.isEmpty(aMainProcessDefinition), new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                aBank = aMainProcessDefinition.getEndBank();
            }
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            boolean isBankInBank = aBank.isBankInBank();
            Validations.check(!isBankInBank, retCodeConfig.getInvalidBankType());
            aCassette.setBank(aBank);
            if (onRouteFlag) {
                log.info("onRouteFlag is TRUE");
                try {
                    aCassette.makeProcessing();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSING));
                }
                try {
                    aCassette.makeProcessed();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSED));
                }
                try {
                    aCassette.makeCompleted();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED));
                }
            }
            try {
                aCassette.makeInBank();
            } catch (Exception e) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK));
            }
            aCassette.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aCassette.setLastClaimedPerson(aPerson);
            aCassette.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aCassette.setStateChangedPerson(aPerson);
            aCassette.makeNotBankInRequired();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            if (!onRouteFlag) {
                log.info("onRouteFlag is FALSE");
                aBank = baseCoreFactory.getBO(CimBank.class, bankID);
            } else {
                log.info("onRouteFlag is TRUE");
                aDurablePO = aReticlePod.getDurableProcessOperation();
                Validations.check(CimObjectUtils.isEmpty(aDurablePO), new OmCode(retCodeConfigEx.getNotFoundDurablepo(), durableID.getValue()));
                aMainProcessDefinition = aDurablePO.getMainProcessDefinition();
                Validations.check(CimObjectUtils.isEmpty(aMainProcessDefinition), new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                aBank = aMainProcessDefinition.getEndBank();
            }
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            boolean isBankInBank = aBank.isBankInBank();
            Validations.check(!isBankInBank, retCodeConfig.getInvalidBankType());
            aReticlePod.setBank(aBank);
            if (onRouteFlag) {
                log.info("onRouteFlag is TRUE");
                try {
                    aReticlePod.makeProcessing();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSING));
                }
                try {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSED));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    aReticlePod.makeCompleted();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED));
                }
            }
            try {
                aReticlePod.makeInBank();
            } catch (Exception e) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK));
            }
            aReticlePod.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
            aReticlePod.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticlePod.setStateChangedPerson(aPerson);
            aReticlePod.makeNotBankInRequired();
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is Reticle");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            if (!onRouteFlag) {
                log.info("onRouteFlag is FALSE");
                aBank = baseCoreFactory.getBO(CimBank.class, bankID);
            } else {
                log.info("onRouteFlag is TRUE");
                aDurablePO = aReticle.getDurableProcessOperation();
                Validations.check(CimObjectUtils.isEmpty(aDurablePO), new OmCode(retCodeConfigEx.getNotFoundDurablepo(), durableID.getValue()));
                aMainProcessDefinition = aDurablePO.getMainProcessDefinition();
                Validations.check(CimObjectUtils.isEmpty(aMainProcessDefinition), new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                aBank = aMainProcessDefinition.getEndBank();
            }
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            boolean isBankInBank = aBank.isBankInBank();
            Validations.check(!isBankInBank, retCodeConfig.getInvalidBankType());
            aReticle.setBank(aBank);
            if (onRouteFlag) {
                log.info("onRouteFlag is TRUE");
                try {
                    aReticle.makeProcessing();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSING));
                }
                try {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_PROCSTATE_PROCESSED));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    aReticle.makeCompleted();
                } catch (Exception e) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED));
                }
            }
            try {
                aReticle.makeInBank();
            } catch (Exception e) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidStateTrans(), "****", BizConstant.SP_DURABLE_INVENTORYSTATE_INBANK));
            }
            aReticle.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
            aReticle.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticle.setStateChangedPerson(aPerson);
            aReticle.makeNotBankInRequired();
        }
    }

    @Override
    public void durableBankInCancel(Infos.ObjCommon objCommonIn, String durableCategory, ObjectIdentifier durableID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommonIn.getUser().getUserID());
        CimBank aBank;
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            aCassette.setAllStateForBankInCancel();
            aBank = aCassette.getBank();
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            aCassette.setBank(null);
            aCassette.setPreviousBank(aBank);
            aCassette.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aCassette.setLastClaimedPerson(aPerson);
            aCassette.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aCassette.setStateChangedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            aReticlePod.setAllStateForBankInCancel();
            aBank = aReticlePod.getBank();
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            aReticlePod.setBank(null);
            aReticlePod.setPreviousBank(aBank);
            aReticlePod.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
            aReticlePod.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticlePod.setStateChangedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is ReticlePod");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            aReticle.setAllStateForBankInCancel();
            aBank = aReticle.getBank();
            Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
            aReticle.setBank(null);
            aReticle.setPreviousBank(aBank);
            aReticle.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
            aReticle.setStateChangedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticle.setStateChangedPerson(aPerson);
        }
    }

    @Override
    public void durableBankMove(Infos.ObjCommon objCommonIn, String durableCategory, ObjectIdentifier durableID, ObjectIdentifier bankID) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommonIn.getUser().getUserID());
        CimBank aBank, previousBank;
        aBank = baseCoreFactory.getBO(CimBank.class, bankID);
        Validations.check(CimObjectUtils.isEmpty(aBank), new OmCode(retCodeConfig.getNotFoundBank(), ""));
        if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_CASSETTE)) {
            log.info("durableCategory is Cassette");
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, durableID);
            previousBank = aCassette.getBank();
            aCassette.setBank(aBank);
            aCassette.setPreviousBank(previousBank);
            aCassette.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aCassette.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLEPOD)) {
            log.info("durableCategory is ReticlePod");
            CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
            previousBank = aReticlePod.getBank();
            aReticlePod.setBank(aBank);
            aReticlePod.setPreviousBank(previousBank);
            aReticlePod.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticlePod.setLastClaimedPerson(aPerson);
        } else if (CimStringUtils.equals(durableCategory, SP_DURABLECAT_RETICLE)) {
            log.info("durableCategory is ReticlePod");
            CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
            previousBank = aReticle.getBank();
            aReticle.setBank(aBank);
            aReticle.setPreviousBank(previousBank);
            aReticle.setLastClaimedTimeStamp(objCommonIn.getTimeStamp().getReportTimeStamp());
            aReticle.setLastClaimedPerson(aPerson);
        }
    }

    @Override
    public void bankDurableSTBCheck(Infos.ObjCommon objCommon, ObjectIdentifier routeID, String durableCategory, List<ObjectIdentifier> durables) {
        Validations.check(null == objCommon
                || null == routeID
                || null == durableCategory
                || CimArrayUtils.isEmpty(durables), retCodeConfig.getInvalidInputParam());

        CimProcessDefinition mainPD = baseCoreFactory.getBO(CimProcessDefinition.class, routeID);
        Validations.check(null == mainPD, retCodeConfig.getNotFoundProcessDefinition());

        //-----------------------------------------------------
        // Get and check route's pd type
        //-----------------------------------------------------
        String pdType = mainPD.getProcessDefinitionType();
        if (!CimStringUtils.equals(pdType, BizConstant.SP_MAINPDTYPE_DURABLE)) {
            log.error("ProcessDefinitionType is not DURABLE");
            Validations.check(retCodeConfig.getInvalidRouteType());
        }

        //-----------------------------------------------------
        // get object reference of Start Bank.
        //-----------------------------------------------------
        CimBank aStartBank = mainPD.getStartBank();
        Validations.check(null == aStartBank, retCodeConfig.getNotFoundBank());

        Boolean bSTBBankFlag = aStartBank.isSTBBank();
        Validations.check(!bSTBBankFlag, retCodeConfigEx.getNotDurableStartbank(), aStartBank.getIdentifier());

        Optional.of(durables).ifPresent(list -> list.forEach(durableID -> {
            CimBank aBank = null;
            switch (durableCategory) {
                case SP_DURABLECAT_CASSETTE:
                    CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, durableID);
                    Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
                    aBank = cassette.getBank();
                    break;
                case SP_DURABLECAT_RETICLEPOD:
                    CimReticlePod reticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID);
                    Validations.check(null == reticlePod, retCodeConfig.getNotFoundReticlePod());
                    aBank = reticlePod.getBank();
                    break;
                case SP_DURABLECAT_RETICLE:
                    CimProcessDurable reticle = baseCoreFactory.getBO(CimProcessDurable.class, durableID);
                    Validations.check(null == reticle, retCodeConfig.getNotFoundReticle());
                    aBank = reticle.getBank();
                    break;
                default:
                    // doting
                    break;
            }
            Validations.check(null == aBank, retCodeConfig.getNotFoundBank());
            if (!CimStringUtils.equals(aStartBank.getIdentifier(), aBank.getIdentifier())) {
                Validations.check(retCodeConfigEx.getNotMatchStartBankForDurable(), durableID.getValue());
            }
        }));
    }
}
