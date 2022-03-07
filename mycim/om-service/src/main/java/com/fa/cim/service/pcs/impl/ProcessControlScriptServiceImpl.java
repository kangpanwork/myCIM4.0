package com.fa.cim.service.pcs.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IScriptMethod;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertySet;
import com.fa.cim.newcore.bo.code.CimScript;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.pcs.engine.CimPcsEngine;
import com.fa.cim.pcs.engine.ScriptParam;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.fa.cim.common.constant.BizConstant.*;

@OmService
@Slf4j
public class ProcessControlScriptServiceImpl implements IProcessControlScriptService {

    @Autowired
    private IScriptMethod scriptMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CimPcsEngine cimPcsEngine;

    @Autowired
    private ILotMethod lotMethod;

    @Override
    public void sxPCSParameterValueSetReq(Infos.ObjCommon objCommon, Params.PCSParameterValueSetReqParams pcsParameterValueSetReqParams) {
        Validations.check(null == objCommon || null == pcsParameterValueSetReqParams, retCodeConfig.getInvalidInputParam());
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        CimPropertySet aPropertySet = null;
        switch (pcsParameterValueSetReqParams.getParameterClass()) {
            case SP_SCRIPTPARM_CLASS_LOT:
                ObjectIdentifier aLotOI = new ObjectIdentifier(pcsParameterValueSetReqParams.getIdentifier());
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, aLotOI);
                Validations.check(aLot == null, retCodeConfig.getNotFoundLot());
                // Lock lot
                objectLockMethod.objectLock(objCommon, CimLot.class, aLotOI);
                aPropertySet = aLot.getPropertySet();
                break;
            case SP_PARVALCLASS_CASSETTE:
                ObjectIdentifier aCastOI = new ObjectIdentifier(pcsParameterValueSetReqParams.getIdentifier());
                CimCassette aCast = baseCoreFactory.getBO(CimCassette.class, aCastOI);
                Validations.check(aCast == null, retCodeConfig.getNotFoundCassette());
                // Lock carrier
                objectLockMethod.objectLock(objCommon, CimCassette.class, aCastOI);
                aPropertySet = aCast.getPropertySet();
                break;
            case SP_PARVALCLASS_RETICLEPOD:
                ObjectIdentifier aReticlePodOI = new ObjectIdentifier(pcsParameterValueSetReqParams.getIdentifier());
                CimReticlePod aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, aReticlePodOI);
                Validations.check(aReticlePod == null, new OmCode(retCodeConfig.getNotFoundReticlePod(), aReticlePodOI.getValue()));
                // Lock ReticlePod
                objectLockMethod.objectLock(objCommon, CimReticlePod.class, aReticlePodOI);
                aPropertySet = aReticlePod.getPropertySet();
                break;
            case SP_PARVALCLASS_RETICLE:
                ObjectIdentifier aReticleOI = new ObjectIdentifier(pcsParameterValueSetReqParams.getIdentifier());
                CimProcessDurable aReticle = baseCoreFactory.getBO(CimProcessDurable.class, aReticleOI);
                Validations.check(aReticle == null, new OmCode(retCodeConfig.getNotFoundReticle(), aReticleOI.getValue()));
                // Lock Reticle
                objectLockMethod.objectLock(objCommon, CimProcessDurable.class, aReticleOI);
                aPropertySet = aReticle.getPropertySet();
                break;
            default:
                log.info("Class Type [ " + pcsParameterValueSetReqParams.getParameterClass() + "] not Lock For Script Parameter Change.");
                break;
        }

        if (null != aPropertySet) {
            lockPropertySet(objCommon, pcsParameterValueSetReqParams.getParameters(), aPropertySet);
        }

        /*------------------------------------------------------------------------*/
        /*   Set UserParameterValue                                               */
        /*------------------------------------------------------------------------*/
        scriptMethod.scriptSetUserParameter(objCommon,
                pcsParameterValueSetReqParams.getParameterClass(),
                pcsParameterValueSetReqParams.getIdentifier(),
                pcsParameterValueSetReqParams.getParameters());

        // script_parameterChangeEvent_Make
        eventMethod.scriptParameterChangeEventMake(objCommon,
                TransactionIDEnum.USER_PARAMETER_VALUE_CHANGE_REQ.getValue(),
                pcsParameterValueSetReqParams.getParameterClass(),
                pcsParameterValueSetReqParams.getIdentifier(),
                pcsParameterValueSetReqParams.getParameters());

    }

    @Override
    public void sxProcessControlScriptRunReq(Infos.ObjCommon objCommon, Params.ProcessControlScriptRunReqParams params) {
        log.info("ProcessControlScriptRunReqService::sxProcessControlScriptRunReq()");
        ObjectIdentifier lotId = params.getLotId();
        if (CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(lotId))) {
            return;
        }
        // Object_lock
        objectLockMethod.objectLock(objCommon, CimLot.class, lotId);

        String lotState = lotMethod.lotStateGet(objCommon, lotId);
        log.info(String.format("【step1】check if the lot state is [%s/%s]",
                CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState));
        if (!CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            return;
        }
        String phase = params.getPhase();
        if (!CimStringUtils.equals(phase, BizConstant.SP_BRSCRIPT_POST)) {
            String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotId);
            log.info(String.format("【step2】check if the lot hold state is [%s/%s]",
                    CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, lotHoldState));
            if (CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                return;
            }
            String lotProcState = lotMethod.lotProcessStateGet(objCommon, lotId);
            log.info(String.format("【step3】check if the lot process state is not [%s/%s]",
                    CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING, lotProcState));
            if (!CimStringUtils.equals(lotProcState, CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING)) {
                return;
            }
        }

        runScript(objCommon, phase, lotId, params.getEquipmentId());
    }

    // *********************************************** Script Method ***********************************************
    private void runScript(Infos.ObjCommon objCommon, String phase, ObjectIdentifier lotId, ObjectIdentifier eqpId) {
        log.info("ScriptMethod::runScript()");
        Validations.check(CimStringUtils.isEmpty(phase), "Input parameter of phase is null.");
        CimProcessOperation aProcessOperation;

        // get current lot info.
        CimLot aLot = productManager.findLotNamed(ObjectIdentifier.fetchValue(lotId));
        // get process operation info.
        if (CimStringUtils.equals(phase, BizConstant.SP_BRSCRIPT_POST)) {
            CimProcessFlowContext aPFX = aLot.getProcessFlowContext();
            Validations.check(null == aPFX, String.format("Cannot found PFX of Lot[%s].", aLot.getIdentifier()));

            aProcessOperation = aLot.isPendingMoveNext() ?
                    aLot.getProcessOperation() : aPFX.getPreviousProcessOperation();
        } else {
            aProcessOperation = aLot.getProcessOperation();
        }
        Validations.check(null == aProcessOperation, String.format("Cannot found Previous PO of Lot[%s].", aLot.getIdentifier()));

        CimScript script = getCimScript(phase, aProcessOperation);

        if (null != script) {
            ScriptParam scriptParam = new ScriptParam();
            scriptParam.setPhase(phase);
            scriptParam.setEquipmentId(eqpId);
            scriptParam.setLotId(lotId);
            scriptParam.setScriptName(script.getIdentifier());
            scriptParam.setScript(script.getIntermediateCode());
            log.info("【step1】call runScript(...)");
            try {
                cimPcsEngine.runScript(objCommon, scriptParam);
            } catch (ServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private CimScript getCimScript(String phase, CimProcessOperation aProcessOperation) {
        switch (phase) {
            case BizConstant.SP_BRSCRIPT_PRE1:
                return aProcessOperation.getPre1Script();
            case BizConstant.SP_BRSCRIPT_PRE2:
                return aProcessOperation.getPre2Script();
            case BizConstant.SP_BRSCRIPT_POST:
                return aProcessOperation.getPostScript();
            default:
                return null;
        }
    }

    private void lockPropertySet(Infos.ObjCommon objCommon, List<Infos.UserParameterValue> parameters, CimPropertySet aPropertySet) {
        // check parameters for actions, if action includes add or delete, lock the propertySet object
        Validations.check(null == parameters || null == aPropertySet, retCodeConfig.getInvalidInputParam());
        for (Infos.UserParameterValue userParameterValue : parameters) {
            if (userParameterValue.getChangeType() == BizConstant.SP_PARVAL_ADD || userParameterValue.getChangeType() == BizConstant.SP_PARVAL_DELETE) {
                ObjectIdentifier aPropertySetOI = new ObjectIdentifier("", aPropertySet.getPrimaryKey());
                objectLockMethod.objectLock(objCommon, CimPropertySet.class, aPropertySetOI);
                break;
            }
        }
    }
}
