package pers.sharedFileSystem.systemFileManager;

import pers.sharedFileSystem.communicationObject.RedundancyFileStoreInfo;
import pers.sharedFileSystem.configManager.Config;
import pers.sharedFileSystem.convenientUtil.CommonUtil;
import pers.sharedFileSystem.communicationObject.FingerprintInfo;
import pers.sharedFileSystem.entity.SystemConfig;
import pers.sharedFileSystem.logManager.LogRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指纹信息文件操作类（文件元数据操作管理类）
 */
public class FingerprintAdapter {
    private static SystemConfig sysConfig=Config.SYSTEMCONFIG;
    /**
     * 按照序列化的方式将文件指纹存储信息保存到磁盘(保存全部信息)
     */
    public static boolean saveAllFingerprint(ConcurrentHashMap<String,FingerprintInfo> fingerprintInfoMap){
        FileOutputStream fout=null;
        ObjectOutputStream sout =null;
        String filePath=sysConfig.FingerprintStorePath;//指纹信息的保存路径
        String fileName=sysConfig.FingerprintName;
        if(!CommonUtil.validateString(filePath)){
            LogRecord.FileHandleErrorLogger.error("save Fingerprint error, filePath is null.");
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() && !file.isDirectory()) {
            LogRecord.RunningErrorLogger.error("save Fingerprint error, filePath illegal.");
            return false;
        }
        File oldFile=new File(filePath+"/"+fileName);
        File tempFile=new File(filePath+"/temp.sys");
        if(oldFile.exists()){
            oldFile.renameTo(tempFile);
        }

        try{
            fout = new FileOutputStream(filePath + "/" + fileName, true);
            int num=0;
            for(FingerprintInfo val:fingerprintInfoMap.values()) {
                sout = new ObjectOutputStream(fout);
                sout.writeObject(val);
                num++;
            }
            LogRecord.RunningInfoLogger.info("save Fingerprint successful. total="+num);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            File newFile=new File(filePath+"/"+fileName);
            newFile.delete();
            tempFile.renameTo(newFile);
            return false;
        }finally {
            try {
                //删除临时文件
                if(tempFile.exists()){
                    tempFile.delete();
                }
                if(fout!=null)
                    fout.close();
                if(sout!=null)
                    sout.close();
            } catch (IOException e) {
                e.printStackTrace();
                return  false;
            }
        }
        return true;
    }

    /**
     * 按照序列化的方式将指纹信息保存到磁盘
     * @param fingerprintInfo 待保存的指纹信息
     */
    public static boolean saveFingerprint(FingerprintInfo fingerprintInfo){
        FileOutputStream fout=null;
        ObjectOutputStream sout =null;
        String filePath=sysConfig.FingerprintStorePath;//指纹信息的保存路径
        String fileName=sysConfig.FingerprintName;
        if(!CommonUtil.validateString(filePath)){
            LogRecord.FileHandleErrorLogger.error("save Fingerprint error, filePath is null.");
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() && !file.isDirectory()) {//如果系统文件夹不存在
            LogRecord.RunningErrorLogger.error("save Fingerprint error, filePath illegal.");
            return false;
        }
        try{
            fout= new FileOutputStream(filePath+"/"+fileName, true);
            sout= new ObjectOutputStream(fout);
            sout.writeObject(fingerprintInfo);
            LogRecord.RunningInfoLogger.info("save Fingerprint successful. [ " + fingerprintInfo+" ]");
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }finally {
                try {
                    if(fout!=null)
                        fout.close();
                    if(sout!=null)
                        sout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    /**
     * 根据被保存文件的目的节点编号和指纹信息查找该指纹信息对应的文件存储信息
     * @param md5 指纹信息
     * @return 该指纹信息对应的文件存储信息
     */
    public static FingerprintInfo getFingerprintInfoByMD5(String md5){
        String filePath=sysConfig.FingerprintStorePath;//指纹信息的保存路径
        String fileName=sysConfig.FingerprintName;
        FileInputStream fin = null;
        BufferedInputStream bis =null;
        ObjectInputStream oip=null;
        if(!CommonUtil.validateString(filePath)){
            LogRecord.FileHandleErrorLogger.error("get Fingerprint error, filePath is null.");
            return null;
        }
        File file = new File(filePath);
        if (!file.isDirectory()||!new File(filePath+"/"+fileName).exists())
            return null;//如果系统文件夹不存在或者指纹信息文件不存在
        try{
            fin = new FileInputStream(filePath+"/"+fileName);
            bis = new BufferedInputStream(fin);
            while (true) {
                try {
                    oip = new ObjectInputStream(bis); // 每次重新构造对象输入流
                }catch (EOFException e) {
                    // e.printStackTrace();
//                    System.out.println("已达文件末尾");// 如果到达文件末尾，则退出循环
                    return null;
                }
                Object object = oip.readObject();
                if (object instanceof FingerprintInfo) { // 判断对象类型
                    FingerprintInfo tmp=(FingerprintInfo)object;
                    if(tmp.getMd5().equals(md5))
                        return tmp;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(oip!=null)
                    oip.close();
                if(bis!=null)
                    bis.close();
                if(fin!=null)
                    fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 按照序列化的方式获取指纹信息
     * @return
     */
    public static List<FingerprintInfo>getAllFingerprintInfo(){
        List<FingerprintInfo>fingerprintInfos=new ArrayList<FingerprintInfo>();
        FileInputStream fin = null;
        BufferedInputStream bis =null;
        ObjectInputStream oip=null;
        String filePath=sysConfig.FingerprintStorePath;//指纹信息的保存路径
        String fileName=sysConfig.FingerprintName; // 指纹信息文件名
        if(!CommonUtil.validateString(filePath)){
            LogRecord.FileHandleErrorLogger.error("get Fingerprint error, filePath is null.");
            return fingerprintInfos;
        }
        File file = new File(filePath);
        String fullFilePath = filePath+"/"+fileName;
        if (!file.isDirectory()||!new File(fullFilePath).exists()) {
            LogRecord.FileHandleErrorLogger.error("file not found: "+fullFilePath);
            return fingerprintInfos;//如果系统文件夹不存在或者指纹信息文件不存在
        }
        try{
            fin = new FileInputStream(fullFilePath);
            bis = new BufferedInputStream(fin);
            while (true) {
                try {
                    oip = new ObjectInputStream(bis); // 每次重新构造对象输入流
                }catch (EOFException e) {
                    // e.printStackTrace();
//                    System.out.println("已达文件末尾");// 如果到达文件末尾，则退出循环
                    return fingerprintInfos;
                }
                Object object =  oip.readObject();
                if (object instanceof FingerprintInfo) { // 判断对象类型
                    fingerprintInfos.add((FingerprintInfo) object);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if(oip!=null)
                    oip.close();
                if(bis!=null)
                    bis.close();
                if(fin!=null)
                    fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fingerprintInfos;
    }

}