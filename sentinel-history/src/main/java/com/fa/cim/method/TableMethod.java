package com.fa.cim.method;

import com.fa.cim.Custom.ArrayList;
import com.fa.cim.Custom.List;
import com.fa.cim.core.BaseCore;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.utils.ArrayUtils;
import com.fa.cim.utils.BaseUtils;
import com.fa.cim.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.convert;
import static com.fa.cim.utils.StringUtils.sprintf;
import static com.fa.cim.utils.StringUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 13:21:52
 */
@Slf4j
@Repository
public class TableMethod {

    @Autowired
    private BaseCore baseCore;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID
     * @param castCategory
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/26 17:09:00
     */
    public Response getFRCAST(String cassetteID, String castCategory) {
        Response response=new Response();
        String buff;

        String hFRCAST_CASSETTE_ID,
            hFRCAST_CAST_CATEGORY;

        String castCategoryStr="";
        hFRCAST_CASSETTE_ID="";
        hFRCAST_CAST_CATEGORY="";

        hFRCAST_CASSETTE_ID=cassetteID;

        List<Object> args=new ArrayList<>();

        args.add(hFRCAST_CASSETTE_ID);
        buff="SELECT  CARRIER_CATEGORY,CARRIER_ID \n"+
            "                     FROM OMCARRIER\n"+
            "                     Where CARRIER_ID = ?";

        Object[] object = baseCore.queryOne(buff, args.toArray());

        hFRCAST_CAST_CATEGORY= ArrayUtils.get(object,0);

        castCategoryStr=hFRCAST_CAST_CATEGORY;
        response.setBody(castCategoryStr);

        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param cassetteID
     * @param castCategory
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:38
     */
    public Response getFRCAST(String cassetteID, Params.String castCategory) {
        Response response=new Response();
        String buff;

        String hFRCAST_CASSETTE_ID,
            hFRCAST_CAST_CATEGORY;

        hFRCAST_CASSETTE_ID="";
        hFRCAST_CAST_CATEGORY="";

        hFRCAST_CASSETTE_ID=cassetteID;

        List<Object> args=new ArrayList<>();

        args.add(hFRCAST_CASSETTE_ID);
        buff="SELECT  CARRIER_CATEGORY,CARRIER_ID \n"+
            "                     FROM OMCARRIER\n"+
            "                     Where CARRIER_ID = ?";

        Object[] object = baseCore.queryOne(buff, args.toArray());

        hFRCAST_CAST_CATEGORY= ArrayUtils.get(object,0);

        castCategory.setValue(hFRCAST_CAST_CATEGORY);
        response.setBody(castCategory);

        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param waferID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/11 14:55
     */
    public Response getFRWAFER(String waferID, Infos.Frwafer resultData ) {
        String hFRWAFER_WAFER_ID          ;
        String hFRWAFER_PRODSPEC_ID       ;
        Integer hFRWAFER_BAD_DICE_QTY;
        Integer hFRWAFER_GOOD_DICE_QTY;
        Integer hFRWAFER_REPAIRED_DICE_QTY;
        String hFRWAFER_ALIAS_WAFER_NAME  ;

        hFRWAFER_WAFER_ID="";
        hFRWAFER_PRODSPEC_ID="";
        hFRWAFER_BAD_DICE_QTY        = 0;
        hFRWAFER_GOOD_DICE_QTY       = 0;
        hFRWAFER_REPAIRED_DICE_QTY  = 0;
        hFRWAFER_ALIAS_WAFER_NAME="";

        hFRWAFER_WAFER_ID = waferID;

        Object[] one = baseCore.queryOne("SELECT  PROD_ID, BAD_DICE_QTY, GOOD_DICE_QTY,\n" +
                "            REPAIRED_DICE_QTY, ALIAS_WAFER_NAME\n" +
                "        FROM OMWAFER\n" +
                "        Where WAFER_ID = ?", hFRWAFER_WAFER_ID);

        if (ArrayUtils.length(one)!=0){
            hFRWAFER_PRODSPEC_ID=convert(one[0]);
            hFRWAFER_BAD_DICE_QTY=convertI(one[1]);
            hFRWAFER_GOOD_DICE_QTY=convertI(one[2]);
            hFRWAFER_REPAIRED_DICE_QTY=convertI(one[3]);
            hFRWAFER_ALIAS_WAFER_NAME=convert(one[4]);
        }

        resultData.setProductID(hFRWAFER_PRODSPEC_ID);
        resultData.setBadDiceQty       (hFRWAFER_BAD_DICE_QTY);
        resultData.setGoodDiceQty      (hFRWAFER_GOOD_DICE_QTY);
        resultData.setRepairedDiceQty  (hFRWAFER_REPAIRED_DICE_QTY);
        resultData.setAlias_wafer_name(hFRWAFER_ALIAS_WAFER_NAME);
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param customerID
     * @param prodSpecID
     * @param custProdID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 10:23
     */
    public Response getFRCUSTPROD2(String customerID, String prodSpecID, Params.String custProdID ) {
        String hFRCUSTPROD_CUSTOMERID  ;
        String hFRCUSTPROD_PRODSPEC_ID2;
        String hFRCUSTPROD_CUSTPROD_ID2;


        custProdID.setValue("");
        hFRCUSTPROD_CUSTOMERID="";
        hFRCUSTPROD_PRODSPEC_ID2="";
        hFRCUSTPROD_CUSTPROD_ID2="";

        hFRCUSTPROD_CUSTOMERID = customerID ;
        hFRCUSTPROD_PRODSPEC_ID2 = prodSpecID ;

        Object CUSTPROD_ID=baseCore.queryAll("SELECT  CUST_PROD_ID \n" +
                "        FROM OMCUSTPROD\n" +
                "        Where CUST_PROD_ID = ?\n" +
                "        AND   PROD_ID = ?",hFRCUSTPROD_CUSTOMERID
                , hFRCUSTPROD_PRODSPEC_ID2);


        hFRCUSTPROD_CUSTPROD_ID2=convert(CUSTPROD_ID);


        custProdID .setValue(hFRCUSTPROD_CUSTPROD_ID2);

        Response iRc=BaseUtils.returnOK();

        return(iRc);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/24 15:29
     */
    public Response getFRPRODREQ(String lotID, Infos.Frprodreq resultData ) {
        String hFRPRODREQ_LOT_ID           ;
        String hFRPRODREQ_SUB_LOT_TYPE     ;
        String hFRPRODREQ_CUSTOMER_ID      ;
        String hFRPRODREQ_DELIVERY_TIME    ;
        String hFRPRODREQ_LOT_COMMENT      ;
        String hFRPRODREQ_LOT_GEN_MODE     ;
        String hFRPRODREQ_LOT_GEN_TYPE     ;
        String hFRPRODREQ_LOT_OWNER_ID     ;
        String hFRPRODREQ_MFG_LAYER        ;
        String hFRPRODREQ_ORDER_NO         ;
        String hFRPRODREQ_PLAN_RELEASE_TIME;
        String hFRPRODREQ_PRODUCT_MODE     ;
        String hFRPRODREQ_SCHEDULE_MODE    ;
        String hFRPRODREQ_START_BANK_ID    ;
        Integer hFRPRODREQ_PRIORITY_CLASS;
        Integer hFRPRODREQ_SCHEDULE_PRIORITY;

        hFRPRODREQ_LOT_ID="";
        hFRPRODREQ_SUB_LOT_TYPE="";
        hFRPRODREQ_CUSTOMER_ID="";
        hFRPRODREQ_DELIVERY_TIME="";
        hFRPRODREQ_LOT_COMMENT="";
        hFRPRODREQ_LOT_GEN_MODE="";
        hFRPRODREQ_LOT_GEN_TYPE="";
        hFRPRODREQ_LOT_OWNER_ID="";
        hFRPRODREQ_MFG_LAYER="";
        hFRPRODREQ_ORDER_NO="";
        hFRPRODREQ_PLAN_RELEASE_TIME="";
        hFRPRODREQ_PRODUCT_MODE="";
        hFRPRODREQ_SCHEDULE_MODE="";
        hFRPRODREQ_START_BANK_ID="";
        hFRPRODREQ_PRIORITY_CLASS = 0;
        hFRPRODREQ_SCHEDULE_PRIORITY = 0;

        hFRPRODREQ_LOT_ID = lotID ;

        String sql="SELECT PRDREQ.SUB_LOT_TYPE,      PRDREQ.CUSTOMER_ID,       PRDREQ.CUST_DELIVER_DATE,\n" +
                "            PRDREQ.PO_COMMENT,       PRDREQ.LOT_ID_CREATE_MODE,      PRDREQ.RELEASE_TYPE,\n" +
                "            PRDREQ.LOT_OWNER_ID,      PRDREQ.MFG_LAYER,         PRDREQ.MFG_ORDER_NO,\n" +
                "            PRDREQ.PLAN_RELEASE_DATE, PRDREQ.LOT_PRIORITY,    PRDREQ.PROD_DEF_MODE,\n" +
                "            PRDREQ.SCHEDULE_TYPE,     '', PRDREQ.START_BANK_ID\n" +
                "        FROM OMPRORDER      PRDREQ,\n" +
                "            OMLOT          LOT\n" +
                "        Where LOT.LOT_ID         = ?\n" +
                "        AND   PRDREQ.ID = LOT.PROD_RKEY";

        Object[] one=baseCore.queryOne(sql,hFRPRODREQ_LOT_ID);

        if (one==null) {
            one=baseCore.queryOne("SELECT SUB_LOT_TYPE,  CUSTOMER_ID,       CUST_DELIVER_DATE,  PO_COMMENT,\n" +
                    "                    LOT_ID_CREATE_MODE,  RELEASE_TYPE,      LOT_OWNER_ID,   MFG_LAYER,\n" +
                    "                    MFG_ORDER_NO,      PLAN_RELEASE_DATE, LOT_PRIORITY, PROD_DEF_MODE,\n" +
                    "                    SCHEDULE_TYPE, '', START_BANK_ID\n" +
                    "                FROM OMPRORDER\n" +
                    "                Where PROD_ORDER_ID = ? ",hFRPRODREQ_LOT_ID);
        }

        if (one==null) {
            return returnOK();
        }

        hFRPRODREQ_SUB_LOT_TYPE=convert(one[0]);
                hFRPRODREQ_CUSTOMER_ID=convert(one[1]);
        hFRPRODREQ_DELIVERY_TIME=convert(one[2]);
                hFRPRODREQ_LOT_COMMENT=convert(one[3]);
        hFRPRODREQ_LOT_GEN_MODE=convert(one[4]);
                hFRPRODREQ_LOT_GEN_TYPE=convert(one[5]);
        hFRPRODREQ_LOT_OWNER_ID=convert(one[6]);
                hFRPRODREQ_MFG_LAYER=convert(one[7]);
        hFRPRODREQ_ORDER_NO=convert(one[8]);
                hFRPRODREQ_PLAN_RELEASE_TIME=convert(one[9]);
        hFRPRODREQ_PRIORITY_CLASS=convertI(one[10]);
                hFRPRODREQ_PRODUCT_MODE=convert(one[11]);
        hFRPRODREQ_SCHEDULE_MODE=convert(one[12]);
                hFRPRODREQ_SCHEDULE_PRIORITY=convertI(one[13]);
        hFRPRODREQ_START_BANK_ID=convert(one[14]);


        resultData.setSubLotType(hFRPRODREQ_SUB_LOT_TYPE);
        resultData.setCustomerID(hFRPRODREQ_CUSTOMER_ID);
        resultData.setDeliveryTime(hFRPRODREQ_DELIVERY_TIME);
        resultData.setLotComment(hFRPRODREQ_LOT_COMMENT);
        resultData.setLotGenMode(hFRPRODREQ_LOT_GEN_MODE);
        resultData.setLotGenType(hFRPRODREQ_LOT_GEN_TYPE);
        resultData.setLotOwner(hFRPRODREQ_LOT_OWNER_ID);
        resultData.setMfgLayer(hFRPRODREQ_MFG_LAYER);
        resultData.setOrderNO(hFRPRODREQ_ORDER_NO);
        resultData.setPlanReleaseTime(hFRPRODREQ_PLAN_RELEASE_TIME);
        resultData.setPriorityClass (hFRPRODREQ_PRIORITY_CLASS);
        resultData.setProductMode(hFRPRODREQ_PRODUCT_MODE);
        resultData.setScheduleMode(hFRPRODREQ_SCHEDULE_MODE);
        resultData.setSchedulePriority(hFRPRODREQ_SCHEDULE_PRIORITY);
        resultData.setStartBankID(hFRPRODREQ_START_BANK_ID);

        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param operationID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 10:31
     */
    public Response getFRPD2(String operationID, Infos.Frpd resultData) {
        String    hFRPD2_PD_ID        ;
        String    hFRPD2_PD_TYPE      ;
        String    hFRPD2_OPE_NAME     ;
        String    hFRPD2_PD_LEVEL     ;

        hFRPD2_PD_ID="";
        hFRPD2_PD_TYPE="";
        hFRPD2_OPE_NAME="";
        hFRPD2_PD_LEVEL="";

        hFRPD2_PD_ID = operationID ;
        hFRPD2_PD_LEVEL = "Operation";


        Object[] one=baseCore.queryOne("SELECT   OPE_NAME, PRP_TYPE\n" +
                "        FROM  OMPRP\n" +
                "        Where PRP_ID = ? And PRP_LEVEL = ? ",hFRPD2_PD_ID
                , hFRPD2_PD_LEVEL );

        hFRPD2_OPE_NAME = convert(one[0]);
                hFRPD2_PD_TYPE = convert(one[1]);

        resultData.setOperationName(hFRPD2_OPE_NAME);
        resultData.setPd_type(hFRPD2_PD_TYPE);
        return(BaseUtils.returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param calendarDATE
     * @param shopDATE
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 10:27
     */
    public Response getFRCALENDAR(String calendarDATE, Timestamp shopDATE) {
        String hFRCALENDAR_DATE;
        Double  hFRCALENDAR_SHOP_DATE;


        hFRCALENDAR_DATE="";
        hFRCALENDAR_SHOP_DATE = 0.0;

        hFRCALENDAR_DATE = calendarDATE==null?null: BaseUtils.convert(calendarDATE)
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Object[] one=baseCore.queryOne("SELECT  WORK_DATE,CALENDAR_DATE \n" +
                "        FROM OMCALDR\n" +
                "        Where CALENDAR_DATE = ?",hFRCALENDAR_DATE);
        if (one==null){
            return returnOK();
        }
        hFRCALENDAR_SHOP_DATE=convertD(one[0]);



        shopDATE.setTime(hFRCALENDAR_SHOP_DATE.longValue());

        return(BaseUtils.returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param poObj
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/13 10:25
     */
    public Response getFRPO2(String poObj, Infos.Frpo resultData) {
        String hFRPO2_PO_OBJ         ;
        String hFRPO2_PD_ID           ;
        String hFRPO2_ASGN_EQP_ID     ;
        String hFRPO2_ASGN_LCRECIPE_ID;
        String hFRPO2_ASGN_RECIPE_ID  ;
        String hFRPO2_ASGN_PHRECIPE_ID;
        String hFRPO2_CTRLJOB_ID      ;
        Timestamp hFRPO2_ACTUAL_END_TIME ;

        hFRPO2_PO_OBJ="";
        hFRPO2_PD_ID="";
        hFRPO2_ASGN_EQP_ID="";
        hFRPO2_ASGN_LCRECIPE_ID="";
        hFRPO2_ASGN_RECIPE_ID="";
        hFRPO2_ASGN_PHRECIPE_ID="";
        hFRPO2_CTRLJOB_ID="";
        hFRPO2_ACTUAL_END_TIME=null;
        hFRPO2_PO_OBJ =poObj ;

        Object[] one=baseCore.queryOne("SELECT   STEP_ID,                   ALLOC_EQP_ID,         ALLOC_LRCP_ID,         ALLOC_MRCP_ID,\n" +
                "                ALLOC_PRCP_ID,        CJ_ID,          ACTUAL_MOVOUT_TIME\n" +
                "        FROM  OMPROPE\n" +
                "        WHERE ID = ?",hFRPO2_PO_OBJ);

        hFRPO2_PD_ID = BaseUtils.convert(one[0]);
                hFRPO2_ASGN_EQP_ID = BaseUtils.convert(one[1]);
        hFRPO2_ASGN_LCRECIPE_ID = BaseUtils.convert(one[2]);
                hFRPO2_ASGN_RECIPE_ID = BaseUtils.convert(one[3]);
        hFRPO2_ASGN_PHRECIPE_ID = BaseUtils.convert(one[4]);
                hFRPO2_CTRLJOB_ID = BaseUtils.convert(one[5]);
        hFRPO2_ACTUAL_END_TIME = BaseUtils.convert(one[6]);


        resultData.setPd_id(hFRPO2_PD_ID);
        resultData.setAsgn_eqp_id(hFRPO2_ASGN_EQP_ID);
        resultData.setAsgn_lcrecipe_id(hFRPO2_ASGN_LCRECIPE_ID);
        resultData.setAsgn_recipe_id(hFRPO2_ASGN_RECIPE_ID);
        resultData.setAsgn_phrecipe_id(hFRPO2_ASGN_PHRECIPE_ID);
        resultData.setCtrljob_id(hFRPO2_CTRLJOB_ID);
        resultData.setActual_end_time(convert(hFRPO2_ACTUAL_END_TIME));

        return(BaseUtils.returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param operationID
     * @param resultData_pd
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 10:19:11
     */
    public Response getFRPD(String operationID, Infos.Frpd resultData_pd) {
        Response response=new Response();
        String buff;

        String hFRPD_D_THESYSTEMKEY,
            hFRPD_PD_ID         ,
            hFRPD_PD_TYPE       ,
            hFRPD_DEPARTMENT    ,
            hFRPD_OPE_NAME      ,
            hFRPD_START_BANK_ID ,
            hFRPD_PD_LEVEL      ;

        buff=String.format("  in para operationID = [%s]",operationID);

        Infos.Frpd resultData = resultData_pd;

        hFRPD_PD_ID="";
        hFRPD_PD_TYPE="";
        hFRPD_DEPARTMENT="";
        hFRPD_OPE_NAME="";
        hFRPD_START_BANK_ID="";
        hFRPD_PD_LEVEL="";

        hFRPD_PD_ID=operationID;

        hFRPD_PD_LEVEL="Operation";

        buff="";

        buff="SELECT ID, DEPT, OPE_NAME,\n"+
                "                            START_BANK_ID, PRP_TYPE\n"+
                "                     FROM OMPRP\n"+
                "                     Where PRP_ID = ? AND\n"+
                "                           PRP_LEVEL = ?";

        Object[] object = baseCore.queryOne(buff,hFRPD_PD_ID, hFRPD_PD_LEVEL);

        hFRPD_D_THESYSTEMKEY= ArrayUtils.get(object,0);
        hFRPD_DEPARTMENT=ArrayUtils.get(object,1);
        hFRPD_OPE_NAME= ArrayUtils.get(object,2);
        hFRPD_START_BANK_ID=ArrayUtils.get(object,3);
        hFRPD_PD_TYPE=ArrayUtils.get(object,4);

        resultData.setDepartment(hFRPD_DEPARTMENT);
        resultData.setOperationName(hFRPD_OPE_NAME);
        resultData.setPd_type(hFRPD_PD_TYPE);
        resultData.setStartBankID(hFRPD_START_BANK_ID);

        response.setBody(resultData);
        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productID
     * @param prodGrpID
     * @param prodType
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 10:26:58
     */
    public Response getFRPRODSPEC(String productID, String prodGrpID, String prodType) {
        Response response=new Response();
        String buff;

        String hFRPRODSPEC_PRODSPEC_ID2,
                hFRPRODSPEC_PRODGRP_ID,
                hFRPRODSPEC_PROD_TYPE;

        String prodGrpIDStr="";
        String prodTypeStr="";
        hFRPRODSPEC_PRODSPEC_ID2="";
        hFRPRODSPEC_PRODGRP_ID="";
        hFRPRODSPEC_PROD_TYPE="";

        hFRPRODSPEC_PRODSPEC_ID2=productID;

        buff="SELECT  PRODFMLY_ID, PROD_TYPE\n" +
                "        FROM    OMPRODINFO\n" +
                "        Where   PROD_ID = ?";
        List<Object> args=new ArrayList<>();
        args.add(hFRPRODSPEC_PRODSPEC_ID2);

        Object[] object=baseCore.queryOne(buff,args.toArray());
        hFRPRODSPEC_PRODGRP_ID=ArrayUtils.get(object,0);
        hFRPRODSPEC_PROD_TYPE=ArrayUtils.get(object,1);

        prodGrpIDStr=hFRPRODSPEC_PRODGRP_ID;
        prodTypeStr=hFRPRODSPEC_PROD_TYPE;

        response.setBody(new Object[]{prodGrpIDStr,prodTypeStr});
        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productID
     * @param prodGrpID
     * @param prodType
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:31
     */
    public Response getFRPRODSPEC(String productID, Params.String prodGrpID, Params.String prodType) {
        Response response=new Response();
        String buff;

        String hFRPRODSPEC_PRODSPEC_ID2,
                hFRPRODSPEC_PRODGRP_ID,
                hFRPRODSPEC_PROD_TYPE;

        hFRPRODSPEC_PRODSPEC_ID2="";
        hFRPRODSPEC_PRODGRP_ID="";
        hFRPRODSPEC_PROD_TYPE="";

        hFRPRODSPEC_PRODSPEC_ID2=productID;

        buff="SELECT  PRODFMLY_ID, PROD_TYPE\n" +
                "        FROM    OMPRODINFO\n" +
                "        Where   PROD_ID = ?";
        List<Object> args=new ArrayList<>();
        args.add(hFRPRODSPEC_PRODSPEC_ID2);

        Object[] object=baseCore.queryOne(buff,args.toArray());
        hFRPRODSPEC_PRODGRP_ID=ArrayUtils.get(object,0);
        hFRPRODSPEC_PROD_TYPE=ArrayUtils.get(object,1);

        prodGrpID.setValue(hFRPRODSPEC_PRODGRP_ID);
        prodType.setValue(hFRPRODSPEC_PROD_TYPE);

        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 10:46:08
     */
    public Response getFRLOT(String lotID, Infos.Frlot resultData) {
        Response response=new Response();
        String buff="";
        String hFRLOT_LOT_ID      ="" ,
                hFRLOT_CUSTOMER_ID=""  ,
                hFRLOT_PRODSPEC_ID=""  ,
                hFRLOT_LOT_OWNER  =""  ,
                hFRLOT_MFG_LAYER  =""  ,
                hFRLOT_ORDER_NO   =""  ,
                hFRLOT_PLAN_END_TIME="";

        Integer hFRLOT_PRIORITY,hFRLOT_PRIORITY_CLASS;
        String hFRLOT_PRODREQ_OBJ ="";
        String hFRLOT_SUB_LOT_TYPE="";
        String hFRLOT_VENDOR_NAME ="";
        String hFRLOT_LOT_TYPE    ="";

        resultData.setPlanEndTime("1901-01-01-00.00.00.000000");

        hFRLOT_PRIORITY = 0;
        hFRLOT_PRIORITY_CLASS = 0;

        hFRLOT_LOT_ID=lotID;

        buff="SELECT   CUSTOMER_ID,           PROD_ID,           LOT_OWNER_ID,        MFG_LAYER,\n" +
                "                MFG_ORDER_NO,              PLAN_END_TIME,         PROD_ORDER_RKEY,\n" +
                "                LOT_PRIORITY,        SUB_LOT_TYPE,          VENDOR_NAME,         LOT_TYPE\n" +
                "        FROM OMLOT\n" +
                "        Where LOT_ID = ?";

        List<Object> args=new ArrayList<>(hFRLOT_LOT_ID);

        Object[] object = baseCore.queryOne(buff, args.toArray());

        int i = 0;
        hFRLOT_CUSTOMER_ID=ArrayUtils.get(object,i++);
        hFRLOT_PRODSPEC_ID=ArrayUtils.get(object,i++);
        hFRLOT_LOT_OWNER=ArrayUtils.get(object,i++);
        hFRLOT_MFG_LAYER=ArrayUtils.get(object,i++);
        hFRLOT_ORDER_NO=ArrayUtils.get(object,i++);
        hFRLOT_PLAN_END_TIME=convert(ArrayUtils.get(object,i++));
        hFRLOT_PRODREQ_OBJ=ArrayUtils.get(object,i++);
//        hFRLOT_PRIORITY= BaseUtils.convertI(ArrayUtils.get(object,i++));
        hFRLOT_PRIORITY_CLASS=BaseUtils.convertI(ArrayUtils.get(object,i++));
        hFRLOT_SUB_LOT_TYPE=ArrayUtils.get(object,i++);
        hFRLOT_VENDOR_NAME=ArrayUtils.get(object,i++);
        hFRLOT_LOT_TYPE=ArrayUtils.get(object,i);

        resultData.setCustomerID(hFRLOT_CUSTOMER_ID);
        resultData.setProdspec_id(hFRLOT_PRODSPEC_ID);
        resultData.setLotOwner(hFRLOT_LOT_OWNER);
        resultData.setMfgLayer(hFRLOT_MFG_LAYER);
        resultData.setOrderNO(hFRLOT_ORDER_NO);
        resultData.setPlanEndTime(hFRLOT_PLAN_END_TIME);
        resultData.setProdReqObj(hFRLOT_PRODREQ_OBJ);
        resultData.setSubLotType(hFRLOT_SUB_LOT_TYPE);
        resultData.setVendor_name(hFRLOT_VENDOR_NAME);
        resultData.setLotType(hFRLOT_LOT_TYPE);

        resultData.setPriority(hFRLOT_PRIORITY);
        resultData.setPriorityClass(hFRLOT_PRIORITY_CLASS);

        response.setCode(0);
        response.setBody(resultData);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productID
     * @param techID
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 13:01:10
     */
    public Response getFRPRODGRP(String productID, String techID) {
        Response response=new Response();
        String buff;

        String hFRPRODSPEC_PRODSPEC_ID,
            hFRPRODGRP_TECH_ID;

        String techIDStr="";

        hFRPRODSPEC_PRODSPEC_ID="";
        hFRPRODGRP_TECH_ID="";

        hFRPRODSPEC_PRODSPEC_ID=productID;

        buff="SELECT  PRDGRP.TECH_ID,PRDSPC.PROD_ID \n" +
                "        FROM OMPRODFMLY      PRDGRP,\n" +
                "                OMPRODINFO     PRDSPC\n" +
                "        WHERE PRDSPC.PROD_ID = ?\n" +
                "        AND   PRDGRP.PRODFMLY_ID  = PRDSPC.PRODFMLY_ID";

        Object[] object=baseCore.queryOne(buff,hFRPRODSPEC_PRODSPEC_ID);
        hFRPRODGRP_TECH_ID=ArrayUtils.get(object,0);

        techIDStr=hFRPRODGRP_TECH_ID;
        response.setCode(0);
        response.setBody(techIDStr);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param productID
     * @param techID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:36
     */
    public Response getFRPRODGRP(String productID, Params.String techID) {
        Response response=new Response();
        String buff;

        String hFRPRODSPEC_PRODSPEC_ID,
            hFRPRODGRP_TECH_ID;

        hFRPRODSPEC_PRODSPEC_ID="";
        hFRPRODGRP_TECH_ID="";

        hFRPRODSPEC_PRODSPEC_ID=productID;

        buff="SELECT  PRDGRP.TECH_ID,PRDSPC.PROD_ID \n" +
                "        FROM OMPRODFMLY PRDGRP,\n" +
                "             OMPRODINFO PRDSPC\n" +
                "        WHERE PRDSPC.PROD_ID = ? \n" +
                "        AND   PRDGRP.PRODFMLY_ID  = PRDSPC.PRODFMLY_ID";

        Object[] object=baseCore.queryOne(buff,hFRPRODSPEC_PRODSPEC_ID);
        hFRPRODGRP_TECH_ID=ArrayUtils.get(object,0);

        techID.setValue(hFRPRODGRP_TECH_ID);
        response.setCode(0);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param bankID
     * @param stockerID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/31 17:07
     */
    public Response getFRBANK(String bankID, Params.String stockerID) {
        String hFRBANK_BANK_ID = null;

        hFRBANK_BANK_ID="";
        String hFRBANK_STK_ID="";

        hFRBANK_BANK_ID= bankID ;

        hFRBANK_STK_ID= ArrayUtils.get(baseCore.queryOne("SELECT  STOCKER_ID,BANK_ID \n" +
                "        FROM OMBANK\n" +
                "        Where BANK_ID = ?",hFRBANK_BANK_ID),0);

        stockerID.setValue(hFRBANK_STK_ID);
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param stockerID
     * @param areaID
     * @param description
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/31 17:12
     */
    public Response getFRSTK(Params.String stockerID, Params.String areaID, Params.String description ) {
        String hFRSTK_STK_ID     ;
        String hFRSTK_AREA_ID    ;
        String hFRSTK_DESCRIPTION;

        hFRSTK_STK_ID = "";
        hFRSTK_AREA_ID = "";
        hFRSTK_DESCRIPTION = "";

        hFRSTK_STK_ID = stockerID .getValue();

        Object[] ones=baseCore.queryOne("SELECT  BAY_ID, DESCRIPTION \n" +
                "        FROM OMSTOCKER\n" +
                "        WHERE STOCKER_ID = ?",hFRSTK_STK_ID);
        hFRSTK_AREA_ID=ArrayUtils.get(ones,0);
        hFRSTK_DESCRIPTION=ArrayUtils.get(ones,1);


        areaID.setValue(hFRSTK_AREA_ID);
        description.setValue(hFRSTK_DESCRIPTION);
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param stockerID
     * @param areaID
     * @param description
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/27 13:32
     */
    public Response getFRSTK(String stockerID, Params.String areaID, Params.String description ) {
        Params.String stkID=new Params.String();
        stkID.setValue(stockerID);
        return getFRSTK(stkID,areaID,description);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param prodSpecID
     * @param custProdID
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 13:03:43
     */
    public Response getFRCUSTPROD(String lotID, String prodSpecID, String custProdID) {
        Response response=new Response();
        String buff;
        String hFRCUSTPROD_LOT_ID     ;
        String hFRCUSTPROD_PRODSPEC_ID;
        String hFRCUSTPROD_CUSTPROD_ID;

        String custProdIDStr="";
        hFRCUSTPROD_LOT_ID="";
        hFRCUSTPROD_PRODSPEC_ID="";
        hFRCUSTPROD_CUSTPROD_ID="";

        hFRCUSTPROD_LOT_ID=lotID;
        hFRCUSTPROD_PRODSPEC_ID=prodSpecID;

        buff="SELECT  OMCUSTPROD.CUST_PROD_ID,OMLOT.LOT_ID \n" +
                "        FROM OMCUSTPROD,\n" +
                "                OMLOT\n" +
                "        Where OMLOT.LOT_ID            = ?\n" +
                "        AND   OMCUSTPROD.CUSTOMER_RKEY = OMLOT.CUSTOMER_RKEY\n" +
                "        AND   OMCUSTPROD.PROD_ID  = ?";

        Object[] object=baseCore.queryOne(buff,hFRCUSTPROD_LOT_ID,hFRCUSTPROD_PRODSPEC_ID);
        hFRCUSTPROD_CUSTPROD_ID=ArrayUtils.get(object,0);

        custProdIDStr=hFRCUSTPROD_CUSTPROD_ID;

        response.setCode(0);
        response.setBody(custProdIDStr);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param prodSpecID
     * @param custProdID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:42
     */
    public Response getFRCUSTPROD(String lotID, String prodSpecID, Params.String custProdID) {
        Response response=new Response();
        String buff;
        String hFRCUSTPROD_LOT_ID     ;
        String hFRCUSTPROD_PRODSPEC_ID;
        String hFRCUSTPROD_CUSTPROD_ID;

        hFRCUSTPROD_LOT_ID="";
        hFRCUSTPROD_PRODSPEC_ID="";
        hFRCUSTPROD_CUSTPROD_ID="";

        hFRCUSTPROD_LOT_ID=lotID;
        hFRCUSTPROD_PRODSPEC_ID=prodSpecID;

        buff="SELECT  OMCUSTPROD.CUST_PROD_ID,OMLOT.LOT_ID \n" +
                "        FROM OMCUSTPROD,\n" +
                "                OMLOT\n" +
                "        Where OMLOT.LOT_ID            = ?\n" +
                "        AND   OMCUSTPROD.CUSTOMER_RKEY = OMLOT.CUSTOMER_RKEY\n" +
                "        AND   OMCUSTPROD.PROD_ID  = ?";

        Object[] object=baseCore.queryOne(buff,hFRCUSTPROD_LOT_ID,hFRCUSTPROD_PRODSPEC_ID);
        hFRCUSTPROD_CUSTPROD_ID=ArrayUtils.get(object,0);

        custProdID.setValue(hFRCUSTPROD_CUSTPROD_ID);

        response.setCode(0);
        response.setBody(custProdID);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param posObj
     * @param operationNo
     * @param objrefMainPF
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 13:10:57
     */
    public Response getFRPOS(String posObj, String operationNo, String objrefMainPF, Infos.Frpos resultData) {
        Response response=new Response();
        String buff;
        String hFRPOS_POS_OBJ            ;
        String hFRPOS_PHOTO_LAYER        ;
        String hFRPF_D_THE_SYSKEY_POS    ;
        String hFRPF_PF_OBJ_POS          ;
        String hFRPF_PDLIST_MODULE_NO_POS;
        String hFRPF_PDLIST_STAGE_ID_POS ;

        hFRPOS_POS_OBJ="";
        hFRPOS_PHOTO_LAYER="";
        hFRPF_D_THE_SYSKEY_POS="";
        hFRPF_PF_OBJ_POS="";
        hFRPF_PDLIST_MODULE_NO_POS="";
        hFRPF_PDLIST_STAGE_ID_POS="";

        hFRPOS_POS_OBJ=posObj;
        hFRPF_PF_OBJ_POS=objrefMainPF;

        String tempOperationNo="";

        tempOperationNo=operationNo;

        String token = strtok( tempOperationNo, "\\." );

        hFRPF_PDLIST_MODULE_NO_POS=token;

        buff="SELECT  PHOTO_LAYER,ID\n" +
                "        FROM OMPRSS\n" +
                "        Where ID = ?";

        Object[] object=baseCore.queryOne(buff,hFRPOS_POS_OBJ);

        hFRPOS_PHOTO_LAYER=ArrayUtils.get(object,0);

        resultData.setPhotoLayer(hFRPOS_PHOTO_LAYER);


        buff="SELECT   ID, ID AS TID \n" +
                "        FROM  OMPRF\n" +
                "        WHERE ID = ?";

        object=baseCore.queryOne(buff,hFRPF_PF_OBJ_POS);
        hFRPF_D_THE_SYSKEY_POS=ArrayUtils.get(object,0);

        buff="SELECT     STAGE_ID,REFKEY \n" +
                "        FROM    OMPRF_ROUTESEQ\n" +
                "        WHERE   REFKEY = ?\n" +
                "        AND     ROUTE_NO      = ?";
        object=baseCore.queryOne(buff,hFRPF_D_THE_SYSKEY_POS,hFRPF_PDLIST_MODULE_NO_POS);

        hFRPF_PDLIST_STAGE_ID_POS=ArrayUtils.get(object,0);

        resultData.setStageID(hFRPF_PDLIST_STAGE_ID_POS);

        response.setCode(0);
        response.setBody(resultData);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param stageID
     * @param stageGrpIDParam
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 14:08:57
     */
    public Response getFRSTAGE(String stageID, Params.Param stageGrpIDParam) {
        Response response=new Response();
        String buff;
        String hFRSTAGE_STAGE_ID   ;
        String hFRSTAGE_STAGEGRP_ID;

        String stageGrpID="";
        hFRSTAGE_STAGE_ID="";
        hFRSTAGE_STAGEGRP_ID="";

        hFRSTAGE_STAGE_ID=stageID;

        buff="SELECT  STAGE_GRP_ID,STAGE_ID \n" +
                "        FROM OMSTAGE\n" +
                "        Where STAGE_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFRSTAGE_STAGE_ID);
        hFRSTAGE_STAGEGRP_ID=ArrayUtils.get(object,0);

        stageGrpID=hFRSTAGE_STAGEGRP_ID;

        response.setCode(0);
        stageGrpIDParam.setT(stageGrpID);
        response.setBody(stageGrpID);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param stageID
     * @param stageGrpIDParam
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:50
     */
    public Response getFRSTAGE(String stageID, Params.String stageGrpIDParam) {
        Response response=new Response();
        String buff;
        String hFRSTAGE_STAGE_ID   ;
        String hFRSTAGE_STAGEGRP_ID;

        String stageGrpID="";
        hFRSTAGE_STAGE_ID="";
        hFRSTAGE_STAGEGRP_ID="";

        hFRSTAGE_STAGE_ID=stageID;

        buff="SELECT  STAGE_GRP_ID,STAGE_ID \n" +
                "        FROM OMSTAGE\n" +
                "        Where STAGE_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFRSTAGE_STAGE_ID);
        hFRSTAGE_STAGEGRP_ID=ArrayUtils.get(object,0);

        stageGrpID=hFRSTAGE_STAGEGRP_ID;

        response.setCode(0);
        stageGrpIDParam.setValue(stageGrpID);
        response.setBody(stageGrpID);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param routeID
     * @param operationNo
     * @param photoLayer
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/2 15:59
     */
    public Response getFRPOS2(String routeID, String operationNo, Params.String photoLayer ) {
        String hFRPF_PF_OBJ_POS2       ;
        String hFRPF_MAINPD_ID_POS2    ;
        String hFRPOS_OPE_NO_POS2      ;
        String hFRPOS_PHOTO_LAYER_POS2 ;

        hFRPF_PF_OBJ_POS2 = "";
        hFRPF_MAINPD_ID_POS2 = "";
        hFRPOS_OPE_NO_POS2 = "";
        hFRPOS_PHOTO_LAYER_POS2 = "";

        hFRPF_MAINPD_ID_POS2 = routeID ;
        hFRPOS_OPE_NO_POS2 = operationNo ;

        Object[] one = baseCore.queryOne("SELECT  ID,PRP_ID\n" +
                "        FROM OMPRF\n" +
                "        WHERE PRP_ID = ? and PRP_LEVEL = 'Main_Ope' ", hFRPF_MAINPD_ID_POS2);
        hFRPF_PF_OBJ_POS2=ArrayUtils.get(one,0);

        one=baseCore.queryOne("SELECT  PHOTO_LAYER,PRF_RKEY\n" +
                "        FROM OMPRSS\n" +
                "        Where PRF_RKEY = ? and OPE_NO = ?",hFRPF_PF_OBJ_POS2,
                hFRPOS_OPE_NO_POS2);

        hFRPOS_PHOTO_LAYER_POS2=ArrayUtils.get(one,0);

        photoLayer.setValue(hFRPOS_PHOTO_LAYER_POS2);

        return( returnOK() );
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param routeID
     * @param moduleNumber
     * @param stageID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/2 15:51
     */
    public Response getFRPF2(String routeID, Params.String moduleNumber, Params.String stageID ) {
        String hFRPF2_D_THE_SYSKEY      ;
        String hFRPF2_MAINPD_ID         ;
        String hFRPF2_PDLIST_MODULE_NO ;
        String hFRPF2_PDLIST_STAGE_ID   ;

        stageID.setValue("");
        hFRPF2_D_THE_SYSKEY = "";
        hFRPF2_MAINPD_ID = "";
        hFRPF2_PDLIST_MODULE_NO = "";
        hFRPF2_PDLIST_STAGE_ID = "";

        hFRPF2_MAINPD_ID = routeID ;
        hFRPF2_PDLIST_MODULE_NO = moduleNumber  .getValue();

        Object[] one = baseCore.queryOne("SELECT   ID,PRP_ID \n" +
                "        FROM  OMPRF\n" +
                "        WHERE PRP_ID = ? and PRP_LEVEL='Main_Mod' and ACTIVE_FLAG = 1", hFRPF2_MAINPD_ID);

        hFRPF2_D_THE_SYSKEY=ArrayUtils.get(one,0);

        one=baseCore.queryOne("SELECT     STAGE_ID,REFKEY  \n" +
                "        FROM    OMPRF_ROUTESEQ\n" +
                "        WHERE   REFKEY = ?\n" +
                "        AND     ROUTE_NO      = ?",
                hFRPF2_D_THE_SYSKEY,
                hFRPF2_PDLIST_MODULE_NO );

        hFRPF2_PDLIST_STAGE_ID=ArrayUtils.get(one,0);

        stageID.setValue(hFRPF2_PDLIST_STAGE_ID);

        return(returnOK());

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @exception
     * @author Ho
     * @date 2019/6/1 14:30
     */
    public Response getFRCODE(Infos.ObjectIdentifier codeID, Params.String codeDescription ) {
        String hFRCODE_CODE_OBJ   ;
        String hFRCODE_DESCRIPTION;

        hFRCODE_CODE_OBJ = "";
        hFRCODE_DESCRIPTION = "";

        hFRCODE_CODE_OBJ = codeID.getStringifiedObjectReference ();

        hFRCODE_DESCRIPTION= ArrayUtils.get(baseCore.queryOne("SELECT  DESCRIPTION ,ID \n" +
                "        FROM OMCODE\n" +
                "        Where ID = ?",hFRCODE_CODE_OBJ),0);

        codeDescription.setValue(hFRCODE_DESCRIPTION);
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentID
     * @param areaID
     * @param eqpDescription
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 14:14:15
     */
    public Response getFREQP(String equipmentID, String areaID, String eqpDescription) {
        Response response=new Response();
        String buff;
        String hFREQP_EQP_ID     ;
        String hFREQP_AREA_ID    ;
        String hFREQP_DESCRIPTION;

        String areaIDStr="";
        String eqpDescriptionStr="";
        hFREQP_EQP_ID="";
        hFREQP_AREA_ID="";
        hFREQP_DESCRIPTION="";

        hFREQP_EQP_ID=equipmentID;

        buff="SELECT BAY_ID, DESCRIPTION \n" +
                "        FROM OMEQP\n" +
                "        Where EQP_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFREQP_EQP_ID);
        hFREQP_AREA_ID=ArrayUtils.get(object,0);
        hFREQP_DESCRIPTION=ArrayUtils.get(object,1);

        areaIDStr=hFREQP_AREA_ID;

        eqpDescriptionStr=hFREQP_DESCRIPTION;

        response.setCode(0);
        response.setBody(new Object[]{areaIDStr,eqpDescriptionStr});

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param mainPDID
     * @param operationNO
     * @param passCount
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 11:00
     */
    public Response getFRPOS_Abnormal( String lotID, String mainPDID, String operationNO, Long passCount, Infos.Frpos resultData ) {
        String hFRPOS_LOT_ID      ;
        String hFRPOS_MAINPD_ID   ;
        String hFRPOS_OPE_NO2     ;
        String hFRPOS_PHOTO_LAYER2;

        hFRPOS_LOT_ID = "";
        hFRPOS_MAINPD_ID = "";
        hFRPOS_OPE_NO2 = "";
        Long hFRPO_PASS_COUNT2 = 0L;
        hFRPOS_PHOTO_LAYER2 = "";
        hFRPOS_LOT_ID = lotID       ;
        hFRPOS_MAINPD_ID = mainPDID    ;
        hFRPOS_OPE_NO2 = operationNO ;
        hFRPO_PASS_COUNT2 = passCount;

        Object[] one = baseCore.queryOne("SELECT  POS.PHOTO_LAYER,LOT.LOT_ID\n" +
                        "        FROM OMPRSS        POS,\n" +
                        "            OMPROPE         PO,\n" +
                        "            OMLOT        LOT,\n" +
                        "            OMPRFCX        PFX,\n" +
                        "            OMPRFCX_PROPESEQ PFX_POLIST\n" +
                        "        WHERE   LOT.LOT_ID                = ?\n" +
                        "        AND     PFX.ID               =  LOT.PRFCX_RKEY\n" +
                        "        AND     PFX_POLIST.REFKEY =  PFX.ID\n" +
                        "        AND     PO.ID                 =  PFX_POLIST.PROPE_RKEY\n" +
                        "        AND     PO.MAIN_PROCESS_ID              = ?\n" +
                        "        AND     PO.OPE_NO                 = ?\n" +
                        "        AND     PO.PASS_COUNT             = ?\n" +
                        "        AND     POS.ID               = PO.MPROCESS_PRSS_RKEY"
                , hFRPOS_LOT_ID
                , hFRPOS_MAINPD_ID
                , hFRPOS_OPE_NO2
                , hFRPO_PASS_COUNT2);

        hFRPOS_PHOTO_LAYER2=one==null?null:StringUtils.convert(one[0]);

        resultData.setPhotoLayer(hFRPOS_PHOTO_LAYER2);
        String tempOperationNo;
        String temModuleNumber;
        tempOperationNo = "";
        temModuleNumber = "";
        tempOperationNo = operationNO;
        String token = strtok( tempOperationNo, "\\." );
        temModuleNumber = token ;

        Response iRc = returnOK();
        Params.String stageID;
        stageID=new Params.String();
        iRc = getFRPF2( mainPDID, temModuleNumber, stageID );
        if( !BaseUtils.isOk(iRc)) {
            return( iRc );
        }
        resultData.setStageID( stageID.getValue() );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lotID
     * @param mainPDID
     * @param operationNO
     * @param passCount
     * @param equipmentID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 14:46
     */
    public Response getFRPO(String lotID, String mainPDID, String operationNO, Long passCount, Params.String equipmentID ) {
        String            buff;
        String  hFRPO_LOT_ID     ;
        String  hFRPO_MAINPD_ID  ;
        String  hFRPO_OPE_NO     ;
        String  hFRPO_ASGN_EQP_ID;

        Long hFRPO_PASS_COUNT;

        log.info("HistoryWatchDogServer::getOMPROPE" );

        equipmentID.setValue("");
        hFRPO_LOT_ID=      "";
        hFRPO_MAINPD_ID=   "";
        hFRPO_OPE_NO=      "";
        hFRPO_ASGN_EQP_ID= "";
        hFRPO_PASS_COUNT = 0L;

        hFRPO_LOT_ID=      lotID      ;
        hFRPO_MAINPD_ID=   mainPDID   ;
        hFRPO_OPE_NO=      operationNO;
        hFRPO_PASS_COUNT = passCount;

        buff= "";
        buff=sprintf(
                "SQL State : EXEC SQL SELECT PO.ASGN_EQP_ID into :hOMPROPE_ASGN_EQP_ID\n"+
                        "                     FROM OMPROPE         PO\n"+
                        "                          OMLOT        LOT\n"+
                        "                          OMPRFCX        PFX\n"+
                        "                          OMPRFCX_PROPESEQ POLIST\n"+
                        "                     Where LOT.LOT_ID                  = %s\n"+
                        "                     AND     PFX.PFX_OBJ               = LOT.PRFCX_RKEY\n"+
                        "                     AND     PFX_POLIST.REFKEY = PFX.ID\n"+
                        "                     AND     PO.ID                 = PFX_POLIST.PROPE_RKEY\n"+
                        "                     AND     PO.MAIN_PROCESS_ID              = %s\n"+
                        "                     AND     PO.OPE_NO                 = %s\n"+
                        "                     AND     PO.PASS_COUNT             = %d\n",
                hFRPO_LOT_ID,hFRPO_MAINPD_ID, hFRPO_OPE_NO,hFRPO_PASS_COUNT);
        log.info( buff );

        Object[] one=baseCore.queryOne("SELECT PO.ALLOC_EQP_ID,LOT.LOT_ID"+
                        " FROM    OMPROPE         PO,"+
                        " OMLOT        LOT,"+
                        " OMPRFCX        PFX,"+
                        " OMPRFCX_PROPESEQ PFX_POLIST"+
                        " WHERE   LOT.LOT_ID                = ?"+
                        " AND     PFX.ID               =  LOT.PRFCX_RKEY"+
                        " AND     PFX_POLIST.REFKEY =  PFX.ID"+
                        " AND     PO.ID                 =  PFX_POLIST.PROPE_RKEY"+
                        " AND     PO.MAIN_PROCESS_ID              = ?"+
                        " AND     PO.OPE_NO                 = ?"+
                        " AND     PO.PASS_COUNT             = ?",
                hFRPO_LOT_ID,
                hFRPO_MAINPD_ID,
                hFRPO_OPE_NO,
                hFRPO_PASS_COUNT);
        hFRPO_ASGN_EQP_ID=one==null?null:convert(one[0]);

        log.info("Get OMPROPE Successful {}",
                hFRPO_ASGN_EQP_ID );
        equipmentID.setValue( hFRPO_ASGN_EQP_ID);
        log.info("HistoryWatchDogServer::getOMPROPE" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param waferID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 14:47
     */
    public Response getFHSCRHS( String waferID, Infos.Ohscrhs resultData ) {
        String            buff;
        String  hFHSCRHS_WAFER_ID           ;
        String  hFHSCRHS_REASON_LOT_ID      ;
        String  hFHSCRHS_REASON_MAINPD_ID   ;
        String  hFHSCRHS_REASON_OPE_NO      ;
        String  hFHSCRHS_REASON_PD_ID       ;
        Long hFHSCRHS_REASON_PASS_COUNT;
        String  hFHSCRHS_REASON_OPE_NAME    ;
        String  hFHSCRHS_REASON_TEST_TYPE   ;
        String  hFHSCRHS_REASON_STAGE_ID    ;
        String  hFHSCRHS_REASON_STAGEGRP_ID ;
        String  hFHSCRHS_REASON_PHOTO_LAYER ;
        String  hFHSCRHS_REASON_DEPARTMENT  ;
        String  hFHSCRHS_REASON_LOCATION_ID ;
        String  hFHSCRHS_REASON_AREA_ID     ;
        String  hFHSCRHS_REASON_EQP_ID      ;
        String  hFHSCRHS_REASON_EQP_NAME    ;
        String  hFHSCRHS_CLAIM_TIME         ;

        log.info("HistoryWatchDogServer::GetFHSCRHS" );
        hFHSCRHS_WAFER_ID=          "";
        hFHSCRHS_REASON_LOT_ID=     "";
        hFHSCRHS_REASON_MAINPD_ID=  "";
        hFHSCRHS_REASON_OPE_NO=     "";
        hFHSCRHS_REASON_PD_ID=      "";
        hFHSCRHS_REASON_PASS_COUNT = 0L;
        hFHSCRHS_REASON_OPE_NAME=   "";
        hFHSCRHS_REASON_TEST_TYPE=  "";
        hFHSCRHS_REASON_STAGE_ID=   "";
        hFHSCRHS_REASON_STAGEGRP_ID="";
        hFHSCRHS_REASON_PHOTO_LAYER="";
        hFHSCRHS_REASON_DEPARTMENT= "";
        hFHSCRHS_REASON_LOCATION_ID="";
        hFHSCRHS_REASON_AREA_ID=    "";
        hFHSCRHS_REASON_EQP_ID=     "";
        hFHSCRHS_REASON_EQP_NAME=   "";
        hFHSCRHS_CLAIM_TIME=        "";

        hFHSCRHS_WAFER_ID= waferID ;

        buff= "";
        buff=sprintf(
                "SQL State : EXEC SQL DECLARE CURSOR_1 CURSOR FOR\n"+
                        "               SELECT  REASON_LOT_ID,REASON_PROCESS_ID,REASON_OPE_NO,REASON_STEP_ID,REASON_PASS_COUNT,\n"+
                        "                       REASON_OPE_NAME,REASON_TEST_TYPE,REASON_STAGE_ID,REASON_STAGE_GRP_ID,REASON_PHOTO_LAYER,\n"+
                        "                       REASON_DEPARTMENT,REASON_LOCATION_ID,REASON_BAY_ID,REASON_EQP_ID,REASON_EQP_NAME\n"+
                        "               FROM OHSCRAP\n"+
                        "               Where WAFER_ID = %s\n"+
                        "               Order BY TRX_TIME DESC", hFHSCRHS_WAFER_ID );
        log.info(buff );

        java.util.List<Object[]> CURSOR_1 = baseCore.queryAll("SELECT  REASON_LOT_ID,     REASON_PROCESS_ID,   REASON_OPE_NO,   REASON_STEP_ID,       REASON_PASS_COUNT,\n" +
                        "                REASON_OPE_NAME,   REASON_TEST_TYPE,   REASON_STAGE_ID, REASON_STAGE_GRP_ID, REASON_PHOTO_LAYER,\n" +
                        "                REASON_DEPARTMENT, REASON_LOCATION_ID, REASON_BAY_ID,  REASON_EQP_ID,      REASON_EQP_NAME\n" +
                        "        FROM OHSCRAP\n" +
                        "        Where WAFER_ID = ?\n" +
                        "        Order BY TRX_TIME DESC",
                hFHSCRHS_WAFER_ID);

        for (Object[] cursor_1:CURSOR_1){
            hFHSCRHS_REASON_LOT_ID=convert(cursor_1[0]);
                    hFHSCRHS_REASON_MAINPD_ID=convert(cursor_1[1]);
            hFHSCRHS_REASON_OPE_NO=convert(cursor_1[2]);
                    hFHSCRHS_REASON_PD_ID=convert(cursor_1[3]);
            hFHSCRHS_REASON_PASS_COUNT=convertL(cursor_1[4]);
                    hFHSCRHS_REASON_OPE_NAME=convert(cursor_1[5]);
            hFHSCRHS_REASON_TEST_TYPE=convert(cursor_1[6]);
                    hFHSCRHS_REASON_STAGE_ID=convert(cursor_1[7]);
            hFHSCRHS_REASON_STAGEGRP_ID=convert(cursor_1[8]);
                    hFHSCRHS_REASON_PHOTO_LAYER=convert(cursor_1[9]);
            hFHSCRHS_REASON_DEPARTMENT=convert(cursor_1[10]);
                    hFHSCRHS_REASON_LOCATION_ID=convert(cursor_1[11]);
            hFHSCRHS_REASON_AREA_ID=convert(cursor_1[12]);
                    hFHSCRHS_REASON_EQP_ID=convert(cursor_1[13]);
            hFHSCRHS_REASON_EQP_NAME=convert(cursor_1[14]);
        }

        resultData.setReason_lot_id(hFHSCRHS_REASON_LOT_ID      );
        resultData.setReason_mainpd_id(hFHSCRHS_REASON_MAINPD_ID   );
        resultData.setReason_ope_no(hFHSCRHS_REASON_OPE_NO      );
        resultData.setReason_pd_id(hFHSCRHS_REASON_PD_ID       );
        resultData.setReason_pass_count (hFHSCRHS_REASON_PASS_COUNT==null?null:
                hFHSCRHS_REASON_PASS_COUNT.intValue());
        resultData.setReason_ope_name(hFHSCRHS_REASON_OPE_NAME    );
        resultData.setReason_test_type(hFHSCRHS_REASON_TEST_TYPE   );
        resultData.setReason_stage_id(hFHSCRHS_REASON_STAGE_ID    );
        resultData.setReason_stagegrp_id(hFHSCRHS_REASON_STAGEGRP_ID );
        resultData.setReason_photo_layer(hFHSCRHS_REASON_PHOTO_LAYER );
        resultData.setReason_department(hFHSCRHS_REASON_DEPARTMENT  );
        resultData.setReason_location_id(hFHSCRHS_REASON_LOCATION_ID );
        resultData.setReason_area_id(hFHSCRHS_REASON_AREA_ID     );
        resultData.setReason_eqp_id(hFHSCRHS_REASON_EQP_ID      );
        resultData.setReason_eqp_name(hFHSCRHS_REASON_EQP_NAME    );

        log.info("HistoryWatchDogServer::GetFHSCRHS" );
        return(returnOK());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objrefMainPF
     * @param moduleNumber
     * @param stageID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/29 14:00
     */
    public Response getFRPF(String objrefMainPF, String moduleNumber, Params.String stageID ) {
        String            buff;
        String hFRPF_D_THE_SYSKEY      ;
        String hFRPF_PF_OBJ           ;
        String hFRPF_PDLIST_MODULE_NO ;
        String hFRPF_PDLIST_STAGE_ID   ;

        log.info("HistoryWatchDogServer::getOMPRF" );


        hFRPF_D_THE_SYSKEY=     "";
        hFRPF_PF_OBJ=           "";
        hFRPF_PDLIST_MODULE_NO= "";
        hFRPF_PDLIST_STAGE_ID=  "";

        hFRPF_PF_OBJ= objrefMainPF ;
        hFRPF_PDLIST_MODULE_NO= moduleNumber  ;

        buff= "";
        buff=sprintf(
                "SQL State : EXEC SQL SELECT  ID into :hOMPRF_D_THE_SYSKEY \n"+
                        "                     FROM OMPRF \n"+
                        "                     Where ID = %s" ,hFRPF_PF_OBJ );

        log.info( 3+ buff );

        Object[] one=baseCore.queryOne("SELECT   ID ,ID as alid\n"+
                        "FROM  OMPRF\n"+
                        "WHERE ID = ? ",
                hFRPF_PF_OBJ);
        hFRPF_D_THE_SYSKEY=convert(one[0]);

        log.info("Get OMPRF Successful "+
                hFRPF_D_THE_SYSKEY );

        buff= "";
        buff=sprintf(
                "SQL State : EXEC SQL SELECT  STAGE_ID into :hOMPRF_ROUTESEQ_STAGE_ID \n"+
                        "                     FROM  OMPRF_ROUTESEQ \n"+
                        "                     WHERE REFKEY = %s\n"+
                        "                     AND   ROUTE_NO = %s\n",
                hFRPF_D_THE_SYSKEY, hFRPF_PDLIST_MODULE_NO ) ;

        log.info( 3+ buff );

        one=baseCore.queryOne("SELECT     STAGE_ID   ,REFKEY\n"+
                        "FROM    OMPRF_ROUTESEQ\n"+
                        "WHERE   REFKEY = ?\n"+
                        "AND     ROUTE_NO      = ? ",
                hFRPF_D_THE_SYSKEY,
                hFRPF_PDLIST_MODULE_NO);
        hFRPF_PDLIST_STAGE_ID=convert(one[0]);

        log.info("Get OMPRF Successful "+
                hFRPF_PDLIST_STAGE_ID );

        stageID.setValue( hFRPF_PDLIST_STAGE_ID);

        log.info("HistoryWatchDogServer::getOMPRF" );
        return(returnOK());

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param routeID
     * @param moduleNumber
     * @param stageID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/7/24 11:10
     */
    public Response getFRPF2(String routeID, String moduleNumber, Params.String stageID ) {
        Params.String _moduleNumber=new Params.String();
        _moduleNumber.setValue(moduleNumber);
        return getFRPF2(routeID,_moduleNumber,stageID);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param equipmentID
     * @param areaID
     * @param eqpDescription
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/5 17:02
     */
    public Response getFREQP(String equipmentID, Params.String areaID, Params.String eqpDescription) {
        Response response=new Response();
        String buff;
        String hFREQP_EQP_ID     ;
        String hFREQP_AREA_ID    ;
        String hFREQP_DESCRIPTION;

        hFREQP_EQP_ID="";
        hFREQP_AREA_ID="";
        hFREQP_DESCRIPTION="";

        hFREQP_EQP_ID=equipmentID;

        buff="SELECT BAY_ID, DESCRIPTION \n" +
                "        FROM OMEQP\n" +
                "        WHERE EQP_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFREQP_EQP_ID);
        hFREQP_AREA_ID=ArrayUtils.get(object,0);
        hFREQP_DESCRIPTION=ArrayUtils.get(object,1);

        areaID.setValue(hFREQP_AREA_ID);

        eqpDescription.setValue(hFREQP_DESCRIPTION);

        response.setCode(0);
        response.setBody(new Object[]{areaID,eqpDescription});

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param areaID
     * @param locationID
     * @return com.fa.cim.dto.Response
     * @author Ho
     * @date 2019/2/27 14:21:14
     */
    public Response getFRAREA(Params.String areaID, Params.String locationID) {
        Response response=new Response();
        String buff;

        String hFRAREA_AREA_ID     ;
        String hFRAREA_SUPERAREA_ID;

        hFRAREA_AREA_ID="";
        hFRAREA_SUPERAREA_ID="";

        hFRAREA_AREA_ID=areaID.getValue();

        buff="SELECT  FAB_ID,BAY_ID \n" +
                "        FROM OMBAY\n" +
                "        Where BAY_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFRAREA_AREA_ID);
        hFRAREA_SUPERAREA_ID=ArrayUtils.get(object,0);

        locationID.setValue(hFRAREA_SUPERAREA_ID);

        response.setCode(0);
        response.setBody(locationID);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param areaID
     * @param locationID
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/6/1 16:40
     */
    public Response getFRAREA(String areaID, String locationID) {
        Response response=new Response();
        String buff;

        String hFRAREA_AREA_ID     ;
        String hFRAREA_SUPERAREA_ID;

        String locationIDStr="";
        hFRAREA_AREA_ID="";
        hFRAREA_SUPERAREA_ID="";

        hFRAREA_AREA_ID=areaID;

        buff="SELECT  FAB_ID,BAY_ID \n" +
                "        FROM OMBAY\n" +
                "        Where BAY_ID = ?";
        Object[] object=baseCore.queryOne(buff,hFRAREA_AREA_ID);
        hFRAREA_SUPERAREA_ID=ArrayUtils.get(object,0);

        locationIDStr=hFRAREA_SUPERAREA_ID;

        response.setCode(0);
        response.setBody(locationIDStr);

        return response;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param lcrecipeID
     * @param resultData
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/3/1 16:55
     */
    public Response getFRLRCP(String lcrecipeID, Infos.Frlrcp resultData) {
        Response response=new Response();
        String hFRLRCP_LCRECIPE_ID;
        String hFRLRCP_TESTTYPE_ID;

        hFRLRCP_LCRECIPE_ID = lcrecipeID;

        Object[] object = baseCore.queryOne("SELECT TEST_TYPE_ID,LRCP_ID\n" +
                "        FROM OMLRCP\n" +
                "        WHERE LRCP_ID = ?", hFRLRCP_LCRECIPE_ID);

        hFRLRCP_TESTTYPE_ID=ArrayUtils.get(object,0);

        resultData.setTest_type(hFRLRCP_TESTTYPE_ID);

        response.setCode(0);
        response.setBody(resultData);

        return response;
    }
}
