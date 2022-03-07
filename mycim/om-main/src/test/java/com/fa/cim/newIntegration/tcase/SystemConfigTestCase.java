package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.controller.interfaces.systemConfig.ISystemInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
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
 * @date: 2019/12/9 18:22
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class SystemConfigTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;
    @Autowired
    private ISystemInqController systemConfigInqController;

    public List<Infos.CodeInfo> codeSelectionInqCase(String categoryID) {
        Params.CodeSelectionInqParams codeSelectionInqParams = new Params.CodeSelectionInqParams();
        codeSelectionInqParams.setUser(testCommonData.getUSER());
        codeSelectionInqParams.setCategory(new ObjectIdentifier(categoryID));
        return (List<Infos.CodeInfo>) systemConfigInqController.codeSelectionInq(codeSelectionInqParams).getBody();
    }

}