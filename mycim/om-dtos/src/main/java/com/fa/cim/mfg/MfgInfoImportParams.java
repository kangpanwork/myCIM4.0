package com.fa.cim.mfg;

import com.fa.cim.basic.BasicImportParam;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/5/8        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/5/8 14:38
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class MfgInfoImportParams extends BasicImportParam {

    private MultipartFile file;

}