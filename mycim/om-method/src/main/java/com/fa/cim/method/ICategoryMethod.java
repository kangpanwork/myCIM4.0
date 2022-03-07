package com.fa.cim.method;

import com.fa.cim.dto.Infos;

import java.util.List;

public interface ICategoryMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<List<com.fa.cim.pojo.Infos.SubMfgLayerAttributes>>
     * @author Sun
     * @since 2018/10/23 15:05
     */
    List<Infos.SubMfgLayerAttributes> categoryFillInTxTRQ010DR(Infos.ObjCommon objCommon);
}
