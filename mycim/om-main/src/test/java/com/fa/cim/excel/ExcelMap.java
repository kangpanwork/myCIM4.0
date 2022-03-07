package com.fa.cim.excel;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * description:
 * <p>Entry .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/11/19         ********             ZQI               create file
 *
 * @author ZQI
 * @date 2019/11/19 10:40
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ExcelMap implements Serializable {
    private String columnName;
    private String columnValue;
    private List<ExcelMap> methods;
    private Node dataType;
    private Node URL;
    private Node TransactionID;

    @Data
    public static class Node{
        private String name;
        private String value;
    }
}
