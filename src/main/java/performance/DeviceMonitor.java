package performance;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.logging.LogEntries;
import util.ConfigUtil;
import util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

public class DeviceMonitor {

    //Android Performance Data type
    public static final String cpuinfo= "cpuinfo";
    public static final String memoryinfo= "memoryinfo";
    public static final String batteryinfo= "batteryinfo";
    public static final String networkinfo= "networkinfo";
    public static final String nativeKey = "Native Heap";
    public static final String dalKey = "Dalvik Heap";
    public static final String totalKey = "TOTAL";

    // ========================================Android========================================

    // same result as "adb logcat"
    public void startTrackAndroidLogCat(AndroidDriver driver) {
        LogEntries logs= driver.manage().logs().get("logcat");
        logs.filter(Level.WARNING);
        StreamSupport.stream(logs.spliterator(), false).limit(10).forEach(System.out::println);
        System.out.println("...");
        StreamSupport.stream(logs.spliterator(), false).skip(logs.getAll().size() - 10).forEach(System.out::println);
    }

    public void stopTrackAndroidLogCat(AndroidDriver driver){
        // demonstrate that each time get logs, we only get new logs
        // which were generated since the last time we got logs
        LogEntries secondCallToLogs = driver.manage().logs().get("logcat");
        System.out.println("\nFirst ten lines of next log call: ");
        StreamSupport.stream(secondCallToLogs.spliterator(), false).limit(10).forEach(System.out::println);
    }


    private HashMap<String, Integer> getAndroidPerformanceData(AndroidDriver driver, String dataType) throws Exception {
        List<List<Object>> data = driver.getPerformanceData("com.kroger.mobile", dataType, 10);
        HashMap<String, Integer> readableData = new HashMap<>();
        for (int i = 0; i < data.get(0).size(); i++) {
            int val;
            if (data.get(1).get(i) == null) {
                val = 0;
            } else {
                val = Integer.parseInt((String) data.get(1).get(i));
            }
            readableData.put((String) data.get(0).get(i), val);
        }
        return readableData;
    }

    private static String getMemString(String output,String key){
        int index = output.indexOf(key) + key.length();
        return output.substring(index , index + 9).trim();
    }

    private static Map<String,String> getMemoryInfo(String udid, String packageName, boolean enableCmdOutput){

        Map<String,String> map = new HashMap<>();

        String res = Util.exeCmd("adb -s " + udid + " shell dumpsys meminfo " + packageName , enableCmdOutput);

        if(res.contains("No process found for")){
            return map;
        }

        String nativeMem = getMemString(res,nativeKey);

        String dalvikMem = getMemString(res,dalKey);

        String totalMem = getMemString(res,totalKey);

        map.put(nativeKey,nativeMem);
        map.put(dalKey,dalvikMem);
        map.put(totalKey,totalMem);

        return map;
    }

    private static String getCPUInfo(String udid, String grep, String name, boolean enableCmdOutput){
        String info;
        //String cmd = "adb -s " + udid + " shell dumpsys cpuinfo " + packageName + " | " + grep + " "+ packageName;
        String cmd = "adb -s " + udid + "  shell top -n 1 | " + grep + " " + name;

        String res = Util.exeCmd(cmd, enableCmdOutput);

        if(res.isEmpty()){
            return "";
        }

        String []val = res.split(" ");

        int CPU_INDEX = 9;
        int index = 0;

        for( int i = 0; i < val.length ; i ++){
            if(!val[i].isEmpty()){
                index ++;
            }
            if(index == CPU_INDEX){
                index = i;
                break;
            }
        }
        info = val[index];
        return info;
    }

    // ========================================iOS========================================
    public void filterIOSLogcat(){
        File logs =new File("");
    }

    //Activity Monitor
    //me ProfilerTi
    private void startToRecordIOSData(IOSDriver driver){
        HashMap<String, Object> args = new HashMap<>();
        args.put("pid", "current");
        args.put("profileName", "Time Profiler");
        args.put("timeout", 60000);
        driver.executeScript("mobile: startPerfRecord", args);
    }

    private void stopRecordIOSData(IOSDriver driver){
        HashMap<String, Object> args = new HashMap<>();
        args.put("pid", "current");
        args.put("profileName", "Time Profiler");
        args.put("timeout", 60000);

        File traceZip = new File("/path/to/trace.zip");
        String b64Zip = (String)driver.executeScript("mobile: stopPerfRecord", args);
        byte[] bytesZip = Base64.getMimeDecoder().decode(b64Zip);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(traceZip);
            stream.write(bytesZip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
