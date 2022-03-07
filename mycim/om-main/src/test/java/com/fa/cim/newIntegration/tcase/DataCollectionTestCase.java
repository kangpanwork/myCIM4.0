package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.controller.interfaces.engineerDataCollection.IEngineerDataCollectionController;
import com.fa.cim.controller.interfaces.engineerDataCollection.IEngineerDataCollectionInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/20          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/20 13:50
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DataCollectionTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private IEngineerDataCollectionInqController engineerDataCollectionInqController;

    public List<Infos.DataCollection> edcConfigListInqCase(Infos.EDCConfigListInqInParm edcConfigListInqInParm){
        Params.EDCConfigListInqParams params = new Params.EDCConfigListInqParams();
        params.setUser(testCommonData.getUSER());
        params.setStrEDCConfigListInqInParm(edcConfigListInqInParm);
        return (List<Infos.DataCollection>) engineerDataCollectionInqController.edcConfigListInq(params).getBody();
    }

    public Results.EDCSpecInfoInqResult edcSpecInfoInqCase(ObjectIdentifier dcSpecID){
        Params.EDCSpecInfoInqParms parms = new Params.EDCSpecInfoInqParms();
        parms.setUser(testCommonData.getUSER());
        parms.setDcSpecID(dcSpecID);
        return (Results.EDCSpecInfoInqResult) engineerDataCollectionInqController.edcSpecInfoInq(parms).getBody();
    }


}