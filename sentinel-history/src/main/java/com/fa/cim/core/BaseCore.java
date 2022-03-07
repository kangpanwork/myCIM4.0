package com.fa.cim.core;

import com.fa.cim.utils.ArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/26 17:31:19
 */
@Slf4j
@Component
public class BaseCore {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sql
     * @param args
     * @return com.fa.cim.Custom.List<java.lang.Object[]>
     * @exception
     * @author Ho
     * @date 2019/3/11 14:42
     */
    public List<Object[]> queryAll(String sql,Object...args) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (int i = 0, n = ArrayUtils.length(args);i<n;i++){
            nativeQuery.setParameter(i+1,args[i]);
        }
        return (List<Object[]>) nativeQuery.getResultList();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sql
     * @param args
     * @return com.fa.cim.Custom.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @exception
     * @author Ho
     * @date 2019/4/19 16:30
     */
    public List<Map> queryAllForMap(String sql,Object...args) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        nativeQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (int i = 0, n = ArrayUtils.length(args);i<n;i++){
            nativeQuery.setParameter(i+1,args[i]);
        }
        return (List<Map>) nativeQuery.getResultList();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sql
     * @param args
     * @return java.lang.Object[]
     * @author Ho
     * @date 2019/2/26 17:35:05
     */
    public Object[] queryOne(String sql,Object...args) {
        List<Object[]> objects = queryAll(sql, args);
        if (length(objects)==0) {
            return null;
        }
        return objects.get(0);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sql
     * @param args
     * @return int
     * @exception
     * @author Ho
     * @date 2019/6/15 15:17
     */
    public int count(String sql,Object...args) {
        List<Object[]> objects = queryAll(sql, args);
        if (length(objects)==0) {
            return 0;
        }
        if (objects.get(0) instanceof Object[]) {
            return convertI(objects.get(0)[0]);
        }
        return convertI(objects.get(0));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param sql
     * @param args
     * @return void
     * @exception
     * @author Ho
     * @date 2019/3/11 14:41
     */
    public void insert(String sql,Object...args) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (int i=0,n=ArrayUtils.length(args);i<n;i++){
            if (args[i]==null) {
                nativeQuery.setParameter(i+1,"");
            }else{
                nativeQuery.setParameter(i+1,args[i]);
            }
        }
        nativeQuery.executeUpdate();
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param srcTable
     * @param targetTable
     * @param event
     * @param id
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 14:40
     */
    public void insertChildTable(String srcTable,String targetTable,String event, String id) {
        insert(String.format("INSERT INTO %s SELECT * FROM %s WHERE REFKEY =?",targetTable,srcTable),event);
        insert(String.format("UPDATE %s SET REFKEY =? WHERE REFKEY =?",targetTable),id,event);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param srcTable
     * @param targetTable
     * @param event
     * @return java.lang.String
     * @exception
     * @author ho
     * @date 2020/7/31 14:42
     */
    public String insertPrimaryTable(String srcTable,String targetTable,String event) {
        insert(String.format("INSERT INTO %s SELECT * FROM %s WHERE ID =?",targetTable,srcTable),event);
        String id=generateID("OHSEASON");
        insert(String.format("UPDATE %s SET ID =? WHERE ID =?",targetTable),id,event);
        return id;
    }

}
