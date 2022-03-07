package com.fa.cim.core;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptTest {

    private static Map<String,Class<?>> varTypes=new HashMap<>();

    private static final String testScript="// Test Case 1: Skip\n" +
            "var lot = $Factory.lot('lotID');\n" +
            "if (lot.productId() == \"PRODUCTPCS.01\" && lot.holdState() == \"NOTONHOLD\") {\n" +
            "\tlot.skipTo(\"1000.0400\");\n" +
            "\tlot.setOpeNote(\"1000.0400\",\"Eric PCS Test\",\"This a simple test of PSC.\");\n" +
            "}\n" +
            "\n" +
            "\n" +
            "// Test Case 2 : setEquipment\n" +
            "var lot = $Factory.lot(\"lotID\");\n" +
            "var temp = lot.stringValue(\"TEMP\");\n" +
            "temp.setValue(lot.operationNumber());\n";

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        List<String> varlines=getVarlines(testScript);
        print(varlines);
        for (String varline : varlines) {
            parseVarline(varline);
        }
        varTypesInvoke(testScript,varTypes);
    }

    private static void varTypesInvoke(String testScript,Map<String, Class<?>> varTypes) throws NoSuchMethodException {
        for (Map.Entry<String, Class<?>> varType : varTypes.entrySet()) {
            Pattern pattern=Pattern.compile(String.format("(?<!\\.)\\s*%s\\s*\\.\\s*\\w.*?(?===|;)",varType.getKey()));
            Matcher matcher=pattern.matcher(testScript);
            while (matcher.find()){
                String varStr=matcher.group(0);
                String _str=testScript.substring(0,matcher.start());
                Class<?> returnClass = parseReturn(varStr, varType.getValue());
                _varTypesInvoke(_str,returnClass);
            }
        }
    }

    private static void _varTypesInvoke(String str, Class<?> returnClass) throws NoSuchMethodException {
        Pattern pattern=Pattern.compile("(?<=var)\\s+\\w+(?=\\s*=\\s*$)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()){
            Map<String,Class<?>> varTypes=new HashMap<>();
            String var=matcher.group().trim();
            varTypes.put(var,returnClass);
            ScriptTest.varTypes.put(var,returnClass);
            varTypesInvoke(testScript,varTypes);
        }
    }

    private static void parseVarline(String varline) throws NoSuchMethodException {
        Pattern pattern=Pattern.compile("(?<=var)\\s+(\\w+)(?= *=)");
        Matcher matcher=pattern.matcher(varline);
        String var=null;
        if (matcher.find()){
            var=matcher.group(1);
        }
        Class<?> type=parseReturn(varline.replaceFirst("var\\s+(\\w+)\\s*=\\s*",""),null);
        print(var);
        varTypes.put(var,type);
    }

    private static Class<?> parseReturn(String rstr,Class<?> cls) throws NoSuchMethodException {
        print(rstr);
        Class<?> _cls=paramInVar(rstr,cls);
        if (_cls!=null){
            return _cls;
        }
        String[] strs=dorstr(rstr).split("\\.",2);
        if (cls==null){
            cls=ScriptRegister.fetch(strs[0]);
        }
        if (cls==null){
            cls=varTypes.get(strs[0]);
        }
        if (strs.length==1){
            Pattern pattern=Pattern.compile("(\\w+)\\s*\\((.*?)\\)");
            Matcher matcher=pattern.matcher(rstr);
            if (matcher.find()){
                String methodName=matcher.group(1);
                if (matcher.group(2).trim().length()==0){
                    return cls.getMethod(methodName).getReturnType();
                }
                String[] ss=matcher.group(2).split(",");
                Class<?>[] ptypes=new Class<?>[ss.length];
                for (int i=0;i<ss.length;i++) {
                    String s=ss[i];
                    if (s.trim().startsWith("'")||s.trim().startsWith("\"")){
                        ptypes[i]=String.class;
                    } else {
                        ptypes[i]=varTypes.get(s.trim());
                    }
                }
                return cls.getMethod(methodName,ptypes).getReturnType();
            }
            throw new NoSuchMethodException();
        }
        return parseReturn(strs[1],cls);
    }

    public static Class<?> paramInVar(String str,Class<?> cls) throws NoSuchMethodException {
        Pattern pattern = Pattern.compile("^(\\w+)\\s*\\(((.*?,)*.*)\\)");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.find()){
            return null;
        }
        String pstr=matcher.group(2);
        if (!pstr.contains(".")){
            return null;
        }
        String[] pvs=pstr.split("\\)\\s*,");
        Class<?>[] pcs=new Class<?>[pvs.length];
        int i=0;
        for (String pv : pvs) {
            pcs[i++]=parseReturn(pv+")",null);
        }
        return cls.getMethod(matcher.group(1),pcs).getReturnType();
    }

    private static String dorstr(String rstr) {
        Pattern pattern=Pattern.compile("(?<=\").*?(?=\")");
        Matcher matcher=pattern.matcher(rstr);
        int i=0;
        while (matcher.find()){
            String s=matcher.group();
            if (!",".equals(s.trim())) {
                rstr=rstr.substring(0,matcher.start()+i)+rstr.substring(matcher.end()+i);
                i-=s.length();
            }
        }
        return rstr;
    }

    public static void print(Object obj){
        System.out.println(obj);
    }

    private static List<String> getVarlines(String testScript) {
        List<String> varlines=new ArrayList<>();
        Pattern pattern=Pattern.compile("\\bvar\\b.*\\$\\w+ *\\..*?;");
        Matcher matcher=pattern.matcher(testScript);
        while (matcher.find()){
            varlines.add(matcher.group());
        }
        return varlines;
    }

}
