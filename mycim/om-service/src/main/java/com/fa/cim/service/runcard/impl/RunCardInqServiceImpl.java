package com.fa.cim.service.runcard.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IRunCardMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.runcard.IRunCardInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@OmService
@Slf4j
public class RunCardInqServiceImpl implements IRunCardInqService {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Override
    public Page<List<Infos.RunCardInfo>> sxRunCardListInq(Infos.ObjCommon objCommon, Params.RunCardListInqParams params) {

        List<Infos.RunCardInfo> result = new ArrayList<>();
        String sql = "SELECT ID, " +
                "RUNCARD_ID, " +
                "LOT_ID, " +
                "LOT_RKEY, " +
                "RUNCARD_STATE, " +
                "OWNER_ID, " +
                "OWNER_RKEY, " +
                "EXT_APROVAL_FLAG, " +
                "CREATE_TIME, " +
                "UPDATE_TIME, " +
                "APPROVERS, " +
                "TRX_MEMO, " +
                "AUTO_COMPLETE_FLAG " +
                "FROM RUNCARD ";
        String sqlTemp = "";

        //Check runCardID input param
        log.info("Check runCardID input param");
        if (CimStringUtils.isNotEmpty(params.getRunCardID())){
            log.info("find runCardID: {}",params.getRunCardID());
            sqlTemp = String.format("WHERE RUNCARD_ID = '%s' ",params.getRunCardID());
            sql += sqlTemp;
        }

        //Check lotID input param
        log.info("Check lotID input param");
        if (ObjectIdentifier.isNotEmptyWithValue(params.getLotID())){
            log.info("find lotID: {}", ObjectIdentifier.fetchValue(params.getLotID()));
            if (CimStringUtils.isNotEmpty(sqlTemp)){
                sqlTemp = String.format("AND LOT_ID = '%s' ", ObjectIdentifier.fetchValue(params.getLotID()));
            }else {
                sqlTemp = String.format("WHERE LOT_ID = '%s' ", ObjectIdentifier.fetchValue(params.getLotID()));
            }
            sql += sqlTemp;
        }

        //Check RunCardState input param
        log.info("Check RunCardState input param");
        if (CimStringUtils.isNotEmpty(params.getRunCardState())){
            log.info("find runCardState: {}",params.getRunCardState());
            if (CimStringUtils.isNotEmpty(sqlTemp)){
                sqlTemp = String.format("AND RUNCARD_STATE = '%s' ",params.getRunCardState());
            }else {
                sqlTemp = String.format("WHERE RUNCARD_STATE = '%s' ",params.getRunCardState());
            }
            sql += sqlTemp;
        }

        //order by create_time
        sql += "ORDER BY CREATE_TIME";
        Integer fwpflag = StandardProperties.OM_RUNCARD_EXT_APROVAL_FLAG.getIntValue();
        log.info("RunCard FWP FLAG： {}",fwpflag);

        List<String> userGroupApproverUsers = new ArrayList<>();
        if (0 == fwpflag){
            //check and get RunCard approveUsers
            userGroupApproverUsers = runCardMethod.getApproveUsersFromUserGroupAndFunction(BizConstant.RUNCARD_APPROVAL_USER_GROUP, TransactionIDEnum.RC_APPROVAL_REQ.getValue());
            log.info("RunCard userGroupApproverUsers: {}", CimStringUtils.join(userGroupApproverUsers, BizConstant.SEPARATOR_COMMA));
        }else {
            // TODO: 2020/8/3 get from FWP
        }

        //Query runCard table
        log.info("Query runCard table");
        List<Object[]> runCardList = cimJpaRepository.query(sql);
        if (CimArrayUtils.isNotEmpty(runCardList)){
            for (Object[] runCardData : runCardList) {
                List<String> approveUsers = null;
                Infos.RunCardInfo runCardInfo = new Infos.RunCardInfo();
                runCardInfo.setRunCardID((String)(runCardData[1]));
                runCardInfo.setLotID(ObjectIdentifier.build((String)(runCardData[2]),(String) runCardData[3]));
                runCardInfo.setRunCardState((String)(runCardData[4]));
                runCardInfo.setOwner(ObjectIdentifier.build((String)(runCardData[5]),(String)(runCardData[6])));
                runCardInfo.setExtApprovalFlag(CimBooleanUtils.getBoolean((String.valueOf(runCardData[7]))));
                runCardInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (runCardData[8])));
                runCardInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (runCardData[9])));
                //approve Users convert to List

                runCardInfo.setApprovers(userGroupApproverUsers);
                runCardInfo.setClaimMemo((String)(runCardData[11]));
                runCardInfo.setAutoCompleteFlag(null != runCardData[12] && CimBooleanUtils.getBoolean((String.valueOf(runCardData[12]))));

                log.info("Get runCard PSM/DOC detail info");
                //get RunCard PSM/DOC detail info
                //query RUNCARD_PSM
                String refkey = (String)(runCardData[0]);
                List<Object[]> psmResultList = cimJpaRepository.query("SELECT ID, PSM_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME,PSM_KEY FROM RUNCARD_PSM WHERE REFKEY = ?1 ORDER BY CREATE_TIME", refkey);
                List<Infos.RunCardPsmInfo> psmInfoList = new ArrayList<>();
                runCardInfo.setRunCardPsmDocInfos(psmInfoList);
                if (CimArrayUtils.isNotEmpty(psmResultList)){
                    for (Object[] psmResult : psmResultList) {
                        Infos.RunCardPsmInfo runCardPsmInfo = new Infos.RunCardPsmInfo();
                        psmInfoList.add(runCardPsmInfo);
                        runCardPsmInfo.setPsmJobID((String)(psmResult[1]));
                        runCardPsmInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmResult[2]));
                        runCardPsmInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[3])));
                        runCardPsmInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmResult[4])));
                        runCardPsmInfo.setPsmKey((String)(psmResult[5]));

                        //query RUNCARD_PSM_DOC
                        String psmJobID = (String)(psmResult[1]);
                        List<Infos.RunCardPsmDocInfo> runCardPsmDocInfoList = new ArrayList<>();
                        runCardPsmInfo.setPsmDocInfos(runCardPsmDocInfoList);
                        List<Object[]> psmDocResultList = cimJpaRepository.query("SELECT DOC_JOB_ID, IDX_NO, CREATE_TIME, UPDATE_TIME, PSM_KEY, PSM_JOB_ID FROM RUNCARD_PSM_DOC WHERE PSM_JOB_ID = ?1 ORDER BY CREATE_TIME", psmJobID);
                        if (CimArrayUtils.isNotEmpty(psmDocResultList)){
                            for (Object[] psmDocResult : psmDocResultList) {
                                Infos.RunCardPsmDocInfo runCardPsmDocInfo = new Infos.RunCardPsmDocInfo();
                                runCardPsmDocInfoList.add(runCardPsmDocInfo);
                                runCardPsmDocInfo.setDocJobID((String)(psmDocResult[0]));
                                runCardPsmDocInfo.setSequenceNumber(CimNumberUtils.intValue((Number) psmDocResult[1]));
                                runCardPsmDocInfo.setCreateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[2])));
                                runCardPsmDocInfo.setUpdateTime(CimDateUtils.convertToSpecString((Timestamp) (psmDocResult[3])));
                                runCardPsmDocInfo.setPsmKey((String)(psmDocResult[4]));
                                runCardPsmDocInfo.setPsmJobID((String)psmDocResult[5]);
                            }
                        }
                    }
                }
                result.add(runCardInfo);
            }
        }

        //add page
        Page page = CimPageUtils.convertListToPage(result,
                params.getSearchCondition().getPage(),
                params.getSearchCondition().getSize());

        return page;
    }
}
