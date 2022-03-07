package com.fa.cim.listener;

import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.memorydata.MailMemoryData;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/2/20 9:20
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
public class Startlistener implements InitializingBean {

    @Autowired
    private MailMemoryData mailMemoryData;

    @Autowired
    private GitLabApi gitLabApi;

    @Override
    public void afterPropertiesSet() {
        mailMemoryData.init();
        initProjectID();
    }

    private void initProjectID() {
        List<Project> projects = null;
        try {
            projects = gitLabApi.getProjectApi().getProjects();
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
        int projectID;
        if (!CimArrayUtils.isEmpty(projects)){
            for (Project project : projects){
                if (project.getName().equals("myCIM4.0")){
                    projectID = project.getId();
                    mailMemoryData.setProjectID(projectID);
                    break;
                }
            }
        }
    }
}