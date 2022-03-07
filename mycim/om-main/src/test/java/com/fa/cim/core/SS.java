package com.fa.cim.core;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fa.cim.common.support.OmPage;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimPageUtils;
import com.fa.cim.jpa.SearchCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.lang.reflect.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/2/12 14:54
 */
public class SS implements FileFilter, MethodInterceptor,Comparator<SS.Outer> {

    private static final String bg="com/fa/cim/controller";

    private static final List<Class<?>> bgCs=new ArrayList<>();

    private static final ClassLoader cl;

    private static final SS ss=new SS();

    private static final String rp;

    private static final int si;

    private static final int ei=6;

    private static final Map<String,List<Outer>> outs=new HashMap<>();

    private static Class<?> c;

    private static Outer outer;

    private static List<Outer> outers;

    private static int counter=0;

    private static final String ramr="D:\\workspaces\\fa\\test";

    private static PrintWriter apw;

    private static String traml;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Class<?> returnType = method.getReturnType();
        return tco(returnType, method.getGenericReturnType());
    }

    @Override
    public int compare(Outer o1, Outer o2) {
        return o1.url.compareToIgnoreCase(o2.url);
    }

    static class Outer{
        String url;
        String out;
        String in;
    }

    static {
        cl= SS.class.getClassLoader();
        rp=cl.getResource(bg).getPath();
        si=rp.length()-bg.length()-1;
        ccs(new File(rp));
        File file=new File(ramr, "t.txt");
        BufferedReader reader=null;
        try {
            reader=new BufferedReader(new FileReader(file));
            String tmp,t="";
            while ((tmp=reader.readLine())!=null){
                t+=tmp+'\n';
            }
            traml=t;
        } catch (Exception e) {
            /*throw new RuntimeException(e);*/
        } finally {
            if (reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void ccs(File file){
        if (file.isFile()){
            try {
                bgCs(file.getAbsolutePath());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
        Arrays.stream(file.listFiles(ss)).forEach(file1 -> ccs(file1));
    }

    private static void bgCs(String absolutePath) throws ClassNotFoundException {
        String cp=absolutePath.substring(si,absolutePath.length()-ei).replaceAll("\\\\",".");
        c = cl.loadClass(cp);
        pcs();
    }

    private static void pcs() {
        RequestMapping requestMapping = c.getAnnotation(RequestMapping.class);
        if (requestMapping==null)
            return;
        bgCs.add(c);
    }

    private static void push(String key){
        outers = outs.get(key);
        if (outers==null){
            outs.put(key, outers=new ArrayList<>());
        }
    }

    private static void ik(Class<?> c){
        SS.c=c;
        RequestMapping requestMapping = c.getAnnotation(RequestMapping.class);
        push(requestMapping.value()[0]);
        Arrays.stream(c.getDeclaredMethods()).forEach(SS::mk);
    }

    private static Object newInstance(Class<?> cl) throws IllegalAccessException, InstantiationException {
        if (cl==String.class){
            return "1";
        }else if (cl==Boolean.class||cl==boolean.class){
            return true;
        }else if (cl== Timestamp.class){
            return Timestamp.valueOf("1990-01-01 00:00:00");
        }else if (cl==Double.class||cl==double.class){
            return 0D;
        }else if (cl==Long.class||cl==long.class){
            return 0L;
        }else if (cl==int.class||cl==Integer.class){
            return 0;
        }else if (cl==void.class){
            return null;
        }else if (cl== SearchCondition.class){
            return new SearchCondition();
        }else if (cl==Object[].class){
            return new Object[100];
        }else if (cl==Object.class){
            return "";
        }
        Object o=null;
        try{
            o=cl.newInstance();
        }catch (Exception e){
            try {
                Constructor<?> declaredConstructor = cl.getConstructors()[0];
                Object[] args=new Object[declaredConstructor.getParameterTypes().length];
                int i=0;
                for (Class<?> parameterType : declaredConstructor.getParameterTypes()) {
                    args[i++]=newInstance(parameterType);
                }
                o=declaredConstructor.newInstance(args);
            } catch (InvocationTargetException ex) {
                throw new IllegalAccessException(ex.getMessage());
            }
        }
        for (Field declaredField : cl.getDeclaredFields()) {
            int modifiers = declaredField.getModifiers();
            if (Modifier.isStatic(modifiers))
                continue;
            declaredField.setAccessible(true);
            if (declaredField.getAnnotation(Autowired.class)!=null)
                declaredField.set(o,pcl(declaredField.getType()));
            else {
                Class<?> type = declaredField.getType();
                declaredField.set(o,tco(type,declaredField.getGenericType()));
            }
        }
        return o;
    }

    private static Object tco(Class<?> type,Type genericType) throws InstantiationException, IllegalAccessException {
        if (type==List.class||type==Page.class|| type==OmPage.class){
            List a=Arrays.asList(newInstance((Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[0]));
            if (type==List.class)
                return a;
            else if (type==Page.class)
                return CimPageUtils.convertListToPage(a,null,null);
            else
                return new OmPage(CimPageUtils.convertListToPage(a,null,null));
        }else if (type==Map.class){
            Map map=new HashMap();
            map.put("key",newInstance((Class<?>) ((ParameterizedType)genericType).getActualTypeArguments()[1]));
            return map;
        }else {
            return newInstance(type);
        }
    }

    private static Object pcl(Class<?> cl){
        Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(cl);
        enhancer.setCallback(ss);
        return enhancer.create();
    }

    private static void mk(Method m) {
        RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
        if (requestMapping==null||m.getParameterTypes().length==0)
            return;
        outer=new Outer();
        outers.add(outer);
        String value = requestMapping.value()[0];
        outer.url=value;
        try {
            Object p = newInstance(m.getParameterTypes()[0]);
            outer.in=json(p);
            Object r = m.invoke(newInstance(c), p);
            if (r==null)
            r=newInstance(Response.class);
            outer.out=json(r);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static String json(Object cl){
        return JSONObject.toJSONString(cl, SerializerFeature.PrettyFormat);
    }

    private static void invoke(){
        bgCs.forEach(SS::ik);
    }

    public static void main(String[] args) throws FileNotFoundException {
        invoke();
        try {
            ist();
            psd();
            System.out.println(String.format("Finished api count = %d",counter));
        } finally {
            cst();
        }
    }

    private static void cst() {
        apw.close();
    }

    private static void ist() throws FileNotFoundException {
        File api=new File(ramr,"api.raml");
        apw=new PrintWriter(new FileOutputStream(api));
    }

    private static void print(String str){
        apw.print(str);
    }

    private static void apiram(String k, List<Outer> o) {
        File file=new File(ramr+"/api",k+"/json");
        file.mkdirs();
        print(String.format("#%1$s\n" +
                "%1$s:\n" +
                "  displayName: %1$s\n",k));
        String pf=null;
        for (Outer outer1 : o) {
            String[] args=durl(outer1.url).split("/",3);
            iraml(durl(outer1.url).substring(1).replace("/","_"),file,outer1);
            args[0]=k;
            args[2]=args[2].replaceAll("/","_");
            if (args[1].equals(pf)){
                print(String.format("    /%3$s: !include api%1$s/%2$s_%3$s.raml\n",args));
                continue;
            }
            print(String.format("  /%2$s:\n" +
                    "    /%3$s: !include api%1$s/%2$s_%3$s.raml\n",args));
            pf=args[1];
        }
    }

    private static void iraml(String durl, File file, Outer outer1) {
        wjs(new File(file,durl+"_in.json"),outer1.in.replaceAll("(?<=\")1(?=\")","string"));
        wjs(new File(file,durl+"_out.json"),outer1.out.replaceAll("(?<=\")1(?=\")","string"));
        wjs(new File(file.getParentFile(),durl+".raml"),String.format(traml,durl));
    }

    private static void wjs(File file,String str){
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(file);
            fos.write(str.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String durl(String url){
        return url.startsWith("/")?url:"/"+url;
    }

    private static void psd() {
        outs.forEach(SS::meo);
    }

    private static void meo(String k,List<Outer> o){
        counter+=o.size();
        o.sort(ss);
        apiram(k,o);
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory()||pathname.getName().endsWith(".class");
    }
}
