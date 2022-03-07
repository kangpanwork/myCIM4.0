package com.fa.cim.utils;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/30          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/6/30 21:14
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class GenerateVendorlot {

    public static String getVendorLot(){
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        return "VL" + currentTimeMillis.substring(currentTimeMillis.length() - 6) + ".00";
    }
    public static String getVendorLot(int addNum){
        String currentTimeMillis = String.valueOf(System.currentTimeMillis() + addNum);
        return "VL" + currentTimeMillis.substring(currentTimeMillis.length() - 6) + ".00";
    }
}