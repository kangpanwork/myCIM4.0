package com.fa.cim.service.equipment;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * @author Yuri
 */
public interface IEquipmentProcessOperation {

    /**
     * normal move out request
     *
     * @param objCommon               objCommon
     * @param opeComWithDataReqParams move out param
     * @return move out result
     * @author Yuri
     */
    Results.MoveOutReqResult sxMoveOutReq(Infos.ObjCommon objCommon, Params.OpeComWithDataReqParams opeComWithDataReqParams);


    /**
     * This function notifies system that Lots' actual Operation has completed on the Equipment that Category ID
     * is Internal Buffer.
     *
     * <p> This function reports to system that Lots in the loaded Carriers have been completed on the Equipment.
     * Operation completion can be claimed by Control Job unit.
     *
     * <p> The phenomenon shown below is performed inside a system.
     * <ui>
     * <li> Processing state of Operation Started Lots is changed from Processing to Waiting.</li>
     * <li> Operation Completed Lots are removed from Equipment's In-Processing Lot information.</li>
     * <li> Control Job is broken up. (with condition)</li>
     * <li> Equipment status is changed from PRD.yyyy to SBY.xxxx. (with condition)</li>
     * <li>...</li>
     * </ui>
     *
     * <p> This function can be used only for the Internal Buffer type Equipment.</p>
     *
     * <p></p>
     *
     * @param objCommon {@link Infos.ObjCommon}
     * @param params    move out for IB params. {@link Params.MoveOutForIBReqParams}
     * @return move out result. {@link Results.MoveOutReqResult}
     * @author zqi
     */
    Results.MoveOutReqResult sxMoveOutForIBReq(Infos.ObjCommon objCommon, Params.MoveOutForIBReqParams params);


    /**
     * This function performs MoveOut forcibly in case the MoveOut cannot be done for some reasons, such as troubles
     * with Equipment, while a Lot is in processing.
     *
     * <p> There are some omitted and added parts from the normal MoveOut function in order to force MoveOut.</p>
     *
     * <p> The omitted functions are: </p>
     * <ui>
     * <li>1. To check whether Cassette Status is "EI" or not, when the current OperationMode is "Off-Line" or "Auto".</li>
     * <li>2. To check whether Equipment Ports and the unloadPort of all cassette information returned from controlJob are the same.</li>
     * <li>3. To check whether the Cassette returned from controlJob and the CassetteID loaded to Equipment are the same.</li>
     * <li>4. Spec Check function in case CompletionMode of Equipment is "AUTO" only.</li>
     * <li>5. Auto-Bank-In function.</li>
     * <li>6. Function to access EAP.</li>
     * </ui>
     *
     * <p> As a prerequisite, Running Hold should be done before ForceOpeComp, so that Lot will not be WIP for the
     * next operation.</p>
     *
     * <p></p>
     *
     * @param objCommon             {@link Infos.ObjCommon}
     * @param equipmentID           equipment id
     * @param controlJobID          control job id
     * @param spcResultRequiredFlag spc result required flag
     * @param claimMemo             claim memo
     * @return force move out result. {@link  Results.ForceMoveOutReqResult}
     * @author zqi
     */
    Results.ForceMoveOutReqResult sxForceMoveOutReq(Infos.ObjCommon objCommon,
                                                    ObjectIdentifier equipmentID,
                                                    ObjectIdentifier controlJobID,
                                                    Boolean spcResultRequiredFlag,
                                                    String claimMemo);

    /**
     * This function performs MoveOut forcibly in case the MoveOut cannot be done for some reasons, such as troubles
     * with Equipment, while a Lot is in processing on InternalBuffer Equipment.
     *
     * <p> There are some omitted and added parts from the normal MoveOut function in order to force MoveOut.</p>
     *
     * <p> The omitted functions are: </p>
     * <ui>
     * <li>1. To check whether Cassette Status is "EI" or not, when the current OperationMode is "Off-Line" or "Auto".</li>
     * <li>2. To check whether Equipment Ports and the unloadPort of all cassette information returned from controlJob are the same.</li>
     * <li>3. To check whether the Cassette returned from controlJob and the CassetteID loaded to Equipment are the same.</li>
     * <li>4. Spec Check function in case CompletionMode of Equipment is "AUTO" only.</li>
     * <li>5. Auto-Bank-In function.</li>
     * <li>6. Function to access EAP.</li>
     * </ui>
     *
     * <p> As a prerequisite, Running Hold should be done before ForceOpeComp, so that Lot will not be WIP for the
     * next operation.</p>
     *
     * <p></p>
     *
     * @param objCommon             {@link Infos.ObjCommon}
     * @param equipmentID           equipment id
     * @param controlJobID          control job id
     * @param spcResultRequiredFlag spc result required flag
     * @param claimMemo             claim memo
     * @return force move out result. {@link  Results.ForceMoveOutReqResult}
     * @author zqi
     */
    Results.ForceMoveOutReqResult sxForceMoveOutForIBReq(Infos.ObjCommon objCommon,
                                                         ObjectIdentifier equipmentID,
                                                         ObjectIdentifier controlJobID,
                                                         Boolean spcResultRequiredFlag,
                                                         String claimMemo);

    /**
     * This function send perform partial operation completion for a controlJob.
     *
     * <p> That is, part of wafers in the controlJob is requested to perform MoveOut and part of wafers is
     * requested to perform MoveInCancel.</p>
     *
     * <p> Following action code is available:</p>
     * <blockquote>
     * <ui>
     * <li>MoveOut</li>
     * <li>MoveInCancel</li>
     * <li>MoveOutWithHold</li>
     * <li>MoveInCancelWithHold</li>
     * </ui>
     * </blockquote>
     *
     * <p></p>
     *
     * @param objCommon               {@link Infos.ObjCommon}
     * @param partialMoveOutReqParams {@link Params.PartialMoveOutReqParams}
     * @param apcIFControlStatus      APCIFControlStatus
     * @param dcsIFControlStatus      DCSIFControlStatus
     * @return partial move out result. {@link Results.PartialMoveOutReqResult}
     * @author zqi
     */
    Results.PartialMoveOutReqResult sxPartialMoveOutReq(Infos.ObjCommon objCommon,
                                                        Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                        String apcIFControlStatus,
                                                        String dcsIFControlStatus);


    /**
     * This function send perform partial operation completion for a controlJob.
     *
     * <p> That is, part of wafers in the controlJob is requested to perform MoveOut and part of wafers is
     * requested to perform MoveInCancel.</p>
     *
     * <p> Following action code is available:</p>
     *
     * <blockquote>
     * <ui>
     * <li>MoveOut</li>
     * <li>MoveInCancel</li>
     * <li>MoveOutWithHold</li>
     * <li>MoveInCancelWithHold</li>
     * </ui>
     * </blockquote>
     * <p></p>
     *
     * @param objCommon               {@link Infos.ObjCommon}
     * @param partialMoveOutReqParams {@link Params.PartialMoveOutReqParams}
     * @param apcIFControlStatus      apcIFControlStatus
     * @param dcsIFControlStatus      dcsIFControlStatus
     * @return partial move out result. {@link Results.PartialMoveOutReqResult}
     * @author zqi
     */
    Results.PartialMoveOutReqResult sxMoveOutWithRunningSplitForIBReq(Infos.ObjCommon objCommon,
                                                                      Params.PartialMoveOutReqParams partialMoveOutReqParams,
                                                                      String apcIFControlStatus,
                                                                      String dcsIFControlStatus);

}
