package com.fa.cim.method.impl;

import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IRTDMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @return
 * @exception
 * @author ho
 * @date 2019/12/30 14:10
 */
@Service
@Slf4j
public class RTDMethod implements IRTDMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    public Infos.RTDDataGetDROut rtdDataGetDR(
            Infos.ObjCommon        strObjCommonIn,
            String                 functionCode,
            String                 dispatchStationID ) {

        String HV_BUFFER= "SELECT FUNCTION_CODE, STATION_ID, "+
                "OPTION_ATTRIBUTES, DEFAULT_TIMEOUT FROM FSRTDIF";

        Boolean whereFlg = FALSE;

        if ( CimStringUtils.length(functionCode) != 0 ) {
            /*--------------------------------*/
            /*   functionCode Condition       */
            /*--------------------------------*/
            HV_BUFFER += " WHERE" ;
            String HV_TMPBUFFER=String.format(" FUNCTION_CODE = '%s'", functionCode);
            HV_BUFFER+= HV_TMPBUFFER;

            whereFlg = TRUE;
        }

        if ( CimStringUtils.length(dispatchStationID) != 0 ) {
            /*--------------------------------*/
            /*   dispatchStationID Condition   */
            /*--------------------------------*/
            if ( whereFlg ) {
                HV_BUFFER+= " AND";
            } else {
                HV_BUFFER+= " WHERE";
                whereFlg = TRUE;
            }

            String HV_TMPBUFFER=String.format(" STATION_ID LIKE '%s'", dispatchStationID);
            HV_BUFFER+= HV_TMPBUFFER;
        }

        int nLoopCounter = 0;
        int nSeqLength = 1000;

        Infos.RTDDataGetDROut strRTDDataGetDROut=new Infos.RTDDataGetDROut();
        
        strRTDDataGetDROut.setRTDRecords(new ArrayList<>(nSeqLength));

        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        Boolean    bConvertFlag = FALSE;
        String              originalSQL;
        originalSQL="";
        originalSQL=HV_BUFFER;
        HV_BUFFER="";
        HV_BUFFER=originalSQL;

        List<Object[]> rtdg1 = cimJpaRepository.query(HV_BUFFER);



        for (Object[] rtd1:rtdg1) {
            String hFSRTDIFFUNCTION_CODE= CimObjectUtils.toString(rtd1[0]);
            String hFSRTDIFSTATION_ID= CimObjectUtils.toString(rtd1[1]);
            String hFSRTDIFOPTION_ATTRIBUTES= CimObjectUtils.toString(rtd1[2]);
            Long hFSRTDIFDEFAULT_TIMEOUT= CimLongUtils.longValue(rtd1[3]);

            if ( nLoopCounter >= nSeqLength ) {
//                strRTDDataGetDROut.RTDRecords.length(nSeqLength + 500);
                nSeqLength = nSeqLength + 500;
            }

            strRTDDataGetDROut.getRTDRecords().add(new Infos.RTDConfigInfo());
            strRTDDataGetDROut.getRTDRecords().get(nLoopCounter).setFunctionCode     ( hFSRTDIFFUNCTION_CODE);
            strRTDDataGetDROut.getRTDRecords().get(nLoopCounter).setStationID        ( hFSRTDIFSTATION_ID);
            strRTDDataGetDROut.getRTDRecords().get(nLoopCounter).setOptionAttributes ( hFSRTDIFOPTION_ATTRIBUTES);
            strRTDDataGetDROut.getRTDRecords().get(nLoopCounter).setDefaultTimeout   ( hFSRTDIFDEFAULT_TIMEOUT);

            String tmp;
            tmp=String.format("%ld", strRTDDataGetDROut.getRTDRecords().get(nLoopCounter).getDefaultTimeout());
            nLoopCounter++;
        }

        Validations.check(nLoopCounter == 0,retCodeConfigEx.getNotFoundRTD());


        return strRTDDataGetDROut;
    }

}