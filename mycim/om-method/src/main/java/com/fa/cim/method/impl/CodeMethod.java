package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ICodeMethod;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/18         ********              Nyx             create file
 *
 * @author Nyx
 * @since 2018/7/18 18:33
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
public class CodeMethod  implements ICodeMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public void codeCheckExistanceDR(Infos.ObjCommon objCommon, String categoryID, List<ObjectIdentifier> codeDataIDs) {
        com.fa.cim.newcore.bo.code.CimCategory categoryBO = baseCoreFactory.getBO(com.fa.cim.newcore.bo.code.CimCategory.class, new ObjectIdentifier(categoryID));
        Validations.check(null == categoryBO, retCodeConfig.getNotFoundCategory());
        for (ObjectIdentifier codeID : codeDataIDs) {
            List<?> hvRowCount =  cimJpaRepository.query("SELECT ID from OMCODE WHERE CODETYPE_ID = ?1 AND CODE_ID = ?2", categoryID, ObjectIdentifier.fetchValue(codeID));
            int rowCount = hvRowCount.size();
            Validations.check(rowCount == 0, new OmCode(retCodeConfig.getNotFoundCode(), categoryID, ObjectIdentifier.fetchValue(codeID)));
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn -
     * @param categoryID -
     * @param reasonCodeID -
     * @return com.fa.cim.dto.RetCode<java.util.List<Infos.ReasonCodeAttributes>>
     * @author Ho
     * @since 2019/1/10 14:44:51
     */
    @Override
    public List<Infos.ReasonCodeAttributes> codeReasonDiscriptionGetDR(Infos.ObjCommon strObjCommonIn, String categoryID, ObjectIdentifier reasonCodeID) {
        List<Infos.ReasonCodeAttributes> resultObject = new ArrayList<>();
        if(CimStringUtils.isNotEmpty(categoryID)) {
            String sql = String.format("SELECT CODE_ID, ID, DESCRIPTION FROM OMCODE WHERE CODETYPE_ID = '%s' ", categoryID);
            if(ObjectIdentifier.isNotEmptyWithValue(reasonCodeID)) {
                sql += String.format("AND CODE_ID = '%s' ",reasonCodeID.getValue());
            }
            List<Object[]> objects = cimJpaRepository.query(sql);
            int objectsCount = CimArrayUtils.getSize(objects);
            for(int i=0;i<objectsCount;i++){
                Object[] objs = objects.get(i);
                if(objs == null){
                    continue;
                }
                Infos.ReasonCodeAttributes reasonCodeAttributes=new Infos.ReasonCodeAttributes();
                ObjectIdentifier codeID = new ObjectIdentifier(objs[0].toString(),objs[1].toString());
                reasonCodeAttributes.setReasonCodeID(codeID);
                reasonCodeAttributes.setCodeDescription(objs[2].toString());
                resultObject.add(reasonCodeAttributes);
            }
        }

       return resultObject;
    }

    @Override
    public List<Infos.CodeInfo> codeListGetDR(Infos.ObjCommon objCommon,ObjectIdentifier category) {
        List<Infos.CodeInfo> codeInfosList = new ArrayList<>();
        Validations.check(ObjectIdentifier.isEmpty(category),  retCodeConfig.getNotFoundCategory());
        String categoryID = ObjectIdentifier.fetchValue(category);
        CimCategory categoryBO = baseCoreFactory.getBO(CimCategory.class, category);
        if (null == categoryBO) {
            return codeInfosList;
        }

        List<CimCodeDO> codeList = cimJpaRepository.query("SELECT * FROM OMCODE WHERE CODETYPE_ID = ?1", CimCodeDO.class, categoryID);
        int codeCount = CimArrayUtils.getSize(codeList);
        for (int i = 0; i < codeCount; i++) {
            CimCodeDO code = codeList.get(i);
            Infos.CodeInfo codeInfo = new Infos.CodeInfo();
            codeInfo.setCode(ObjectIdentifier.build(code.getCodeID(), code.getId()));
            codeInfo.setCodeName(code.getCodeName());
            codeInfo.setCategoryID(code.getCategoryID());
            codeInfo.setDescription(code.getDescription());
            codeInfo.setPriorityObj(code.getPropertyObject());
            codeInfosList.add(codeInfo);
        }
        return codeInfosList;
    }

    @Override
    public List<Infos.ReasonCodeAttributes> codeFillInTxPLQ010DR(Infos.ObjCommon objCommon, String category) {
        List<Infos.ReasonCodeAttributes> returnObj = new ArrayList<>();
        if (CimStringUtils.isEmpty(category)) {
            return returnObj;
        }
        List<CimCodeDO> codes = cimJpaRepository.query("SELECT * FROM OMCODE WHERE CODETYPE_ID = ?1", CimCodeDO.class, category);
        if (CimArrayUtils.isEmpty(codes)) {
            return returnObj;
        }
        for (CimCodeDO code : codes) {
            if(CimStringUtils.equals(code.getCategoryID(), BizConstant.SP_REASONCAT_ENTITYINHIBIT)) {
                if(CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCERROR)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCBUILDTIME)) {
                    continue;
                }
            }

            if( CimStringUtils.equals(code.getCategoryID(), BizConstant.SP_REASONCAT_LOTHOLD)) {
                if( CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_LOTLOCK)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_WAITINGFORDATACOLLECTIONHOLD )
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_FPCHOLD )
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_APCERRORHOLD )) {
                    continue;
                }
            }

            //Qiandao OCAP integration add OCAP Release Hold filter
            if( CimStringUtils.equals(code.getCategoryID(), BizConstant.SP_REASONCAT_LOTHOLDRELEASE)) {
                if( CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_LOTLOCKRELEASE )
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_WAITINGDATACOLLECTIONHOLDRELEASE)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.OCAP_HOLD_LOT_RELEASE)) {
                    continue;
                }
            }

            if( CimStringUtils.equals(code.getCategoryID(), BizConstant.SP_REASONCAT_DURABLEHOLD)) {
                if( CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_DURABLELOCK)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_WAITINGFORDATACOLLECTIONHOLD )
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_FPCHOLD)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_APCERRORHOLD))
                {
                    continue;
                }
            }
            if( CimStringUtils.equals(code.getCategoryID(), BizConstant.SP_REASONCAT_DURABLEHOLDRELEASE)) {
                if( CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_DURABLELOCKRELEASE)
                        || CimStringUtils.equals(code.getCodeID(), BizConstant.SP_REASON_WAITINGDATACOLLECTIONHOLDRELEASE)){
                    continue;
                }
            }
            Infos.ReasonCodeAttributes reasonCodeAttributes = new Infos.ReasonCodeAttributes();
            reasonCodeAttributes.setReasonCodeID(ObjectIdentifier.build(code.getCodeID(), code.getId()));
            reasonCodeAttributes.setCodeDescription(code.getDescription());
            returnObj.add(reasonCodeAttributes);
        }
        return returnObj;
    }

    @Override
    public CimCode convertCodeIDToCodeOr(CimCategory category, ObjectIdentifier codeID) {
        CimCode code;
        if (CimObjectUtils.isEmpty(ObjectIdentifier.fetchReferenceKey(codeID))) {
            Validations.check(CimObjectUtils.isEmpty(ObjectIdentifier.fetchValue(codeID)), retCodeConfig.getNotFoundCode(), category.getIdentifier(), "*****");
            code = category.findCodeNamed(ObjectIdentifier.fetchValue(codeID));

        } else {
            code = baseCoreFactory.getBO(CimCode.class, ObjectIdentifier.fetchReferenceKey(codeID));
        }
        Validations.check(CimObjectUtils.isEmpty(code), retCodeConfig.getNotFoundCode(), category.getIdentifier(), codeID);
        return code;
    }

    @Override
    public List<Infos.ReasonCodeAttributes> codeByGroupIdsGet(Infos.ObjCommon objCommon, Params.ReasonCodeQueryParams reasonCodeQueryParams) {
        // 【step 1】 find reason code by group
        String queryReasonCodeSql = "SELECT DISTINCT c.* FROM OMCODE_ACCUSRGRP ca inner JOIN OMCODE c ON ca.REFKEY = c.id " +
                "WHERE c.CODETYPE_ID = ?1 AND ca.USERGRP_RKEY IN (?2) ";

        List<CimCodeDO> codes = cimJpaRepository.query(queryReasonCodeSql, CimCodeDO.class, reasonCodeQueryParams.getCodeCategory(), reasonCodeQueryParams.getGroupIds());

        // 【step 2】 conversion attributes
        return codes.parallelStream().filter(checkCode ->
                !CimStringUtils.equals(BizConstant.OCAP_HOLD_LOT, checkCode.getCodeID()) &&
                !CimStringUtils.equals(BizConstant.OCAP_ADD_MEASURE_HOLD_LOT, checkCode.getCodeID()) &&
                !CimStringUtils.equals(BizConstant.OCAP_RE_MEASURE_HOLD_LOT, checkCode.getCodeID()) &&
                !CimStringUtils.equals(BizConstant.OCAP_HOLD_LOT_RELEASE, checkCode.getCodeID())).map(code -> {
            Infos.ReasonCodeAttributes reasonCodeAttributes = new Infos.ReasonCodeAttributes();
            reasonCodeAttributes.setReasonCodeID(ObjectIdentifier.build(code.getCodeID(), code.getId()));
            reasonCodeAttributes.setCodeDescription(code.getDescription());
            return reasonCodeAttributes;
        }).collect(Collectors.toList());
    }
}
