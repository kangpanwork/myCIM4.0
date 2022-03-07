package com.fa.cim.newIntegration.tcase;

import com.fa.cim.controller.durable.DurableInqController;
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
 * 2019/12/9          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/9 18:34
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DurableTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;
    @Autowired
    private DurableInqController durableInqController;

    public List<Infos.FoundReticle> reticleListInqCase() {
        Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
        reticleListInqParams.setUser(testCommonData.getUSER());
        reticleListInqParams.setMaxRetrieveCount(9999);
        reticleListInqParams.setWhiteDefSearchCriteria("All");
        return ((Results.ReticleListInqResult) durableInqController.reticleListInq(reticleListInqParams).getBody()).getStrFoundReticle();
    }
}