package com.fa.cim.dto;

import lombok.Data;

import java.sql.Timestamp;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 14:33:23
 */
public class Params {

    @Data
    public static class EmailInfo{
        private String nickName;
        private String username;
        private String password;
        private String mailType;
        private Timestamp updateTime;
    }

    @Data
    public static class EmailForm{
        private String nickName;
        private String userName;
        private String password;
        private String emailType;
    }
}
