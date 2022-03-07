package com.fa.cim.core;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.jpa.SearchCondition;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * description:
 * change history:
 * date             defect             person             comments
 * -----------------------------------------------------------------------------------------------------------------
 * @author Sun
 * @date 12/25/2018 3:48 PM
 * @return
 */
@lombok.Setter
@lombok.Getter
@ToString
@EqualsAndHashCode
public class TestCommonData implements Serializable {

    //test environment common data;
    private ObjectIdentifier USERID;

    private String USERPASSWORD;

    private ObjectIdentifier BANKID;

    private ObjectIdentifier EQUIPMENTID;

    private ObjectIdentifier EQUIPMENTID2;

    private ObjectIdentifier EQUIPMENTID3;

    private ObjectIdentifier MACHINERECEIPE;

    private ObjectIdentifier STOCKERID;

    private User USER = new User();

    private SearchCondition SEARCHCONDITION = new SearchCondition();

    private ObjectIdentifier ENDBANKID ;

    private ObjectIdentifier PRODUCTSPECID;

    private ObjectIdentifier SHIPBANKID;

    private ObjectIdentifier PRODUCTID;

    private ObjectIdentifier CARRIERID;

    private ObjectIdentifier RETICLEID;

    private ObjectIdentifier RETICLEPODID;

    private String PROTGROUPID;
    private ObjectIdentifier PROTID;
    private ObjectIdentifier UNPROTID;
    private Long LOADSEQUENCENUMBER;
    public TestCommonData(Boolean isForDev) {
        if(isForDev){
            USERID = new ObjectIdentifier("ADMIN");
            USERPASSWORD = "b51fa595e692d53739b69131cdc73440";
            BANKID = new ObjectIdentifier("BNK-0S");
            EQUIPMENTID = new ObjectIdentifier("1SRT03");
            EQUIPMENTID2 = new ObjectIdentifier("1SRT01");
            EQUIPMENTID3 = new ObjectIdentifier("1TKI02_EXI02");
            MACHINERECEIPE=new ObjectIdentifier("SRT.1SRT01.01","FRMRCP.55000434487038374");
            PROTGROUPID = "PG1";
            PROTID = new ObjectIdentifier("P1");
            UNPROTID = new ObjectIdentifier("P1");
            USER.setUserID(USERID);
            USER.setPassword(USERPASSWORD);
            SEARCHCONDITION.setSize(300);
            SEARCHCONDITION.setPage(1);
            ENDBANKID = new ObjectIdentifier("BKP-ENDI");
            PRODUCTSPECID = new ObjectIdentifier("RAW-2000.01");
            PRODUCTID = new ObjectIdentifier("PRODUCT0.01");
            RETICLEID = new ObjectIdentifier("Reticle_B05");
            RETICLEPODID = new ObjectIdentifier("RPOD25");
            SHIPBANKID = new ObjectIdentifier("BKP-WIPG");
            STOCKERID = new ObjectIdentifier("SHELF01","OMSTOCKER.29978739629766351");
            CARRIERID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");
            LOADSEQUENCENUMBER = 1L;
        }else{
            USERID = new ObjectIdentifier("ADMIN");
            USERPASSWORD = "b51fa595e692d53739b69131cdc73440";
            BANKID = new ObjectIdentifier("BNK-0S");
            EQUIPMENTID = new ObjectIdentifier("1SRT04");
            EQUIPMENTID2 = new ObjectIdentifier("1SRT01");
            EQUIPMENTID3 = new ObjectIdentifier("1TKI02_EXI02");
            PROTGROUPID = "PG1";
            PROTID = new ObjectIdentifier("P1");
            UNPROTID = new ObjectIdentifier("P1");
            USER.setUserID(USERID);
            USER.setPassword(USERPASSWORD);
            SEARCHCONDITION.setSize(100);
            SEARCHCONDITION.setPage(1);
            ENDBANKID = new ObjectIdentifier("BKP-ENDI");
            PRODUCTSPECID = new ObjectIdentifier("RAW-2000.01");
            PRODUCTID = new ObjectIdentifier("PRODUCT0.01");
            RETICLEID = new ObjectIdentifier("Reticle_B05");
            RETICLEPODID = new ObjectIdentifier("RPOD25");
            SHIPBANKID = new ObjectIdentifier("BKP-WIPG");
            STOCKERID = new ObjectIdentifier("SHELF01","OMSTOCKER.29978739629766351");
            CARRIERID = new ObjectIdentifier("CRUP0001","FRCAST.81057531304862583");
            LOADSEQUENCENUMBER = 1L;
        }
    }

    public TestCommonData(ObjectIdentifier userID, String passWord,
                          ObjectIdentifier bankID, ObjectIdentifier eqpID, ObjectIdentifier eqpID2,
                          int pageSize, int pageNumber,ObjectIdentifier endBankID,
                          ObjectIdentifier productSpecID,ObjectIdentifier shipBankID) {
        USERID = userID;
        USERPASSWORD = passWord;
        BANKID = bankID;
        EQUIPMENTID = eqpID;
        EQUIPMENTID2 = eqpID2;
        USER.setUserID(USERID);
        USER.setPassword(USERPASSWORD);
        SEARCHCONDITION.setSize(pageSize > 0 ? pageSize : 100);
        SEARCHCONDITION.setPage(pageNumber > 0 ? pageNumber : 1);
        ENDBANKID = endBankID;
        PRODUCTSPECID = productSpecID;
        SHIPBANKID = shipBankID;
    }
}