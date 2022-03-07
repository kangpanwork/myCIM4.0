package com.fa.cim.method.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IFabMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/24          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/24 17:00
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
public class FabMethod implements IFabMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public List<Infos.InterFabDestinationInfo> fabInfoGetDR(Infos.ObjCommon objCommon, String fabID) {
        List<Infos.InterFabDestinationInfo> out = new ArrayList<>();
        String hvBuffer = "SELECT FAB_ID, STK_ID, STK_RKEY, DESCRIPTION "+
                " FROM OSMULTIFAB";
        String hvTmpbuffer = "";
        if (CimStringUtils.isNotEmpty(fabID)){
            hvTmpbuffer = String.format(" WHERE FAB_ID LIKE '%s'",fabID);
            hvBuffer += hvTmpbuffer;
        }
        //-------------------------------------------
        // Judge and Convert SQL with Escape Sequence
        //-------------------------------------------
        Boolean bConvertFlag = false;
        List<Object[]> queryResult = cimJpaRepository.query(hvBuffer);
        if (CimArrayUtils.getSize(queryResult)> 0){
            for (Object[] objects : queryResult) {
                Infos.InterFabDestinationInfo interFabDestinationInfo = new Infos.InterFabDestinationInfo();
                out.add(interFabDestinationInfo);
                interFabDestinationInfo.setFabID(String.valueOf(objects[0]));
                interFabDestinationInfo.setStockerID(ObjectIdentifier.build(String.valueOf(objects[1]),String.valueOf(objects[2])));
                interFabDestinationInfo.setDescription(String.valueOf(objects[3]));
            }
        }
        return out;
    }

}