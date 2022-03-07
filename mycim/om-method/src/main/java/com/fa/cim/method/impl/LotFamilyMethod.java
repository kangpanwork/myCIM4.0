package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotFamilyMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.fa.cim.newcore.standard.prdctmng.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/19        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/19 16:33
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class LotFamilyMethod  implements ILotFamilyMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("ProductManagerCore")
    private ProductManager productManager;



    @Override
    public void lotFamilyDuplicationCheckDR(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID) {
        Validations.check(ObjectIdentifier.isEmptyWithValue(lotFamilyID), retCodeConfig.getNotFoundLotFamily());
        /*--------------------------------------------*/
        /*   Trim suffix number from input family ID  */
        /*--------------------------------------------*/
        String lotFamilyBuf = BaseStaticMethod.strrchr(lotFamilyID.getValue(), ".");
        // bug-http://118.123.246.37:28888/zentao/bug-view-6382.html
        if (lotFamilyBuf!=null){
            lotFamilyBuf=lotFamilyID.getValue().substring(0,lotFamilyID.getValue().length()-lotFamilyBuf.length());
        }
        String hFRLOTFAMILYLOTFAMILY_ID = lotFamilyBuf + "%";
        /*----------------------*/
        /*   Search Lot Family  */
        /*----------------------*/
        String querySql = String.format(" SELECT LOTFAMILY_ID \n" +
                "                  FROM   OMLOTFAMILY\n" +
                "                  WHERE  LOTFAMILY_ID like '%s'", hFRLOTFAMILYLOTFAMILY_ID);
        List<CimLotFamily> cimLotFamilyList = baseCoreFactory.getBOListByCustom(CimLotFamily.class, querySql);
        if (!CimObjectUtils.isEmpty(cimLotFamilyList)){
            for (CimLotFamily cimLotFamily : cimLotFamilyList){
                /*------------------------------------------------*/
                /*   Trim suffix number from lot family records   */
                /*------------------------------------------------*/
                String lotFamilyIDRecord = cimLotFamily.getIdentifier();
                // bug-http://118.123.246.37:28888/zentao/bug-view-6382.html
                lotFamilyIDRecord = lotFamilyIDRecord.substring(0,lotFamilyIDRecord.lastIndexOf('.'));
                Validations.check(CimStringUtils.equals(lotFamilyIDRecord, lotFamilyBuf), new OmCode(retCodeConfig.getDuplicateFamily(), cimLotFamily.getIdentifier()));
            }
        }
    }

    @Override
    public List<Infos.ScrapHistories> lotFamilyFillInTxDFQ003DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        log.info("PPTManager_i::lotFamily_FillInTxDFQ003DR");

        CimLot aLot;
        aLot=baseCoreFactory.getBO(CimLot.class,lotID);

        CimLotFamily aLf;
        CimLotFamily aLotFamily;

        aLf = aLot.getLotFamily();
        aLotFamily = aLf;

        Validations.check(aLotFamily==null,retCodeConfig.getNotFoundLotFamily());

        // Get all scrap wafers. There are scrap wafers in theProductUnitsScrapped.
        List<Product> aWaferseq = null;
        List<Product> aWaferseqVar;

        aWaferseq = aLotFamily.productUnitsScrapped();
        aWaferseqVar = aWaferseq;

        int seqLen = CimArrayUtils.getSize(aWaferseq);
        log.info("aWaferseq->length()={}", seqLen);

        Validations.check(seqLen == 0,retCodeConfig.getNotFoundScrList());

        List<Infos.ScrapHistories> strScrapHistories=new ArrayList<>(seqLen) ;

        CimWafer aWafer;

        for( int i = 0; i < seqLen; i++ ) {
            aWafer = (CimWafer) aWaferseq.get(i);

            strScrapHistories.add(new Infos.ScrapHistories());
            strScrapHistories.get(i).setWaferID(ObjectIdentifier.build(aWafer.getIdentifier(),aWafer.getPrimaryKey()));

            aWafer = productManager.findWaferNamed( strScrapHistories.get(i).getWaferID().getValue() );

            if( aWafer==null ) {
                strScrapHistories.get(i).setLotID(ObjectIdentifier.build("",""));
                strScrapHistories.get(i).setScrappedTimeStamp                ( null );
                continue;
            }

            String id, objRef;
            Lot aWaferLot ;
            Lot aWaferLotTmp ;
            aWaferLotTmp = aWafer.getLot();

            aWaferLot = aWaferLotTmp;

            strScrapHistories.get(i).setLotID(ObjectIdentifier.build(aWaferLot.getIdentifier(),aWaferLot.getPrimaryKey()));

            strScrapHistories.get(i).setScrappedTimeStamp (CimDateUtils.getTimestampAsString(aWafer.getLastClaimedTimeStamp()));
        }

        log.info("PPTManager_i::lotFamily_FillInTxDFQ003DR");
        return strScrapHistories ;
    }

    @Override
    public Outputs.ObjLotFamilySplitNoAdjustOut lotFamilySplitNoAdjust(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID) {
        Outputs.ObjLotFamilySplitNoAdjustOut out = new Outputs.ObjLotFamilySplitNoAdjustOut();
        int suffixNumber = 0;
        /*-------------------------------*/
        /*   Abstract lot Family Suffix  */
        /*-------------------------------*/
        String lotFamilyIDValue = ObjectIdentifier.fetchValue(lotFamilyID);
        int lastIndex = lotFamilyIDValue.lastIndexOf(".");
        String suffix = lotFamilyIDValue.substring(lastIndex + 1);
        // Eric-20201226-BUG-6385
        // BUG-6385: Vendor lot split: Lot split fail,show error'It reached max split count.',but Vendor lot wafer QTY is 25
        /*if (StringUtils.length(suffix) == 2 && StringUtils.equals(suffix.substring(0,2), BizConstant.SP_DEFAULT_LOT_FAMILY_SUFFIX.substring(0,2))){*/
        if (CimStringUtils.length(suffix) >= 2 && !CimStringUtils.compare(suffix, BizConstant.SP_DEFAULT_LOT_FAMILY_SUFFIX, 2)) {              // Eric-20201226-BUG-6385
            log.debug(" ##### lotFamily suffix = {}", suffix);
            /**********************************************************/
            /*   Keep table.                                          */
            /*   If you want to change SplitSuffix generating rule,   */
            /*   You have to change following definition.             */
            /*   If invalid charactor(Ex. 'a0') is given ,            */
            /*   the split number will be initialized by '0'.         */
            /*                                                        */
            /****** Precondition on LotFamily Suffix                  */
            /*  1.Prohibit character is alphabet only                 */
            /*  2.firstFigure is " 0 to 9 + A to Z"                   */
            /*  3.secondFigure is "0 to 9"                            */
            /**********************************************************/
            // Eric-20201226-BUG-6385
            // 0  1  2  3  4  5  6  7  8  9   A   B   C   D   E   F   G   H   I   J   K   L   M   N   O   P   Q   R   S   T   U   V   W   X   Y  Z  //
            // 0  1  2  3  4  5  6  7  8  9  10  11  12  13  14  15  16  17  18  19  20  21  22  23  24  25  26  27  28  29  30  31  32  33  34 35  //
            // Eric-20201226-BUG-6385
            String firstFigureTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            int figureTableLen = CimStringUtils.length(firstFigureTable);
            for (int i = 0; i < figureTableLen; i++) {
                if (firstFigureTable.charAt(i) == suffix.charAt(0)) {
                    String convertedBuf;                                                                  // Eric-20201226-BUG-6385
                    if (CimRegexUtils.matches("[0-9]", String.valueOf(suffix.charAt(1)))) {          // Eric-20201226-BUG-6385
                        convertedBuf = String.format("%d%c", i, suffix.charAt(1));                        // Eric-20201226-BUG-6385
                    } else {
                        convertedBuf = String.format("%d", i);                                            // Eric-20201226-BUG-6385
                    }
                    suffixNumber = Integer.parseInt(convertedBuf);
                    break;
                }
            }
            if (suffixNumber > 0){
                com.fa.cim.newcore.bo.product.CimLotFamily aLotFamily = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLotFamily.class, lotFamilyID);
                Validations.check(aLotFamily == null, new OmCode(retCodeConfig.getNotFoundLotFamily()));
                aLotFamily.setLastSplitNumber(suffixNumber);
            }
        } else {
            log.debug("##### lot family doesn't include '.' or suffix is '.00x'. lotFamilyID = {}", lotFamilyIDValue);
        }
        out.setSplitNumber(suffixNumber);
        return out;
    }

    @Override
    public Results.LotFamilyInqResult lotFamilyFillInTxTRQ007DR(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {

        Results.LotFamilyInqResult lotFamilyInqResult = new Results.LotFamilyInqResult();
        log.info("【Method Entry】lotFamilyFillInTxTRQ007DR");
        List<CimLotDO> lots = cimJpaRepository.query("SELECT A.LOT_ID,\n" +
                "       A.ID,\n" +
                "       A.LOT_STATE,\n" +
                "       A.LOT_HOLD_STATE,\n" +
                "       A.LOT_INV_STATE,\n" +
                "       A.LOT_PROCESS_STATE,\n" +
                "       A.LOT_FINISHED_STATE,\n" +
                "       A.BANK_ID,\n" +
                "       A.BANK_RKEY,\n" +
                "       A.LOT_TYPE,\n" +
                "       A.ORDER_NO,\n" +
                "       A.CUSTOMER_ID,\n" +
                "       A.PROD_ID,\n" +
                "       A.PROD_RKEY,\n" +
                "       A.TRX_TIME,\n" +
                "       A.PLAN_END_TIME,\n" +
                "       A.MAIN_PROCESS_ID,\n" +
                "       A.MAIN_PROCESS_RKEY,\n" +
                "       A.OPE_NO,\n" +
                "       A.QTY,\n" +
                "       A.BANK_IN_REQD,\n" +
                "       A.LOT_PRODUCTION_STATE,\n" +
                "       A.COMPLETION_TIME,\n" +
                "       A.LOT_FAMILY_ID,\n" +
                "       A.LOT_FAMILY_RKEY,\n" +
                "       A.PROD_ORDER_ID,\n" +
                "       A.PROD_ORDER_RKEY,\n" +
                "       A.SUB_LOT_TYPE,\n" +
                "       A.LOT_OWNER_ID,\n" +
                "       A.LOT_OWNER_RKEY\n" +
                "  FROM OMLOT A, OMLOT B\n" +
                " WHERE A.LOT_FAMILY_ID = B.LOT_FAMILY_ID\n" +
                "   AND B.LOT_ID = ?1\n", CimLotDO.class, ObjectIdentifier.fetchValue(lotID));

        List<Infos.LotListAttributes> LotAttributesList = new ArrayList<>();

        int lotsSize = CimArrayUtils.getSize(lots);
        if (lotsSize > 0) {
            for (int i = 0; i < lotsSize; i++) {
                CimLotDO tmpLot = lots.get(i);

                Infos.LotListAttributes lotAttributes = new Infos.LotListAttributes(tmpLot);

                String aStatus = "";

                if (CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK, tmpLot.getLotInventoryState())) {
                    aStatus = tmpLot.getLotInventoryState();
                } else {
                    if (CimStringUtils.equals(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD, tmpLot.getLotHoldState())) {
                        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED, tmpLot.getLotFinishedState()) ||
                                CimStringUtils.equals(CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED, tmpLot.getLotFinishedState())) {
                            aStatus = tmpLot.getLotFinishedState();
                        } else {
                            aStatus = tmpLot.getLotHoldState();
                        }
                    } else {
                        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_LOTCREATED, tmpLot.getLotState()) ||
                                CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_RELEASED, tmpLot.getLotState()) ||
                                CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_SHIPPED, tmpLot.getLotState())) {
                            aStatus = tmpLot.getLotState();
                        } else if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_FINISHED, tmpLot.getLotState())) {
                            aStatus = tmpLot.getLotFinishedState();
                        } else if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, tmpLot.getLotState())) {
                            aStatus = tmpLot.getLotProcessState();
                        }
                    }
                }

                lotAttributes.setLotStatus(aStatus);

                List<Infos.LotStatusList> lotStatusLists = new ArrayList<>();
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_STATE, tmpLot.getLotState()));
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PRODUCTIONSTATE, tmpLot.getLotProductionState()));
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_HOLDSTATE, tmpLot.getLotHoldState()));
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_FINISHEDSTATE, tmpLot.getLotFinishedState()));
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PROCSTATE, tmpLot.getLotProcessState()));
                lotStatusLists.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_INVENTORYSTATE, tmpLot.getLotInventoryState()));
                lotAttributes.setLotStatusList(lotStatusLists);

                LotAttributesList.add(lotAttributes);
            }
        }

        lotFamilyInqResult.setStrLotListAttributes(LotAttributesList);

        log.info("【Method Exit】lotFamilyFillInTxTRQ007DR");

        return lotFamilyInqResult;
    }

    @Override
    public void lotFamilyCheckMerge(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {

        com.fa.cim.newcore.bo.product.CimLot parentLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, parentLotID);
        Validations.check(CimObjectUtils.isEmpty(parentLot),retCodeConfig.getNotFoundLot());

        com.fa.cim.newcore.bo.product.CimLot childLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, childLotID);
        Validations.check(CimObjectUtils.isEmpty(childLot),retCodeConfig.getNotFoundLot());

        com.fa.cim.newcore.bo.product.CimLotFamily aParentLotFamily = parentLot.getLotFamily();
        com.fa.cim.newcore.bo.product.CimLotFamily aChildLotFamily = childLot.getLotFamily();
        if (null == aChildLotFamily || null == aParentLotFamily) {
            throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
        }
        if (!CimStringUtils.equals(aParentLotFamily.getIdentifier(), aChildLotFamily.getIdentifier())) {
            throw new ServiceException(retCodeConfig.getError());
        }
        Lot aOrgLot = aChildLotFamily.originalLot();
        if (null == aOrgLot) {
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }
        if (CimStringUtils.equals(childLot.getIdentifier(), aOrgLot.getIdentifier())) {
            throw new ServiceException(new OmCode(retCodeConfig.getOriglotCannotBeChild(), childLot.getIdentifier()));
        }
    }

    @Override
    public void lotFamilyCheckReworkCancel(Infos.ObjCommon objCommon, ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        log.info("【Method Entry】lotFamilyCheckReworkCancel()");

        com.fa.cim.newcore.bo.product.CimLot aParentLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, parentLotID);
        if (aParentLot == null) {
            log.info("Not found parent lot by {}", parentLotID.getValue());
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }

        com.fa.cim.newcore.bo.product.CimLot aChildLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, childLotID);
        if (aChildLot == null) {
            log.info("Not found child lot by {}", childLotID.getValue());
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }

        com.fa.cim.newcore.bo.product.CimLotFamily parentLotFamily = aParentLot.getLotFamily();
        if (parentLotFamily == null) {
            log.info("【Parent lot】: Not found lot family by {}", parentLotID.getValue());
            throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
        }

        com.fa.cim.newcore.bo.product.CimLotFamily childLotFamily = aChildLot.getLotFamily();
        if (childLotFamily == null) {
            log.info("【Child lot】: Not found lot family by {}", childLotID.getValue());
            throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
        }

        if (!CimStringUtils.equals(parentLotFamily.getIdentifier(), childLotFamily.getIdentifier())) {
            log.info("The lot family id of the parent lot is different from the lot family id of the child lot.");
            throw new ServiceException(retCodeConfig.getLotsNotSameFamily());
        }

        com.fa.cim.newcore.bo.product.CimLot aParentOfChildLot =(com.fa.cim.newcore.bo.product.CimLot) aChildLot.mostRecentlySplitFrom();
        if (aParentOfChildLot == null) {
            log.info("Not found the parent lot of the child lot {}", aChildLot.getIdentifier());
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }

        if (!CimStringUtils.equals(aParentOfChildLot.getIdentifier(), aParentLot.getIdentifier())) {
            log.info("The parent lot id of the child lot is different from the imputed parent lot id.");
            throw new ServiceException(retCodeConfig.getNotNotDirectparent());
        }

        log.info("【Method Exit】lotFamilyCheckReworkCancel()");
    }

    @Override
    public List<ObjectIdentifier> lotFamilyAllLotsGetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID) {
        log.info("lotFamilyID = {}", lotFamilyID);
        if (!ObjectIdentifier.isEmpty(lotFamilyID)) {
            List<CimLotDO> lots = cimJpaRepository.query("SELECT LOT_ID FROM OMLOT WHERE LOT_FAMILY_ID = ?1 AND LOT_FINISHED_STATE != ?2", CimLotDO.class, lotFamilyID.getValue(), BizConstant.SP_LOT_FINISHED_STATE_EMPTIED);
            if (!CimObjectUtils.isEmpty(lots)) {
                return lots.stream().map(x -> new ObjectIdentifier(x.getLotID(), x.getId())).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }


}
