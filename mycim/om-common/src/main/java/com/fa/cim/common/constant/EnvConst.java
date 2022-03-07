package com.fa.cim.common.constant;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/20        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/9/20 11:32
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class EnvConst {

    public static final String OM_CARRIER_CHK_FOR_LOT_START = "OM_CARRIER_CHK_FOR_LOT_START";

    public static final String SP_AUTH_AUTHSERVER_AVAILABLE = "SP_AUTH_AUTHSERVER_AVAILABLE";
    public static final String SP_DO_COLUMN_MAX_LENGTH_USER_PASSWORD = "SP_DO_COLUMN_MAX_LENGTH_USER_PASSWORD";
    public static final String SP_PRIVILEGE_CHECK_BY_DR_FLAG = "SP_PRIVILEGE_CHECK_BY_DR_FLAG";

    public static final String SP_DISPATCH_CAST_CLEARED_BY_CHANGE_TO_ONLINE = "SP_DISPATCH_CAST_CLEARED_BY_CHANGE_TO_ONLINE";
    public static final String PPT_TRACE_INCOMING = "PPT_TRACE_INCOMING";

    public static final String SP_MAIL_SENDER = "SP_MAIL_SENDER";

    /******************* email **********************************/
    public static final String SENTMESSAGECHECKTIME_STRING = "SP_SENTMESSAGECHECKTIME";
    public static final String TEMPLATEFILEPATH_STRING = "SP_TEMPLATEFILEPATH";
    /***********************************************************/


    public static final String SP_POST_PROCESS_ASYNC_FLAG = "SP_POST_PROCESS_ASYNC_FLAG";

    public static final String SP_RTD_FUNCTION_CODE_RETICLEACTIONLISTINQ = "SP_RTD_FUNCTION_CODE_RETICLEACTIONLISTINQ";

    public static final String SP_SEASON_PM_STATUS = "SP_SEASON_PM_STATUS";

    public static final String OM_REGISTER_FOSB = "OM_REGISTER_FOSB";
    public static final String OM_STK_ZONE_TYPE_FILE_PATH = "OM_STK_ZONE_TYPE_FILE_PATH";

    public static boolean ifEquals(String envConst, String value) {
        if(value == null){
            return false;
        }
        if (value instanceof String) {
            return value.equals(envConst);
        }
        return (null == envConst);
    }
}
