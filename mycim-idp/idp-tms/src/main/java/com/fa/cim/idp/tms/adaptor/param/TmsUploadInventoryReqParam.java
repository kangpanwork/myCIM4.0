package com.fa.cim.idp.tms.adaptor.param;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.idp.tms.adaptor.TmsAdapt;
import com.fa.cim.idp.tms.adaptor.common.TmsUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/3/30 16:09
 */
@Data
public class TmsUploadInventoryReqParam implements TmsAdapt<Inputs.SendUploadInventoryReqIn, TmsUploadInventoryReqParam> {

    private static final long serialVersionUID = -2987098808851427245L;

    private TmsUser requestUserID;
    private ObjectIdentifier machineID;
    private String uploadLevel;


    @Override
    public Inputs.SendUploadInventoryReqIn adapt() {
        Inputs.SendUploadInventoryReqIn result = new Inputs.SendUploadInventoryReqIn();
        if (null != requestUserID)
            result.setUser(this.requestUserID.adapt());
        result.setUploadInventoryReq(new Infos.UploadInventoryReq());
        BeanUtils.copyProperties(this,result.getUploadInventoryReq());
        return result;
    }

    @Override
    public TmsUploadInventoryReqParam from(Inputs.SendUploadInventoryReqIn obj) {
        this.requestUserID = new TmsUser().from(obj.getUser());
        Infos.UploadInventoryReq uploadInventoryReq = obj.getUploadInventoryReq();
        BeanUtils.copyProperties(uploadInventoryReq,this);
        return this;
    }

}
