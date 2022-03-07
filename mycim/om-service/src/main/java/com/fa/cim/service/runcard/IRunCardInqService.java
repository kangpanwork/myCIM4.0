package com.fa.cim.service.runcard;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IRunCardInqService {
    /**
     * description: Get RunCard List Service
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/9/24                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/9/24 13:06
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Page<List<Infos.RunCardInfo>> sxRunCardListInq(Infos.ObjCommon objCommon, Params.RunCardListInqParams params) ;
}
