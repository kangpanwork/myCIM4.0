package com.fa.cim.common.support;

/**
 * description:
 * This ErrorCode use to return error message.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/25        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/6/25 14:39
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class ErrorCode extends OmCode {
    private static final long serialVersionUID = -254827031057932488L;

    private String errorMsg;
    public ErrorCode(String pErrorMsg) {
        super(ERROR_CODE, pErrorMsg);
    }
}
