package pers.sharedFileSystem.networkManager;


import pers.sharedFileSystem.communicationObject.FingerprintInfo;
import pers.sharedFileSystem.communicationObject.RedundancyFileStoreInfo;
import pers.sharedFileSystem.logManager.LogRecord;
import pers.sharedFileSystem.systemFileManager.FingerprintAdapter;
import pers.sharedFileSystem.systemFileManager.RedundantFileAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件系统存储端，运行在每个存储服务器上面
 */
public class FileSystemStore {

    private static ConcurrentHashMap<String,ArrayList<FingerprintInfo>> redundancyFileMap;

    /**
     * 添加冗余信息
     * @param r
     */
    public static boolean addRedundancyFileStoreInfo(RedundancyFileStoreInfo r){
        ArrayList<FingerprintInfo>fingerprintInfos=redundancyFileMap.get(r.essentialStorePath);
        if(fingerprintInfos==null){
            fingerprintInfos=new ArrayList<FingerprintInfo>();
        }
        for(FingerprintInfo f:r.otherFileInfo)
            fingerprintInfos.add(f);
        redundancyFileMap.put(r.essentialStorePath,fingerprintInfos);
        boolean re=RedundantFileAdapter.saveRedundancyFileStoreInfo(redundancyFileMap);
        return  re;
    }
    /**
     * 初始化
     */
    private void initServerSocket(){
        ConnWatchDog connWatchDog = new ConnWatchDog();
        Thread connWatchDogThread = new Thread(connWatchDog);
        connWatchDogThread.start();
        List<RedundancyFileStoreInfo>redundancyFileStoreInfos=RedundantFileAdapter.getAllRedundancyFileStoreInfo();
        redundancyFileMap=new ConcurrentHashMap<String,ArrayList<FingerprintInfo>>();
        for(RedundancyFileStoreInfo r:redundancyFileStoreInfos){
            redundancyFileMap.put(r.essentialStorePath,r.otherFileInfo);
        }
        LogRecord.RunningInfoLogger.info("load RedundancyFileStoreInfo successful. total= "+redundancyFileStoreInfos.size());
    }

    public FileSystemStore() {
        initServerSocket();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new FileSystemStore();
    }
}
