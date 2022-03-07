package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.edc.IEdcMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PostProcessTaskHandler
public class EDCSpecCheckExecutor implements PostProcessExecutor {

    @Autowired
    private final IEdcMethod edcMethod;

    @Autowired
    public EDCSpecCheckExecutor(IEdcMethod edcMethod) {
        this.edcMethod = edcMethod;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        ObjectIdentifier lotID = param.getEntityID();
        ObjectIdentifier controlJobID = param.getControlJobID();

        Infos.StartRecipe startRecipe = edcMethod.lotStartRecipeInfoGet(objCommon, lotID, controlJobID);
        if(CimBooleanUtils.isTrue(startRecipe.getDataCollectionFlag())) {
            startRecipe.getDcDefList().stream()
                    .flatMap(dataCollectionInfo -> dataCollectionInfo.getDcItems().stream())
                    .filter(itemInfo ->
                            // return true if the measurement type is not byPJ
                            !CimStringUtils.equalsIn(itemInfo.getMeasurementType(),
                                    BizConstant.SP_DCDEF_MEAS_PJ,
                                    BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                    BizConstant.SP_DCDEF_MEAS_PJWAFERSITE) ||
                                    // otherwise check the spec check result
                                    // return true if the spec check result is already empty
                                    // or the spec check result is not with error
                                    !(CimStringUtils.isNotEmpty(itemInfo.getSpecCheckResult()) &&
                                            CimStringUtils.equalsIn(itemInfo.getSpecCheckResult(),
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_APCERROR,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_POUND,
                                                    BizConstant.SP_SPECCHECKRESULT_ERROR)))
                    .peek(itemInfo -> {
                        /*===== Clear specCheckResult / actionCode =====*/
                        if (log.isDebugEnabled()) {
                            log.debug("Clear specCheckResult / actionCode...");
                        }
                        itemInfo.setSpecCheckResult("");
                        itemInfo.setActionCodes("");

                        /*===== Clear dataValue =====*/
                        if (log.isDebugEnabled()) {
                            log.debug("Clear dataValue...");
                        }
                    })
                    .filter(itemInfo -> {
                        boolean isRaw = !CimStringUtils.equals(itemInfo.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW);
                        if (log.isDebugEnabled() && isRaw) {
                            log.debug("Raw Item. continue...");
                        }
                        return isRaw;
                    })
                    .forEach(itemInfo -> itemInfo.setDataValue(""));

            /*----------------------------------*/
            /*   Set Initialized Data into PO   */
            /*----------------------------------*/
            edcMethod.edcTempDataSet(objCommon, lotID, controlJobID, startRecipe.getDcDefList());


        }

        return PostProcessTask.success();
    }
}
