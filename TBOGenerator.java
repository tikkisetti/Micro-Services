package com.jci.utils;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.IDfAttr;
import com.jci.common.util.BaseUtils;
import com.jci.common.util.PropertyManager;
import com.jci.common.util.job.JobUtils;
import com.jci.common.util.bpm.BPMUtils;
import org.apache.commons.lang.StringUtils;
import org.dctmutils.common.SessionHelper;

import java.util.StringTokenizer;
import java.util.Hashtable;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by IntelliJ IDEA.
 * User: ctikkis
 * Date: Sep 24, 2007
 * Time: 10:16:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class TBOGenerator {

    static BaseUtils baseUtils = new BaseUtils();
    static BPMUtils bpmUtils = new BPMUtils();
    static JobUtils jobUtils = new JobUtils();
    static IDfSession session = null;
    static IDfSessionManager sMgr = null;
    static PropertyManager propsMgr=PropertyManager.getManager("FileTemplate");
    static String implFile = null,interfaceFile=null,className=null,interfaceName = null;
    static String objectType = "be_intracompany_document";
    IDfType typeObj = null;
    static BufferedWriter interfaceClass = null;
    static BufferedWriter implementationClass = null;
    
    public TBOGenerator() throws Exception {
        intiliaze();
    }

    public void intiliaze() throws Exception {
    	System.out.println("Initialize");
        implFile=propsMgr.getString("IMPL_FILE_HEADER");
        interfaceFile=propsMgr.getString("INTERFACE_TEMPLATE");
        className=getTransformedName(objectType)+"TBO";
        interfaceName="I"+className;
        System.out.println("Creating session");
        sMgr = SessionHelper.getSessionManager("BE", "User01", "User01");
		session = sMgr.getSession("BE");
        
        System.out.println("Session created");
//        sMgr = jobUtils.connectToDocbase("CORPORATE", "dctmconnect", "Dctmcnt1");
//         session = sMgr.getSession("CORPORATE");
        typeObj = session.getType(objectType);
        System.out.println("Type is : "+typeObj.getName());
        interfaceClass=new BufferedWriter(new FileWriter("c:/temp/"+interfaceName+".java"));
        implementationClass =new BufferedWriter(new FileWriter("c:/temp/"+className+".java"));
    }

    public static void main(String[] args) throws Exception{




        try {
            System.out.println("Main method!!!!!");
            TBOGenerator tboGenerator = new TBOGenerator();
            tboGenerator.generateInterface(objectType);
            tboGenerator.generateClass(objectType);
        } catch (Exception dfe) {
            dfe.printStackTrace();
        } finally {
            sMgr.release(session);

            interfaceClass.close();
            implementationClass.close();
            System.out.println("Cleanup done");
        }
    }

    public void generateClass(String objectType) throws Exception {

        Hashtable tokenTable = new Hashtable();
        tokenTable.put("TOKEN_CLASS_NAME",className);
        tokenTable.put("TOKEN_INTERFACE_NAME",interfaceName);

        String otherMethods="";
        for (int i = typeObj.getInt("start_pos"); i < typeObj.getTypeAttrCount(); i++) {
            String otherMethodsTemp= propsMgr.getString("IMPL_OTHER_METHODS_TEMPLATE");
            //System.out.println("Other methods is "+otherMethodsTemp);
            Hashtable hashTable=new Hashtable();
            IDfAttr attrObj = typeObj.getTypeAttr(i);
            //System.out.println("Attribute Name: " + attrObj.getName());
            //System.out.println("Attribute Type: "+attrObj.getDataType());
            //System.out.println("Description is : "+attrObj.);
            String transformedAttr = getTransformedName(attrObj.getName());
            String dataType=getDataType(attrObj.getDataType());
            String capsAttribute= attrObj.getName().toUpperCase();
            hashTable.put("TOKEN_ATTR_NAME",capsAttribute);
            hashTable.put("TOKEN_ATTR_VALUE",StringUtils.uncapitalize(transformedAttr));
            hashTable.put("TOKEN_GET_METHOD_NAME","get"+transformedAttr);
            hashTable.put("TOKEN_SET_METHOD_NAME","set"+transformedAttr);
            hashTable.put("TOKEN_DATA_TYPE",dataType);
            hashTable.put("TOKEN_DATA_NAME",capsAttribute);
            hashTable.put("TOKEN_GET_DATE_TYPE","get"+StringUtils.capitalize(dataType));
            hashTable.put("TOKEN_SET_DATE_TYPE","set"+StringUtils.capitalize(dataType));


            otherMethodsTemp = propsMgr.replaceTokens(otherMethodsTemp,hashTable);
            otherMethods=otherMethods+"\n"+ otherMethodsTemp;
            //System.out.println("Transformed String is : "+otherMethods);

        }
        System.out.println("Setter methods "+otherMethods);
        tokenTable.put("TOKEN_GETTER_SETTER_METHODS",otherMethods);

        implFile=propsMgr.replaceTokens(implFile,tokenTable);
         System.out.println("File is "+implFile);
        implementationClass.write(implFile);

    }

    public void generateInterface(String objectType)throws Exception{
        Hashtable tokenTable = new Hashtable();
        tokenTable.put("TOKEN_INTERFACE_NAME",interfaceName);
        String otherMethods="";
        String attributeDefinitions = "";
        for (int i = typeObj.getInt("start_pos"); i < typeObj.getTypeAttrCount(); i++) {
            String otherMethodsTemp= propsMgr.getString("INTERFACE_OTHER_METHODS_TEMPLATE");
            //System.out.println("Other methods is "+otherMethodsTemp);
            Hashtable hashTable=new Hashtable();
            IDfAttr attrObj = typeObj.getTypeAttr(i);
            //System.out.println("Attribute Name: " + attrObj.getName());
            //System.out.println("Attribute Type: "+attrObj.getDataType());
            //System.out.println("Description is : "+attrObj.);
            String transformedAttr = getTransformedName(attrObj.getName());
            System.out.println("TransformedAttr : "+transformedAttr);
            String capsAttribute= attrObj.getName().toUpperCase();
            attributeDefinitions =attributeDefinitions+"\npublic static final String "+capsAttribute+" = \""+attrObj.getName()+"\";";
            String dataType=getDataType(attrObj.getDataType());
            hashTable.put("TOKEN_GET_METHOD","get"+transformedAttr);
            hashTable.put("TOKEN_SET_METHOD","set"+transformedAttr);
            hashTable.put("TOKEN_DATA_TYPE",dataType);
            hashTable.put("TOKEN_ATTRIBUTE_NAME",StringUtils.uncapitalize(transformedAttr));
            otherMethodsTemp = propsMgr.replaceTokens(otherMethodsTemp,hashTable);
            otherMethods=otherMethods+"\n"+ otherMethodsTemp;
            //System.out.println("Transformed String is : "+otherMethods);

        }
        tokenTable.put("TOKEN_ATTR_DEFINITIONS",attributeDefinitions);
        tokenTable.put("TOKEN_GET_SET_METHODS",otherMethods);
        interfaceFile=propsMgr.replaceTokens(interfaceFile,tokenTable);
         System.out.println("Interface File is :\n"+interfaceFile);
         interfaceClass.write(interfaceFile);
    }

    public static String getTransformedName(String objectName) {
        String transformedAttr = "";
        if (objectName != null && StringUtils.contains(objectName, "_")) {
            StringTokenizer st = new StringTokenizer(objectName, "_");

            while (st.hasMoreTokens()) {
               transformedAttr=transformedAttr+StringUtils.capitalize(st.nextToken());

            }
        } else{
           transformedAttr=transformedAttr+StringUtils.capitalize(objectName); 
        }
        return transformedAttr;

    }

    public static String getDataType(int iDataType){

        String dataType = null;
        switch (iDataType) {
                    case 0:  {dataType="boolean"; break;}
                    case 1:  {dataType="int"; break;}
                    case 2:  {dataType="String"; break;}
                    case 3:  {dataType="ID"; break;}
                    case 4:  {dataType="Date"; break;}
                    case 5:  {dataType="Double"; break;}


                }

        return dataType;
    }


}
