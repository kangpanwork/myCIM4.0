package com.fa.cim.mfg;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/5/8        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/5/8 15:17
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class MfgInfoParams {

    @Excel(name = "Constraint Type", width = 40)
    private String constraintType;

    @Excel(name = "Constraint ID", orderNum = "1", width = 40)
    private String constraintID;

    @Excel(name = "Equipment", orderNum = "2", width = 40)
    private String equipment;

    @Excel(name = "Chamber", orderNum = "3", width = 40)
    private String chamber;

    @Excel(name = "Product", orderNum = "4", width = 40)
    private String product;

    @Excel(name = "Recipe", orderNum = "5",width = 40)
    private String recipe;

    @Excel(name = "Main PF", orderNum = "6", width = 40)
    private String mainPF;

    @Excel(name = "Step No", orderNum = "7", width = 40)
    private String operation;

    @Excel(name = "Route", orderNum = "8", width = 40)
    private String route;

    @Excel(name = "Step", orderNum = "9", width = 40)
    private String step;

    @Excel(name = "Reticle", orderNum = "10", width = 40)
    private String reticle;

    @Excel(name = "Reticle Group", orderNum = "11", width = 40)
    private String reticleGrp;

    @Excel(name = "Lot ID", orderNum = "12", width = 40)
    private String lotID;

    @Excel(name = "EX Product", orderNum = "13", width = 40)
    private String exProduct;

    @Excel(name = "EX Main PF", orderNum = "14", width = 40)
    private String exMainPF;

    @Excel(name = "EX Lot ID", orderNum = "15", width = 40)
    private String exLotID;

    @Excel(name = "Rule Function", orderNum = "16", width = 40)
    private String ruleFunction;

    @Excel(name = "Owner", orderNum = "17", width = 40)
    private String owner;

    @Excel(name = "Start Time", orderNum = "18", width = 40)
    private String startTime;

    @Excel(name = "End Time", orderNum = "19", width = 40)
    private String endTime;

    @Excel(name = "Claim Time", orderNum = "20", width = 40)
    private String claimTime;

    @Excel(name = "SubLotType", orderNum = "21", width = 40)
    private String subLotType;

    @Excel(name = "Reason Code", orderNum = "22", width = 40)
    private String reasonCode;

    @Excel(name = "Memo", orderNum = "23", width = 40)
    private String memo;
}