package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.einfo.ElectronicInformationController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/13        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/13 13:42
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
public class LotNoteCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private ElectronicInformationController electronicInformationController;

    public Response lotNote_norm(Infos.LotNoteInfo lotNoteInfo) {
        //【step1】einfo/lot_memo_info/inq
        Params.LotMemoInfoInqParams lotMemoInfoInqParams = new Params.LotMemoInfoInqParams();
        lotMemoInfoInqParams.setUser(testUtils.getUser());
        lotMemoInfoInqParams.setLotID(lotNoteInfo.getReportUserID());
        electronicInformationInqController.lotMemoInfoInq(lotMemoInfoInqParams);

        //【step2】einfo/lot_memo_add/req
        Params.LotMemoAddReqParams params = new Params.LotMemoAddReqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotNoteInfo.getReportUserID());
        params.setLotNoteDescription(lotNoteInfo.getLotNoteDescription());
        params.setLotNoteTitle(lotNoteInfo.getLotNoteTitle());
        return electronicInformationController.lotMemoAddReq(params);
    }
}