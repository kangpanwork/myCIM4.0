package com.fa.cim.common.exception;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/5/1        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/5/1 9:44
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public class DuplicateException extends Exception {
    static final long serialVersionUID = -7446455436142562135L;

    /**
     * description:
     *      Constructs a new exception with {@code null} as its detail message.
     *      The cause is not initialized, and may subsequently be initialized by a
     *      call to {@link #initCause}.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/1
     * @param  -
     * @return
     */
    public DuplicateException() {
        super();
    }

    /**
     * description:
     *      Constructs a new exception with the specified detail message.
     *      The cause is not initialized, and may subsequently be initialized by
     *      a call to {@link #initCause}.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/1
     * @param message - the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
     * @return
     */
    public DuplicateException(String message) {
        super(message);
    }

    /**
     * description:
     *      Constructs a new exception with the specified detail.
     *      message and cause.<p>Note that the detail message associated with @code cause} is <i>not</i> automatically incorporated in this exception's detail message.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/1
     * @param message - the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause - cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *      (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @return
     */
    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * description:
     *      Constructs a new exception with the specified cause and a detail
     *      message of <tt>(cause==null ? null : cause.toString())</tt> (which
     *      typically contains the class and detail message of <tt>cause</tt>).
     *      This constructor is useful for exceptions that are little more than
     *      wrappers for other throwables (for example, {@link
     *      java.security.PrivilegedActionException}).
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/1
     * @param cause - the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *       (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @return
     */
    public DuplicateException(Throwable cause) {
        super(cause);
    }

    /**
     * description:
     *      Constructs a new exception with the specified detail message,
     *      cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Bear
     * @date 2018/5/1
     * @param message
 * @param cause
 * @param enableSuppression
 * @param writableStackTrace - whether or not the stack trace should be writable
     * @return
     */
    protected DuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
