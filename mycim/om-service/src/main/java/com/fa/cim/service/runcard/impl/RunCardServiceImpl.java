package com.fa.cim.service.runcard.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.fpc.CimFPCDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IRunCardMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimPlannedSplitJob;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.runcard.IRunCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.List;

@OmService
@Slf4j
public class RunCardServiceImpl implements IRunCardService {
    @Autowired
    private IRunCardMethod runCardMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Override
    public void sxRunCardUpdateReq(Infos.ObjCommon objCommon, Params.RunCardUpdateReqParams params) {
        Infos.RunCardInfo runCardInfo = new Infos.RunCardInfo();

        //Check input param
        //Check lotID input param
        log.info("Check lotID input param");
        if (!ObjectIdentifier.isEmptyWithRefKey(params.getLotID())) {
            log.info("set runCard lotID :{}", params.getLotID().getValue());
            runCardInfo.setLotID(params.getLotID());
        } else {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
            Validations.check(null == aLot, retCodeConfig.getNotFoundLot());
            runCardInfo.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
        }

        //Check lotID exsit runCardID
        log.info("Check lotID: {} exsit runCardID",ObjectIdentifier.fetchValue(runCardInfo.getLotID()));
        Infos.RunCardInfo runCardFromLotID = runCardMethod.getRunCardFromLotID(runCardInfo.getLotID());
        Validations.check(null != runCardFromLotID,retCodeConfigEx.getAlreadyExsitRunCard(), ObjectIdentifier.fetchValue(runCardInfo.getLotID()));

        // bug-1636 Get and Check lotState ,must not be Finished
        String interFabXferState = lotMethod.lotStateGet(objCommon, runCardInfo.getLotID());
        String finishedStateGet = lotMethod.lotFinishedStateGet(objCommon, runCardInfo.getLotID());
        Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, interFabXferState), retCodeConfig.getInvalidLotStat(),finishedStateGet);

        //Check psm/doc exsit
        log.info("Check psm/doc exsit");
        if (CimArrayUtils.isNotEmpty(params.getRunCardPsmDocInfos())) {
            for (Infos.RunCardPsmInfo psmInfo : params.getRunCardPsmDocInfos()) {
                log.info("Set runCard psmJobID: {}", psmInfo.getPsmJobID());
                CimPlannedSplitJob plannedSplitJob = baseCoreFactory.getBO(CimPlannedSplitJob.class, ObjectIdentifier.buildWithValue(psmInfo.getPsmJobID()));
                Validations.check(null == plannedSplitJob, retCodeConfig.getNotFoundExperimentalLotData());

                if (CimArrayUtils.isNotEmpty(psmInfo.getPsmDocInfos())){
                    for (Infos.RunCardPsmDocInfo psmDocInfo : psmInfo.getPsmDocInfos()) {
                        log.info("Set runCard docJobID: {}", psmDocInfo.getDocJobID());
                        CimFPCDO cimFPCExam = new CimFPCDO();
                        cimFPCExam.setFpcID(psmDocInfo.getDocJobID());
                        CimFPCDO fpc = cimJpaRepository.findOne(Example.of(cimFPCExam)).orElse(null);
                        Validations.check(null == fpc, retCodeConfig.getInvalidInputParam());
                    }
                }
            }
        }
        //Check runCardState
        log.info("Check runCardState");
        if (CimStringUtils.isNotEmpty(params.getRunCardState())) {
            log.info("Set runCard state: {}", params.getRunCardState());
            //check runCardState
            if (!CimStringUtils.equals(BizConstant.RUNCARD_DRAFT, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_ONGOING, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_ACTIVE, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_CANCEL, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_MODIFY, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_RUNNING, params.getRunCardState()) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_DONE, params.getRunCardState())) {
                log.info("Set runCard state: {} is invalid",params.getRunCardState());
                Validations.check(true, retCodeConfig.getInvalidInputParam());
            }
            runCardInfo.setRunCardState(params.getRunCardState());
        }
        //Set runCard fwpFlag
        Integer fwpflag = StandardProperties.OM_RUNCARD_EXT_APROVAL_FLAG.getIntValue();
        if (1 ==  fwpflag) {
            log.info("Set runCard fwpFlag: {}", fwpflag);
            runCardInfo.setExtApprovalFlag(true);
        }else {
            runCardInfo.setExtApprovalFlag(false);
        }
        //Check runCard approvedUsers
        log.info("Check runCard approvedUsers");

        //get RunCardListInq approved users
        List<String> runCardListApproveUsers = null;
        if (CimBooleanUtils.isFalse(runCardInfo.getExtApprovalFlag())){
            runCardListApproveUsers = runCardMethod.getApproveUsersFromUserGroupAndFunction(BizConstant.RUNCARD_APPROVAL_USER_GROUP, TransactionIDEnum.RC_APPROVAL_REQ.getValue());
            log.info("Find runCardListApproveUsers: {}", CimStringUtils.join(runCardListApproveUsers, BizConstant.SEPARATOR_COMMA));
        }else {
            // TODO: 2020/6/23 get runCardListApproveUsers from FWP system
        }

        //get runCardInfo by runCardID;
        log.info("Get runCardInfo by runCardID: {}",params.getRunCardID());
        Infos.RunCardInfo runCardData = runCardMethod.getRunCardInfo(objCommon,params.getRunCardID());
        String runCardID = null;
        if (null == runCardData){
            String preId = ObjectIdentifier.fetchValue(runCardInfo.getLotID());
            String suffixId = CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp());
            runCardID = String.format("%s.%s", preId, suffixId);

            runCardInfo.setRunCardID(runCardID);
            runCardInfo.setOwner(objCommon.getUser().getUserID());
            runCardInfo.setApprovers(null);
            runCardInfo.setCreateTime(params.getCreateTime());
        }

        //Update runCard data
        log.info("Update runCard data");
        runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

        //Make runCard history
        log.info("Make runCard history");
        eventMethod.runCardEventMake(objCommon,BizConstant.RUNCARD_ACTION_CREATE,runCardInfo);
    }

    @Override
    public void sxRunCardDeleteReq(Infos.ObjCommon objCommon, List<String> runCardIDs) {
        //Check input param
        log.info("Check input param");
        Validations.check(CimArrayUtils.isEmpty(runCardIDs),retCodeConfig.getInvalidInputParam());

        //Delete runCard by runCardID
        log.info("Delete runCard by runCardID");
        for (String runCardID : runCardIDs) {
            //Get runCard Info
            log.info("Get runCard Info: {}",runCardID);
            Infos.RunCardInfo runCardInfoBack = runCardMethod.getRunCardInfo(objCommon, runCardID);
            Validations.check(null == runCardInfoBack,retCodeConfigEx.getNotFoundRunCard());

            //Check Only runCard owner can delete runCard by itself
            log.info("Check Only runCard owner can delete runCard by itself");
            Validations.check(!ObjectIdentifier.equalsWithValue(objCommon.getUser().getUserID(), runCardInfoBack.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()),runCardID);

            String runCardState = runCardInfoBack.getRunCardState();
            //Check only done/cancel/draft runCardState can be delete
            log.info("Check only done/cancel/draft runCardState can be delete");
            Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_DONE,runCardState) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_CANCEL,runCardState) &&
                    !CimStringUtils.equals(BizConstant.RUNCARD_DRAFT,runCardState),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardState);

            //Remove all runCardInfo by runCardID
            log.info("Remove all runCardInfo by runCardID");
            runCardMethod.removeAllRunCardInfo(objCommon,runCardID,true);

            //Make runcard history
            log.info("Make runcard history");
            eventMethod.runCardEventMake(objCommon,BizConstant.RUNCARD_ACTION_DELETE,runCardInfoBack);
        }
    }

    @Override
    public void sxRunCardApprovalReq(Infos.ObjCommon objCommon, Params.RunCardStateApprovalReqParams params) {
        //Check inputParam
        log.info("Check inputParam");
        String claimMemo = params.getClaimMemo();
        String approvalInstruction = params.getApprovalInstruction();
        List<String> runCardIDs = params.getRunCardIDs();

        //Init approveAction
        log.info("Init approveAction");
        String approveAction = null;

        //Check only runcard approve or reject action can operate
        log.info("Check only runcard approve or reject instruction can operate");
        if (CimStringUtils.isEmpty(approvalInstruction) ||
                (!CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_APPROVE, approvalInstruction) &&
                        !CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_REJECT, approvalInstruction))) {
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }
        Validations.check(CimArrayUtils.isEmpty(runCardIDs),retCodeConfig.getInvalidInputParam());

        for (String runCardID : runCardIDs) {
            //Get runCard Info
            log.info("Get runCard Info: {}",runCardID);
            Infos.RunCardInfo runCardInfo = runCardMethod.getRunCardInfo(objCommon, runCardID);
            Validations.check(null == runCardInfo,retCodeConfigEx.getNotFoundRunCard());

            //Get operate user
            log.info("Get operate user");
            ObjectIdentifier userID = objCommon.getUser().getUserID();
            log.info("RunCard operater: {}",ObjectIdentifier.fetchValue(userID));

            //Check runCard setting psm or doc
            log.info("Check runCard setting psm or doc");
            if (CimArrayUtils.isNotEmpty(runCardInfo.getRunCardPsmDocInfos())){
                Validations.check(CimArrayUtils.isEmpty(runCardInfo.getRunCardPsmDocInfos().get(0).getPsmDocInfos()),retCodeConfigEx.getNoSettingPsmOrDoc(),runCardID);
            }else {
                Validations.check(true,retCodeConfigEx.getNoSettingPsmOrDoc(),runCardID);
            }

            //Fetch runcard instruction
            log.info("Fetch runcard instruction");
            switch (approvalInstruction) {
                case BizConstant.RUNCARD_INSTRUCTTION_APPROVE: {
                    //Fetch aprove the runCard
                    log.info("Fetch approve the runCard: {}", runCardID);

                    if (!CimStringUtils.equals(BizConstant.RUNCARD_ACTIVE, runCardInfo.getRunCardState())) {
                        log.info("RunCard state is : {} need to approval", runCardInfo.getRunCardState());

                        //Check only runCard state ongoing can approve
                        log.info("Check only runCard state onGoing can approve");
                        Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_ONGOING, runCardInfo.getRunCardState()), retCodeConfigEx.getInvalidRunCardState(), runCardID, runCardInfo.getRunCardState());

                        //Get userGroupApproveUsers by runCardGroup
                        log.info("Get userGroupApproveUsers by runCardGroup");
                        List<String> userGroupApproverUsers = runCardMethod.getApproveUsersFromUserGroupAndFunction(BizConstant.RUNCARD_APPROVAL_USER_GROUP, TransactionIDEnum.RC_APPROVAL_REQ.getValue());
                        log.info("RunCard userGroupApproverUsers: {}", CimStringUtils.join(userGroupApproverUsers, BizConstant.SEPARATOR_COMMA));

                        //Update runCard approve users and runCard state
                        log.info("Update runCard approve users and runCard state");
                        userGroupApproverUsers.forEach(users ->{
                            if (userGroupApproverUsers.contains(ObjectIdentifier.fetchValue(userID))){
                                runCardInfo.setRunCardState(BizConstant.RUNCARD_ACTIVE);
                                runCardInfo.setClaimMemo(claimMemo);
                                runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                                runCardInfo.getRunCardPsmDocInfos().clear();
                                runCardMethod.updateRunCardInfo(objCommon, runCardInfo);
                            }else {
                                Validations.check(true,retCodeConfig.getNotAuthFunc());
                            }
                        });
                    } else {
                        log.info("RunCard state is : {} don't need to approval", runCardInfo.getRunCardState());
                        Validations.check(true, retCodeConfigEx.getAlreadyApprovedRunCard(), runCardID);
                    }

                    approveAction = BizConstant.RUNCARD_ACTION_APPROVE;
                    break;
                }
                case BizConstant.RUNCARD_INSTRUCTTION_REJECT: {
                    //Fetch reject the runCard
                    log.info("Fetch reject the runCard: {}", runCardID);

                    if (!CimStringUtils.equals(BizConstant.RUNCARD_ACTIVE, runCardInfo.getRunCardState())) {
                        log.info("RunCard state is : {} need to approval", runCardInfo.getRunCardState());

                        //Check only runCard state onGoing can reject
                        log.info("Check only runCard state onGoing can reject");
                        Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_ONGOING, runCardInfo.getRunCardState()), retCodeConfigEx.getInvalidRunCardState(), runCardID, runCardInfo.getRunCardState());

                        //Get userGroupApproveUsers by runCardGroup
                        log.info("Get userGroupApproveUsers by runCardGroup");
                        List<String> userGroupApproverUsers = runCardMethod.getApproveUsersFromUserGroupAndFunction(BizConstant.RUNCARD_APPROVAL_USER_GROUP,TransactionIDEnum.RC_APPROVAL_REQ.getValue());
                        log.info("RunCard userGroupApproverUsers: {}", CimStringUtils.join(userGroupApproverUsers, BizConstant.SEPARATOR_COMMA));

                        //Update run card approve users and run card state
                        log.info("Update runCard approve users and runCard state");
                        userGroupApproverUsers.forEach(users ->{
                            if (userGroupApproverUsers.contains(ObjectIdentifier.fetchValue(userID))){
                                runCardInfo.setRunCardState(BizConstant.RUNCARD_CANCEL);
                                runCardInfo.setClaimMemo(claimMemo);
                                runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                                runCardInfo.getRunCardPsmDocInfos().clear();
                                runCardMethod.updateRunCardInfo(objCommon, runCardInfo);
                            }else {
                                Validations.check(true,retCodeConfig.getNotAuthFunc());
                            }
                        });
                    } else {
                        log.info("RunCard state is : {} don't need to approval", runCardInfo.getRunCardState());
                        Validations.check(true, retCodeConfigEx.getAlreadyApprovedRunCard(), runCardID);
                    }

                    approveAction = BizConstant.RUNCARD_ACTION_REJECT;
                    break;
                }
            }

            //Make runCard history
            log.info("RunCard approveAction: {}",approveAction);
            log.info("Make runCard history");
            eventMethod.runCardEventMake(objCommon,approveAction,runCardInfo);
        }
    }

    @Override
    public void sxRunCardStateChangeReq(Infos.ObjCommon objCommon, Params.RunCardStateChangeReqParams params) {
        //Check inputParam
        log.info("Check inputParam");
        String claimMemo = params.getClaimMemo();
        String runCardChangeState = params.getRunCardChangeState();
        List<String> runCardIDs = params.getRunCardIDs();
        Boolean autoCompleteFlag = params.getAutoCompleteFlag();

        //Init runCard state change action
        log.info("Init runCard state change action");
        String stateChangeAction = null;

        //Check only runcard submit or abort or modify or execute or complete action can operate
        log.info("Check only runcard submit or abort or modify or execute or complete action can operate");
        if (CimStringUtils.isEmpty(runCardChangeState) ||
                (!CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_SUBMIT, runCardChangeState) &&
                        !CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_ABORT, runCardChangeState) &&
                        !CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_MODIFY, runCardChangeState) &&
                        !CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_EXECUTE, runCardChangeState) &&
                        !CimStringUtils.equals(BizConstant.RUNCARD_INSTRUCTTION_COMPLETE, runCardChangeState))) {
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }
        Validations.check(CimArrayUtils.isEmpty(runCardIDs),retCodeConfig.getInvalidInputParam());

        Integer extApprovalFlag = StandardProperties.OM_RUNCARD_EXT_APROVAL_FLAG.getIntValue();
        log.info("RunCard extApprovalFlag: {}",extApprovalFlag);

        for (String runCardID : runCardIDs) {
            //Get runCard Info
            log.info("Get runCard Info: {}",runCardID);
            Infos.RunCardInfo runCardInfo = runCardMethod.getRunCardInfo(objCommon, runCardID);
            Validations.check(null == runCardInfo,retCodeConfigEx.getNotFoundRunCard());

            //Get operate user
            log.info("Get operate user");
            ObjectIdentifier userID = objCommon.getUser().getUserID();
            log.info("RunCard operater: {}",ObjectIdentifier.fetchValue(userID));

            //Check runCard setting psm or doc when the run card state is not modify
            log.info("Check runCard setting psm or doc when the run card state is not modify");
            if (!CimStringUtils.equals(runCardChangeState,BizConstant.RUNCARD_INSTRUCTTION_MODIFY)){
                if (CimArrayUtils.isNotEmpty(runCardInfo.getRunCardPsmDocInfos())){
                    Validations.check(CimArrayUtils.isEmpty(runCardInfo.getRunCardPsmDocInfos().get(0).getPsmDocInfos()),retCodeConfigEx.getNoSettingPsmOrDoc(),runCardID);
                }else {
                    Validations.check(true,retCodeConfigEx.getNoSettingPsmOrDoc(),runCardID);
                }
            }

            //Fetch runCard changeState
            log.info("Fetch runCard changeState");
            switch (runCardChangeState){
                case BizConstant.RUNCARD_INSTRUCTTION_SUBMIT: {
                    if (extApprovalFlag == 0){
                        log.info("RunCard extApprovalFlag = 0 so call normal submit");
                        log.info("Fetch submit the runCard",runCardID);

                        //Check only owner can operate submit
                        log.info("Check only owner can operate submit");
                        Validations.check(!ObjectIdentifier.equalsWithValue(userID, runCardInfo.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(userID),runCardID);

                        //Check only runCard state DRAFT can submit
                        log.info("Check only runCard state DRAFT can submit");
                        Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_DRAFT,runCardInfo.getRunCardState()),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardInfo.getRunCardState());

                        //Update draft to onGoing and autocompleteflag if set
                        log.info("Update draft to onGoing and autocompleteflag if set");
                        runCardInfo.setAutoCompleteFlag(autoCompleteFlag);
                        runCardInfo.setRunCardState(BizConstant.RUNCARD_ONGOING);
                        runCardInfo.setClaimMemo(claimMemo);
                        runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                        runCardInfo.getRunCardPsmDocInfos().clear();
                        runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

                        stateChangeAction = BizConstant.RUNCARD_ACTION_SUBMIT;
                    }else {
                        // TODO: 2020/6/23 send request to FWP
                        Validations.check(true,retCodeConfigEx.getNoResponseFromFWP());
                    }
                    break;
                }
                case BizConstant.RUNCARD_INSTRUCTTION_ABORT: {
                    //Fetch Abotrt the runCard
                    log.info("Fetch abort the runCard: {}",runCardID);

                    //Check only owner can operate abort
                    log.info("Check only owner can operate abort");
                    Validations.check(!ObjectIdentifier.equalsWithValue(userID, runCardInfo.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(userID),runCardID);

                    //Check only active and running can abort
                    Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_ACTIVE,runCardInfo.getRunCardState())
                            && !CimStringUtils.equals(BizConstant.RUNCARD_RUNNING,runCardInfo.getRunCardState()),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardInfo.getRunCardState());

                    //Delete psm and doc when runCard state is running
                    log.info("Delete psm and doc when runCard state is running");
                    if (CimStringUtils.equals(BizConstant.RUNCARD_RUNNING,runCardInfo.getRunCardState())){
                        runCardMethod.removeAllRunCardInfo(objCommon,runCardID,false);
                    }
                    //Update runCard state to cancel
                    log.info("Update runCard state to cancel");
                    runCardInfo.setRunCardState(BizConstant.RUNCARD_CANCEL);
                    runCardInfo.setClaimMemo(claimMemo);
                    runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    runCardInfo.getRunCardPsmDocInfos().clear();
                    runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

                    stateChangeAction = BizConstant.RUNCARD_ACTION_ABORT;
                    break;
                }
                case BizConstant.RUNCARD_INSTRUCTTION_MODIFY: {
                    //Fetch modify the runCard
                    log.info("Fetch modify the runCard: {}",runCardID);

                    //Check Only owner can operate modify
                    log.info("Check Only owner can operate modify");
                    Validations.check(!ObjectIdentifier.equalsWithValue(userID, runCardInfo.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(userID),runCardID);

                    //Check only runCard state cancel can modify
                    log.info("Check only runCard state cancel can modify");
                    Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_CANCEL,runCardInfo.getRunCardState()),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardInfo.getRunCardState());

                    //Update runCard state to draft
                    log.info("Update runCard state to draft");
                    runCardInfo.setRunCardState(BizConstant.RUNCARD_DRAFT);
                    runCardInfo.setClaimMemo(claimMemo);
                    runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    runCardInfo.getRunCardPsmDocInfos().clear();
                    runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

                    stateChangeAction = BizConstant.RUNCARD_ACTION_MODIFY;
                    break;
                }
                case BizConstant.RUNCARD_INSTRUCTTION_EXECUTE: {
                    //Fetch exec the runCard
                    log.info("Fetch exec the runCard: {}",runCardID);

                    //Check Only owner can operate exec
                    log.info("Check Only owner can operate exec");
                    Validations.check(!ObjectIdentifier.equalsWithValue(userID, runCardInfo.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(userID),runCardID);

                    //Check only runCard state active can exec
                    log.info("Check only runCard state active can exec");
                    Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_ACTIVE,runCardInfo.getRunCardState()),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardInfo.getRunCardState());

                    //Update run card state to running
                    log.info("Update run card state to running");
                    runCardInfo.setRunCardState(BizConstant.RUNCARD_RUNNING);
                    runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    runCardInfo.getRunCardPsmDocInfos().clear();
                    runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

                    stateChangeAction = BizConstant.RUNCARD_ACTION_EXECUTE;
                    break;
                }
                case BizConstant.RUNCARD_INSTRUCTTION_COMPLETE: {
                    //Fetch complete the runCard
                    log.info("Fetch complete the runCard: {}",runCardID);

                    //Check only owner can operate exec
                    log.info("heck only owner can operate exec");
                    Validations.check(!ObjectIdentifier.equalsWithValue(userID, runCardInfo.getOwner()),retCodeConfigEx.getNoAuthOperateRunCard(), ObjectIdentifier.fetchValue(userID),runCardID);

                    //Check only runCard state running can compelete
                    log.info("Check only runCard state running can compelete");
                    Validations.check(!CimStringUtils.equals(BizConstant.RUNCARD_RUNNING,runCardInfo.getRunCardState()),retCodeConfigEx.getInvalidRunCardState(),runCardID,runCardInfo.getRunCardState());

                    //Remove psm/doc info when complete the runCard
                    log.info("Remove psm/doc info when complete the runCard");
                    runCardMethod.removeAllRunCardInfo(objCommon,runCardID,false);

                    //Update run card state to done
                    log.info("Update run card state to done");
                    runCardInfo.setRunCardState(BizConstant.RUNCARD_DONE);
                    runCardInfo.setUpdateTime(CimDateUtils.getCurrentDateTimeWithDefault());
                    runCardInfo.getRunCardPsmDocInfos().clear();
                    runCardMethod.updateRunCardInfo(objCommon,runCardInfo);

                    stateChangeAction = BizConstant.RUNCARD_ACTION_EXECUTE;
                    break;
                }
            }

            //Make runCard histtory
            log.info("RunCard stateChangeAction: {}",stateChangeAction);
            log.info("Make runCard histtory");
            eventMethod.runCardEventMake(objCommon,stateChangeAction,runCardInfo);
        }
    }
}
