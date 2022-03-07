package com.fa.cim.eqp;

import lombok.Data;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/3/4        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/3/4 14:09
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class IBFurnaceEQPBatchInfo {

    private String controJobID;

    private List<lotInfoByCJ> lotInfoByCJList;

    @Data
    public static class lotInfoByCJ{

        private String lotID;

        private String castID;

        private String lotType;

        private String productID;

    }

}