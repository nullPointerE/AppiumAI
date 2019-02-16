package util;

import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.functions.AppiumFunction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public final class Driver {
    public static org.slf4j.Logger log = LoggerFactory.getLogger(Driver.class);

    public static AppiumDriver driver;
    private static int deviceHeight;
    private static int deviceWidth;
    private static final int APP_START_WAIT_TIME = 20;
    private static int screenshotCount = 0;

    public static void setDriver(AppiumDriver driverRef) {
        driver = driverRef;
    }

    public static AppiumDriver getDriver() {
        return driver;
    }

    public static String takeScreenShot(String screenShotName) {
        //等待1秒再截图，不然界面还在变化，载图不是完整初始化后的页面
        sleep(1);

        File screenShot = null;

        try {

            if (Util.isAndroid()) {
                screenShot = driver.getScreenshotAs(OutputType.FILE);
            } else {
                String fileName = ConfigUtil.getRootDir() + File.separator + "del.png";
                screenShot = new File(fileName);
                Util.exeCmd("idevicescreenshot -u " + ConfigUtil.getUdid() + " " + fileName);
            }

            log.info("screenShotName: " + screenShotName);

            FileUtils.copyFile(screenShot, new File(screenShotName));

            if (++screenshotCount % 5 == 0) {
                log.info("Screenshot count is " + screenshotCount);
                String path = ConfigUtil.getRootDir() + File.separator + ConfigUtil.SCREEN_SHOT;

                File file = new File(path);

                File[] array = file.listFiles();

                if (array.length > ConfigUtil.getScreenshotCount()) {
                    File delFile = array[0];

                    for (File f : array) {
                        if (f.getName().compareTo(delFile.getName()) < 0) {
                            delFile = f;
                        }
                    }

                    log.info(delFile.toString());
                    delFile.delete();
                }
            }
        } catch (Exception e) {
            log.info("Fail to take screenshot!");
        }

        return screenShotName;
    }

    public static String getPageSource() {
        return getPageSource(1);
    }

    public static String getPageSource(int second) {
        log.info("======================================================getPageSource");
        if (second > 0) {
            sleep(second);
        }
        String xml = "";
        try {
            //在有特殊字符时 如&#   DocumentBuilder.parse 会抛异常
            xml = driver.getPageSource().replace("&#", "");
            if (Util.isWin()) {
                xml = xml.replace("UTF-8", "gbk");
            }
            if (ConfigUtil.isShowDomXML()) {
                log.info("\n\n\n\n\n" + xml + "\n\n\n\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to getPageSource!");
        }
        return xml;
    }

    public static String getPageStructure(String xml, String xpathExpression) throws Exception {
        log.info(getMethodName());
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        StringBuilder uiStructure = new StringBuilder();
        XPath xpath = XPathFactory.newInstance().newXPath();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);

        int length = nodes.getLength();

        while (length > 0) {
            length--;
            Node tmpNode = nodes.item(length);
            String nodeXpath = XPathUtil.getNodeXpath(tmpNode, true);
            //黑名单中的node会返回NULL,也就是说UIStructure中不会包含"客服"相关的元素
            if (null != nodeXpath) {
                uiStructure.append("\n" + nodeXpath + "\n");
            }
        }
        //log.info("Complete getting page structure");
        return uiStructure.toString();
    }

    public static void sleep(double seconds) {
        try {
            Thread.sleep((int) (seconds * 1000));
        } catch (Exception e) {
            log.info("Fail to sleep!!!!");
        }
    }


    //====================Element operation========================

    public static void setText(MobileElement elem, String text) {
        log.info(getMethodName() + " " + text);

        elem.click();
        elem.clear();
        elem.setValue(text);
    }

    public static void sendKeys(MobileElement elem, String text) {
        log.info(getMethodName() + " " + text);

        elem.click();
        elem.clear();
        elem.sendKeys(text);
        log.info("Text is set to : " + elem.getText());
    }

    public static int getDeviceHeight() {
        log.info(getMethodName());

        if (isLandscape()) {
            return deviceWidth;
        }

        return deviceHeight;
    }

    public static int getDeviceWidth() {
        log.info(getMethodName());

        if (isLandscape()) {
            return deviceHeight;
        }
        return deviceWidth;
    }

    public static boolean isLandscape() {
        log.info(getMethodName());

        boolean ret = false;
        try {
            log.info("Orientation : " + driver.getOrientation().toString());

            ret = driver.getOrientation().toString().contains("landscape");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found while getting screen orientation ");

            if (e.getMessage().contains("right")) {
                ret = true;
            }
        }

        log.info("isLandscape : " + ret);
        return ret;
    }


    public static Dimension getScreenSize() {
        log.info(getMethodName());
        Dimension dimensions = driver.manage().window().getSize();

        return dimensions;
    }

    /**
     * 向上滑动，从某个元素的顶部中间位置开始向上滑动一段距离
     *
     * @param yDiff
     */
    public static void scrollUp(MobileElement elem, int yDiff) {
        log.info(getMethodName());

        Point elemPoint = elem.getLocation();
        Dimension dim = elem.getSize();
        int startX = elemPoint.getX() + (int) (dim.getWidth() * 0.5);
        int startY = elemPoint.getY();
        int endX = startX;
        int endY = startY - yDiff;
        log.info("scroll from : startX " + startX + ", startY " + startY + ", to  endX " + endX + ",endY " + endY);
        TouchAction touchAction = new TouchAction(driver);
        PointOption pointStart = PointOption.point(startX, startY);
        PointOption pointEnd = PointOption.point(endX, endX);
        touchAction.press(pointStart).waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1))).moveTo(pointEnd).release().perform();
        log.info("scroll over");
    }

    public static void swipe(int startX, int startY, int endX, int endY) {
        log.info("scroll from : startX " + startX + ", startY " + startY + ", to  endX " + endX + ",endY " + endY);

        try {
            TouchAction touchAction = new TouchAction(driver);
            PointOption pointStart = PointOption.point(startX, startY);
            PointOption pointEnd = PointOption.point(endX, endY);

            WaitOptions waitOption = WaitOptions.waitOptions(Duration.ofMillis(1000));
            touchAction.press(pointStart).waitAction(waitOption).moveTo(pointEnd).release().perform();
        } catch (Exception e) {
            log.error("scroll from : startX " + startX + ", startY " + startY + ", to  endX " + endX + ",endY " + endY);
            e.printStackTrace();
        }
    }

    /**
     * 向下滑动，从某个元素的顶部中间位置开始向下滑动一段距离
     *
     * @param yDiff
     */
    public static void scrollDown(MobileElement elem, int yDiff) {
        log.info(getMethodName());
        scrollUp(elem, (-yDiff));
    }


    //====================Element Finding===========================
    public static MobileElement findElement(By by, int waitSeconds) {
        //log.info(util.getMethodName() + " " + by.toString());
        log.info(getMethodName());

        //WebElement elemME = waitMe.until(ExpectedConditions.visibilityOfElementLocated(by));
        //.ExpectedConditions.elementToBeClickable();
        AppiumDriverWait wait = AppiumDriverWait.getInstance(driver, waitSeconds);

        AppiumFunction<AppiumDriver, WebElement> waitFunction = var1 -> {
            WebElement elem = null;

            try {
                elem = var1.findElement(by);
            } catch (Exception e) {
                log.error("!!!!!!!!!!!!!!!!!!!!!!Element : " + by.toString() + " is not founded! Polling again...");
            }

            if (null != elem) {

                boolean display = elem.isDisplayed();

                if (!display) {
                    log.error("!!!!!!!!!!!!!!!!!!!!!!Element : " + by.toString() + " is found but not displayed!");
                    //elem = null;

                    if (Util.isAndroid()) {
                        elem = null;
                    }
                } else {
                    log.info("Element " + by.toString() + " is found.");
                }
            }

            return elem;
        };

        WebElement elem = wait.until(waitFunction);

        return (MobileElement) elem;
    }

    public static List<MobileElement> findElements(By by, int waitSeconds) {
        log.info(getMethodName() + by.toString());

        AppiumDriverWait wait = AppiumDriverWait.getInstance(driver, waitSeconds);

        AppiumFunction<AppiumDriver, List<MobileElement>> waitFunction = var1 -> {
            List<MobileElement> list = new ArrayList<>();

            try {
                list = var1.findElements(by);
            } catch (Exception e) {
                log.info("!!!!!!!!!!!!!!!!!!!!!!Element : " + by.toString() + " is not founded! Polling again...");
            }

            int size = list.size();

            if (0 == size) {
                list = null;
                log.info(by.toString() + " list size is 0");
            } else {
                log.info(by.toString() + " list size is " + size);
            }

            return list;
        };

        List<MobileElement> elemList = wait.until(waitFunction);

        return elemList;
    }

    public static List<MobileElement> findElements(By by) {
        log.info(getMethodName() + by);

        return Driver.findElements(by, (int) ConfigUtil.getDefaultWaitSec());
    }

    public static List<MobileElement> findElemsWithoutException(By by) {
        log.info(getMethodName());

        List<MobileElement> list = null;

        try {
            list = Driver.findElements(by, (int) ConfigUtil.getDefaultWaitSec());
        } catch (Exception e) {
            log.info("Elems " + by.toString() + " is not found.");
        }

        return list;
    }

    public static MobileElement findElement(By by) {
        return findElement(by, (int) ConfigUtil.getDefaultWaitSec());
    }


    public static MobileElement findElementWithoutException(By by) {
        MobileElement elem = null;
        try {
            elem = Driver.findElement(by, (int) ConfigUtil.getDefaultWaitSec());
        } catch (Exception e) {
            log.info("Elems " + by.toString() + " is not found.");
        }

        return elem;
    }

    /**
     * 查找元素，如果元素不存在，不报异常，返回null
     *
     * @param id
     * @return
     */
    public static MobileElement findElemByIdWithoutException(String id, int second) {
        log.info(getMethodName());

        MobileElement elem = null;

        try {
            if (!Util.isAndroid()) {
                elem = Driver.findElementByNsPredicateIOS(id, second);
            } else {
                elem = Driver.findElement(By.id(id), second);
            }

        } catch (Exception e) {
            log.info("Elem " + id + " is not found.");
        }

        return elem;
    }

    public static MobileElement findElemByIdWithoutException(String id) {
        return findElemByIdWithoutException(id, (int) ConfigUtil.getDefaultWaitSec());
    }

    /**
     * 检查某个元素是否存在，存在返回true，不存在返回false
     *
     * @param id
     * @return
     */
    public static boolean elemCheckById(String id) {
        log.info(getMethodName());
        return elemCheckById(id, (int) ConfigUtil.getDefaultWaitSec());
    }

    public static boolean elemCheckById(String id, int second) {
        log.info(getMethodName());

        boolean ret = false;
        MobileElement elem = findElemByIdWithoutException(id, second);

        if (null != elem) {
            ret = true;
        }

        log.info(id + " " + ret);
        return ret;
    }

    public static MobileElement findElemByTextWithoutException(String text) {
        log.info(getMethodName());

        MobileElement elem = null;
        text = "//*[contains(@text," + "\"" + text + "\"" + ")]";
        try {
            elem = Driver.findElement(By.xpath(text));
        } catch (Exception e) {
            log.info("Elem " + text + " is not found.");
        }

        return elem;
    }

    public static boolean elemCheckByText(String text) {
        log.info(getMethodName());

        boolean ret = false;
        MobileElement elem = findElemByTextWithoutException(text);

        if (null != elem) {
            ret = true;
        }

        log.info(text + " " + ret);
        return ret;
    }


    /**
     * 滑动到指定元素，如果元素不存在，不报异常，返回null
     * 只有安卓实现了滑动，ios还是使用find方法
     *
     * @param id
     * @return
     */
    public static MobileElement scrollToElementByIdWithoutException(String id) {
        log.info(getMethodName());

        MobileElement elem = null;

        try {
            if (!Util.isAndroid()) {
                elem = Driver.findElementByNsPredicateIOS(id);
            } else {
                //scrollToElementById(id) ;
                By by = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                        + "new UiSelector().resourceId(\"" + id + "\"));");
                elem = Driver.findElement(by);
            }

        } catch (Exception e) {
            log.info("Elem " + id + " is not found.");
        }

        return elem;
    }

    /**
     * 检查某个元素是否存在，存在返回true，不存在返回false
     *
     * @param id
     * @return
     */
    public static boolean elemCheckByScrollToId(String id) {
        log.info(getMethodName());

        boolean ret = false;
        MobileElement elem = scrollToElementByIdWithoutException(id);

        if (null != elem) {
            ret = true;
        }

        log.info(id + " " + ret);
        return ret;
    }


    public static MobileElement findElementByXpath(String xPath) {
        log.info(getMethodName() + xPath);
        return findElement(By.xpath(xPath));
    }

    public static List<MobileElement> findElementsByXpath(String xpath) {
        return findElements(By.xpath(xpath));
    }

    public static List<MobileElement> findElementsByXpathWithoutException(String xpath) {
        return findElemsWithoutException(By.xpath(xpath));
    }

    public static MobileElement findElementById(String id) {
        if (!Util.isAndroid()) {
            return findElementByNsPredicateIOS(id);
        }
        return findElement(By.id(id));
    }

    public static MobileElement findElementByText(String str) {
        if (!Util.isAndroid()) {
            str = "name == " + "\'" + str + "\'";
            return findElementByNsPredicateIOS(str);
        } else {
            //return findElement(By.xpath("//*[contains(@text," + "\"" + str + "\"" + ")]"));
            return scrollToElementByText(str);
        }
    }

    public static MobileElement findElementContainsText(String str) {
        if (!Util.isAndroid()) {
            str = "name CONTAINS " + "\'" + str + "\'";
            return findElementByNsPredicateIOS(str);
        } else {
            str = "//*[contains(@text," + "\"" + str + "\"" + ")]";
            return findElement(By.xpath(str));
        }
    }


    public static MobileElement findElementByClassAndText(String className, String str) {
        str = "//*[@class=\"" + className + "\" and @text=\"" + str + "\"]";
        return findElement(By.xpath(str));
    }

    //IOS only
    public static MobileElement findElementByNsPredicateIOS(String str) {

        return findElementByNsPredicateIOS(str, (int) ConfigUtil.getDefaultWaitSec());
    }

    //IOS only
    public static MobileElement findElementByNsPredicateIOS(String str, int seconds) {

        return findElement(MobileBy.iOSNsPredicateString(str), seconds);
    }

    public static MobileElement getFirstbyPredicateIOS(String str) {
        MobileElement elem;

        List<MobileElement> elemList = ((IOSDriver) driver).findElementsByIosNsPredicate(str);

        if (null == elemList || elemList.size() == 0) {
            elem = null;
        } else {
            elem = elemList.get(0);
        }

        return elem;
    }


    //===================End of element finding methods
    public static String getTextByXpath(String xPath) {
        return findElementByXpath(xPath).getText();
    }

    public static String getXpathByResID(String resId) {
        return getXpathByResID(resId, "com.tigerbrokers.stock:id/edit_number");
    }

    public static String getXpathByResID(String resId, String subResID) {
        return "//*[contains(@resource-id,\"" + resId + "\")]//*[contains(@resource-id,\"" + subResID + "\")]";
    }

    public static String getXpathByClassID(String resId, String classId) {
        return "//*[contains(@resource-id,\"" + resId + "\")]//*[contains(@class,\"" + classId + "\")]";
    }


    /**
     * 滑动到指定元素id的位置
     *
     * @param id
     * @return
     */
    public static MobileElement scrollToElementById(String id) {
        log.info(getMethodName());

        if (!Util.isAndroid()) {
            return Driver.findElementByNsPredicateIOS(id);
        }

        By by = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                + "new UiSelector().resourceId(\"" + id + "\"));");
