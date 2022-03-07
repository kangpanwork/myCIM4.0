package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.dto.Infos;
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
 * 2020/3/27                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/3/27 15:10
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class TmsTransportJobInqParam implements TmsAdapt<Inputs.SendTransportJobInqIn,TmsTransportJobInqParam> {

    private static final long serialVersionUID = -2987098808851427245L;

    private TmsUser requestUserID;
    private String                                   inquiryType;
    private TmsIdentifier                            carrierID;
    private TmsIdentifier                            toMachineID;
    private TmsIdentifier                            fromMachineID;
    private Object                                   siInfo;
    private String functionID;


    @Override
    public Inputs.SendTransportJobInqIn adapt() {
        Inputs.SendTransportJobInqIn result = new Inputs.SendTransportJobInqIn();
        result.setFunctionID(this.functionID);
        if (null != requestUserID)
            result.setUser(this.requestUserID.adapt());
        result.setTransportJobInq(new Infos.TransportJobInq());
        if (null != carrierID)
            result.getTransportJobInq().setCarrierID(carrierID.adapt());
            result.getTransportJobInq().setFromMachineID(fromMachineID.adapt());
            result.getTransportJobInq().setInquiryType(inquiryType);
            result.getTransportJobInq().setToMachineID(toMachineID.adapt());
        return result;

    }

    @Override
    public TmsTransportJobInqParam from(Inputs.SendTransportJobInqIn obj) {
        this.functionID = obj.getFunctionID();
        this.requestUserID = new TmsUser().from(obj.getUser());
        if (null != obj.getTransportJobInq()) {
            this.carrierID = new TmsIdentifier().from(obj.getTransportJobInq().getCarrierID());
            this.toMachineID = new TmsIdentifier().from(obj.getTransportJobInq().getToMachineID());
            this.fromMachineID = new TmsIdentifier().from(obj.getTransportJobInq().getFromMachineID());
            this.inquiryType = obj.getTransportJobInq().getInquiryType();
        }
        return this;
    }
}
