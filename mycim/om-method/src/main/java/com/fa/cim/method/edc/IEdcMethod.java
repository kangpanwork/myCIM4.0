package com.fa.cim.method.edc;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

public interface IEdcMethod {

    void edcTempDataSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID,
                        ObjectIdentifier controlJobID, List<Infos.DataCollectionInfo> edcData);

    Infos.StartRecipe lotStartRecipeInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID,
                                            ObjectIdentifier controlJobID);

    List<Infos.DataCollectionInfo> edcSpecDataSet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier controlJobID,
                        List<Infos.DataCollectionInfo> edcData);
}