//        By by = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
//                + "new UiSelector().text(\"" + text + "\"));");
        return Driver.findElement(by);
    }

    /**
     * 滑动到指定元素文本的位置
     *
     * @param text
     * @return
     */
    public static MobileElement scrollToElementByText(String text) {
        log.info(getMethodName() + " " + text);

        By by = MobileBy.AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                + "new UiSelector().text(\"" + text + "\"));");
        return Driver.findElement(by);
    }

    public static AppiumDriver prepareForAppiumIOS(String bundleId, String uuid) throws Exception {
        log.info(getMethodName());

        ConfigUtil.setUdid(uuid);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, ConfigUtil.getDeviceName());
        capabilities.setCapability(MobileCapabilityType.UDID, uuid);
        capabilities.setCapability("bundleId", bundleId);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 1800);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT, 1800 * 1000);
        capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, nextFreePort());
        capabilities.setCapability(IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS, true);

        driver = new IOSDriver(new URL(url), capabilities);
        setWindowSize();
        log.info("Server started.");
        return driver;
    }

    public static AppiumDriver prepareForAppiumAndroid(String appPackage, String appActivity, String udid) throws Exception {
        log.info(getMethodName());

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, udid);
        capabilities.setCapability(MobileCapabilityType.UDID, udid);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 1800);
        capabilities.setCapability("appPackage", appPackage);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");

        capabilities.setCapability("appActivity", appActivity);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true); //Don't delete app data
        capabilities.setCapability("unicodeKeyboard", true); //支持中文输入
        capabilities.setCapability("resetKeyboard", true); //重置输入法为系统默认

        driver = new AndroidDriver(new URL(url), capabilities);
        //初始化屏幕大小
        setWindowSize();
        return driver;
    }

    private static void setWindowSize() {
        deviceHeight = driver.manage().window().getSize().getHeight();
        deviceWidth = driver.manage().window().getSize().getWidth();
    }


    public static void drag(List<String> pointsList) {
        log.info(getMethodName());
        TouchAction dragAction = new TouchAction(driver);
        List<PointOption> pointOptionList = new ArrayList<>();

        if (pointsList.size() % 2 != 0) {
            log.error("drag value is not configured correctly: " + pointOptionList.toString());
            return;
        }

        try {
            for (int i = 0; i < pointsList.size(); i = i + 2) {
                int x = Integer.valueOf(pointsList.get(i));
                int y = Integer.valueOf(pointsList.get(i + 1));
                pointOptionList.add(PointOption.point(x, y));
            }
            for (int i = 0; i < pointOptionList.size(); i++) {
                if (i == 0) {
                    dragAction.press(pointOptionList.get(i)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)));
                } else {
                    dragAction.moveTo(pointOptionList.get(i)).waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)));
                }
            }
            dragAction.release().perform();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to perform drag operation");
        }
    }

    public static void pressBack() {
        log.info("Method : pressBack");
        if (Util.isAndroid()) {
            pressKeyCode(AndroidKey.BACK);
        } else {
            swipeHorizontally(true);
        }

        Driver.sleep(1);
    }

    public static String getCurrentActivity() {
        String activity = "ios";
        if (Util.isAndroid()) {
            try {
                activity = ((AndroidDriver) driver).currentActivity();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Fail to get current Activity");
                activity = "android";
            }
        }

        return activity;
    }

    public static String startLogRecord() {
        String logName = ConfigUtil.getRootDir() + File.separator + ConfigUtil.getDeviceName() + "-" + Util.getDatetime() + ".log";

        Runnable newRunnable = () -> {
            ArrayList<String> cmd = new ArrayList<>();
            if (Util.isAndroid()) {
                cmd.add("adb -s " + ConfigUtil.getUdid() + " logcat > " + logName);
            } else {
                cmd.add("idevicesyslog -u " + ConfigUtil.getUdid() + " > " + logName);
            }

            Util.exeCmd(cmd);
        };
        Thread thread = new Thread(newRunnable);
        thread.setDaemon(true);
        thread.start();
        return logName;
    }

    public static String getPlatformName() {
        return driver.getPlatformName().toUpperCase();
    }

    public static String getPlatformVersion() {
        if (Util.isAndroid()) {
            return Util.exeCmd("adb -s " + ConfigUtil.getUdid() + " shell getprop ro.build.version.release");
        } else {
            return Util.exeCmd("ideviceinfo  -u " + ConfigUtil.getUdid() + " -k ProductVersion");
        }
    }

    public static void rotateToLandscape(boolean landscape) {
        log.info(getMethodName());

        log.info(driver.getOrientation().toString());

        if (landscape) {
            driver.rotate(ScreenOrientation.LANDSCAPE);
        } else {
            driver.rotate(ScreenOrientation.PORTRAIT);
        }

        log.info(driver.getOrientation().toString());
    }


    public static void swipeVertical(boolean scrollDown) {
        log.info(getMethodName());

        Dimension dimensions = driver.manage().window().getSize();
        log.info("Screen size :" + dimensions);

        //Xiao Mi 1080,1920
        int startX = (int) (dimensions.getWidth() * 0.5);
        int startY = (int) (dimensions.getHeight() * 0.5);
        int endX = startX;
        int endY = 50;

        if (scrollDown) {
            endY = startY * 2 - 50;
        }

        if (!Util.isAndroid()) {
            //TODO: 解决ios 相对坐标问题，  升级Java-client版本？？？

            if (scrollDown) {
                endY = getDeviceHeight() / 2;
            } else {
                endY = -getDeviceHeight() / 2;
            }

            endX = 0;
        }

        swipe(startX, startY, endX, endY);
    }

    public static void swipeHorizontally(boolean leftToRight) {
        log.info(getMethodName());

        Dimension dimensions = driver.manage().window().getSize();
        log.info("Screen size :" + dimensions);

        int screenWidth = dimensions.getWidth();
        int screenHeight = dimensions.getHeight();

        //Xiao Mi 1080,1920  //iPhone 414  736
        int startX = 0;
        int endX = screenWidth / 2;

        if (!leftToRight) {
            startX = screenWidth / 2;
            endX = 0;
        }

        int startY = screenHeight / 2;
        int endY = startY;

        if (!Util.isAndroid()) {
            //TODO: 解决ios 相对坐标问题，  升级Java-client版本？？？
            //注意！！！！iOS时 第二个point的X,Y 会加上第一个Point的X,Y的值 也就是说第二个Point的X,Y是第一个Point的要加的减值
            //iOS, X,Y的值  乘以一个系数才是屏幕上的坐标值
            if (leftToRight) {
                startX = 0;
                endX = 750;//iphone 4-5的屏宽是640  iphone6 750

            } else {
                startX = 750;
                endX = -750;
            }

            startY = 50;
            endY = 0;
        }

        swipe(startX, startY, endX, endY);
    }

    public static void pressKeyCode(AndroidKey code) {
        if (Util.isAndroid()) {
            //((AndroidDriver)driver).pressKeyCode(code);
            ((AndroidDriver) driver).pressKey(new KeyEvent(code));
        } else {
            //((IOSDriver)driver)
        }
    }

    public static void pressHomeKey() {
        log.info(getMethodName());
        pressKeyCode(AndroidKey.HOME);
    }

    public static String getMethodName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];
        String methodName = e.getMethodName();
        return "===== Method : " + methodName + "   ";
    }

    public static synchronized Integer nextFreePort() {
        Integer port = (int) (Math.random() * 8000) + 8100;
        while (true) {
            try (ServerSocket endpoint = new ServerSocket(port)) {
                System.out.println("Local Port on which this socket is listening :" + port);
                return port;
            } catch (IOException e) {
                port = ThreadLocalRandom.current().nextInt();
            }
        }
    }

    public static void appRelaunch() {
        driver.launchApp();
    }

    public static void takeScreenShot() {
        takeScreenShot(String.valueOf(screenshotCount++));
    }
}
