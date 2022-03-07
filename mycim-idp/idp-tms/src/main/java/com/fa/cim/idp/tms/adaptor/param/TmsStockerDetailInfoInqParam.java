package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.dto.Inputs;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import com.fa.cim.idp.tms.adaptor.common.TmsIdentifier;
import com.fa.cim.idp.tms.adaptor.common.TmsUser;
import lombok.Data;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/30                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/3/30 13:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsStockerDetailInfoInqParam implements TmsAdapt<Inputs.StockerDetailInfoInq,TmsStockerDetailInfoInqParam> {
    private static final long serialVersionUID = 7390680964413418266L;
    private TmsUser requestUserID;
    private TmsIdentifier stockerDetailInfoInq;
    private Object          siInfo;


    @Override
    public Inputs.StockerDetailInfoInq adapt() {
        Inputs.StockerDetailInfoInq result = new Inputs.StockerDetailInfoInq();
        result.setSiInfo(this.siInfo);
        if (null != this.stockerDetailInfoInq)
            result.setMachineID(this.stockerDetailInfoInq.adapt());
        return result;
    }

    @Override
    public TmsStockerDetailInfoInqParam from(Inputs.StockerDetailInfoInq obj) {
        this.siInfo = obj.getSiInfo();
        if (null != obj.getMachineID())
            this.stockerDetailInfoInq = new TmsIdentifier().from(obj.getMachineID());
        return this;
    }
}
