package com.fa.cim.tms.event.recovery.utils;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.event.recovery.dto.Code;
import com.fa.cim.tms.event.recovery.dto.Response;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * This Validations use to check business logic.
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/27        ********             miner               create file/OMS
 * 2019/2/18                             Miner                 create file/TMS
 *
 * @author: Miner
 * @date: 2018/6/27 16:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class Validations {

    private Validations() {
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression
     * @param code       -
     * @return void
     * @author Nyx
     * @date 2019/7/10 14:40
     */
    public static void check(final boolean expression, final Code code) {
        if (expression) {
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression
     * @param code
     * @param codeArgs   -
     * @return void
     * @author Nyx
     * @date 2019/10/10 11:31
     */
    public static void check(final boolean expression, Code code, Object... codeArgs) {
        if (expression) {
            List<String> strs = new ArrayList<>();
            for (Object obj : codeArgs) {
                if (obj instanceof ObjectIdentifier) {
                    strs.add(ObjectUtils.getObjectValue((ObjectIdentifier) obj));
                } else {
                    strs.add(String.valueOf(obj));
                }
            }
            code = new Code(code, strs.toArray(new String[strs.size()]));
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId());
        }
    }

    public static ServiceException buildException(Code code, Object... codeArgs) {
        List<String> strs = new ArrayList<>();
        for (Object obj : codeArgs) {
            if (obj instanceof ObjectIdentifier) {
                strs.add(ObjectUtils.getObjectValue((ObjectIdentifier) obj));
            } else {
                strs.add(String.valueOf(obj));
            }
        }
        Code codeClone = new Code(code, strs.toArray(new String[strs.size()]));
        return new ServiceException(codeClone.getCode(), codeClone.getMessage(), ThreadContextHolder.getTransactionId());
    }

    public static void check(final boolean expression, Object data, Code code, Object... codeArgs) {
        if (expression) {
            List<String> strs = new ArrayList<>();
            for (Object obj : codeArgs) {
                if (obj instanceof ObjectIdentifier) {
                    strs.add(ObjectUtils.getObjectValue((ObjectIdentifier) obj));
                } else {
                    strs.add(String.valueOf(obj));
                }
            }
            code = new Code(code, strs.toArray(new String[strs.size()]));
            throw new ServiceException(code.getCode(), code.getMessage(), ThreadContextHolder.getTransactionId(), data);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code -
     * @return void
     * @author Nyx
     * @date 2019/9/25 15:44
     */
    public static void check(final Code code, Object... codeArgs) {
        check(true, code, codeArgs);
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression expression
     * @param code       code
     * @param message    message
     * @author PlayBoy
     * @date 2018/7/30
     */
    public static void check(final boolean expression, final Integer code, final String message) {
        if (expression) {
            throw new ServiceException(code, message, ThreadContextHolder.getTransactionId());
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param expression
     * @param message
     * @return
     * @author PlayBoy
     * @date 2018/8/15
     */
    public static void check(final boolean expression, final String message) {
        if (expression) {
            throw new ServiceException(Code.ERROR_CODE, message, ThreadContextHolder.getTransactionId());
        }
    }

    public static void assertCheck(final boolean expression, final String message) {
        if (!expression) {
            throw new ServiceException(message);
        }
    }

    /**
     * description: if code is not success ,then throw ServiceException
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code
     * @param txId
     * @author PlayBoy
     * @date 2018/7/25
     */
    public static void isOK(final Code code, final String txId) {
        if (code == null) {
            throw new RuntimeException("CimCode is null");
        }
        if (!isSuccess(code)) {
            throw new ServiceException(code.getCode(), code.getMessage(), txId);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code code
     * @param txId txId
     * @param data data
     * @author PlayBoy
     * @date 2018/12/5 13:11:19
     */
    public static void isOK(final Code code, final String txId, final Object data) {
        if (code == null) {
            throw new RuntimeException("CimCode is null");
        }
        if (!isSuccess(code)) {
            throw new ServiceException(code.getCode(), code.getMessage(), txId, data);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param codeNum codeNum
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static boolean isSuccess(final Integer codeNum) {
        return Code.SUCCESS_CODE == codeNum;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code   code
     * @param target target
     * @return boolean
     * @author PlayBoy
     * @date 2018/11/5 18:04:40
     */
    public static boolean isEquals(Code code, Code target) {
        if (null == code || null == target) {
            return false;
        }
        return code.getCode() == target.getCode();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code
     * @param target -
     * @return boolean
     * @author Nyx
     * @date 2019/7/4 10:45
     */
    public static boolean isEquals(Code code, int target) {
        if (code == null) {
            return false;
        }
        return code.getCode() == target;
    }

    public static boolean isEquals(int src, Code code) {
        if (code == null) {
            return false;
        }
        return src == code.getCode();
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param code code
     * @author PlayBoy
     * @date 2018/6/29
     */
    public static boolean isSuccess(final Code code) {
        return code != null && Code.SUCCESS_CODE == code.getCode();
    }


    public static void isSuccessWithExceptionList(final Response response) {
        if (Code.SUCCESS_CODE != response.getCode()) {
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID(), response.getBody());
        }
    }

    public static void isSuccessWithException(final Response response) {
        if (response.getCode() == null) {
            throw new ServiceException("CimCode is null");
        }
        if (Code.SUCCESS_CODE != response.getCode()) {
            throw new ServiceException(response.getCode(), response.getMessage(), response.getTransactionID());
        }
    }

}
