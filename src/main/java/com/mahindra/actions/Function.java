package com.mahindra.actions;

import com.mahindra.config.*;
import com.mahindra.core.*;
import com.mahindra.utils.*;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function extends ConnectToDataSheet {

    public static WebElement element;
    public static List<WebElement> elements;
    public static UtilsActivity utilsActivity;
    public static boolean ActualResult;
    public static long executionStartTime;
    public static String executionStartTimeProperFormat;
    public static int randomNumber;
    public static String contractNumber;
    public static String applicantName;
    public static String coApplicantName;
    public static String guarantorName;
    public static String getText;
    public static String webGetText;
    public static Actions act;
    public static int cpcProceedButton = 0;
    public static Map<String, Integer> duplicatePersonaListDetails;
    public static Map<String, Integer> LogicalUserQCduplicatePersonaListDetails;
    public static String applicationID = Framework.applicID;
    public static String appVersion = "945";

    // ✅ DYNAMIC: Actions that trigger page navigation (used to auto-set
    // pageNavigated flag for Web only)
    // Add new navigation action names here — no need to touch individual case
    // blocks
    private static final Set<String> PAGE_NAVIGATION_ACTIONS = Set.of(
            "STARTBROWSER", "NEWWINDOWBROWSWRTAB", "BROWSERURL",
            "NAVIGATEBACK", "PAGEREFRESH", "CLICK", "JAVASCRIPTCLICK",
            "CHECKANDCLICK", "WAIT_FOR_NEXTELEMENT");
    private static final Duration DEFAULT_ACTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SHORT_ACTION_TIMEOUT = Duration.ofSeconds(3);

    Function() {
        element = LocatorManager.webElement;
        elements = LocatorManager.webElements;
    }

    public static void ActionRDS() throws Exception {

        // ✅ FIX: Reuse the utilsActivity already created in
        // ConnectToDataSheet.extractAllData()
        // instead of creating a new UtilsActivity() on every single step (was creating
        // thousands of objects)
        utilsActivity = ConnectToDataSheet.utilsActivity;

        // Here for WebTesting it Highlight every element using color combination
        if (ConnectToMainController.PlatForm.equalsIgnoreCase("WEB")) {

            if (PropertyName != null && !PropertyName.isEmpty() && PropertyValue != null && !PropertyValue.isEmpty()
                    && element != null) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript(
                        "arguments[0].style.border='3px solid red';" + "arguments[0].style.backgroundColor='yellow';"
                                + "setTimeout(() => {" + "   arguments[0].style.border='';"
                                + "   arguments[0].style.backgroundColor='';" + "}, 100);",
                        element);
            }
        }

        /// here for everyStepScreenShot code
        // ✅ FIX: Null-safe check to prevent NPE if StepsScreenshot is null
        if ("Y".equalsIgnoreCase(ConnectToMainController.StepsScreenshot)) {
            if ((PropertyName != null && !PropertyName.isEmpty() && PropertyValue != null && !PropertyValue.isEmpty()
                    && element != null)) {
                String destFileEveryStepScreenShot = utilsActivity.takeScreenShotEveryStep(element);
                everyStepScreenShot.add(Arrays.asList(Si_No, TestCaseStepDescription, PropertyName, PropertyValue,
                        DataField, Action, dataSheet2Value, everyStepExecutionTime, destFileEveryStepScreenShot));
            } else {
                everyStepScreenShot.add(Arrays.asList(Si_No, TestCaseStepDescription, PropertyName, PropertyValue,
                        DataField, Action, dataSheet2Value, everyStepExecutionTime, " "));
            }
        }

        switch (Action.toUpperCase()) {

            case "MONITORING_PROPERTIES" -> {
                // start time count
                executionStartTime = System.nanoTime();
                executionStartTimeProperFormat = utilsActivity.fetchSystemCurrentTime();
            }

            case "START_APPLICATION" -> {
                MobileConfiguration.mobileConfigurationSheet();
                if (ConnectToMainController.ExecutionType.equalsIgnoreCase("local")) {
                    Android_IOS_Driver.InitialisationDriverLocal();
                } else if (ConnectToMainController.ExecutionType.equalsIgnoreCase("remote")) {
                    Android_IOS_Driver.InitialisationDriverRemote();

                }
            }

            case "INSTALLANDSTARTAPPLICATION" -> {
                MobileConfiguration.mobileConfigurationSheet();
            }

            case "STARTBROWSER" -> {
                Initialisation(dataSheet2Value);
            }

            case "NEWWINDOWBROWSWRTAB" -> {
                driver.switchTo().newWindow(WindowType.TAB);
            }

            case "BROWSERURL" -> {
                StringBuilder finalUrl = new StringBuilder();
                if (dataSheet2Value != null && !dataSheet2Value.trim().isEmpty()) {
                    String[] parts = dataSheet2Value.split("\\+");
                    for (String part : parts) {
                        part = part.trim();
                        try {
                            finalUrl.append(ConfigManager.get(part));
                        } catch (Exception e) {
                            finalUrl.append(part);
                        }
                    }
                }
                driver.get(finalUrl.toString());
            }

            case "GETPAGESOURCE" -> {
                String pageHtml = driver.getPageSource();
//                System.out.println(pageHtml);
            }

            case "SENDKEYSUSING_CONFIGVALUE" -> {
                if (element == null) {
                    logger.error("❌ Element is null for action: SENDKEYSUSING_CONFIGVALUE at SI_No={}", Si_No);
                    return;
                }
                element.sendKeys(ConfigManager.get(dataSheet2Value));
            }

            case "SENDKEYSANDENTERKEY" -> {
                if (!ensureElementPresent("SENDKEYSANDENTERKEY")) {
                    return;
                }
                element.sendKeys(dataSheet2Value, Keys.ENTER);
            }

            case "ELEMENTWITHENTERKEY" -> {
                assert element != null;
                element.sendKeys(Keys.ENTER);
            }

            case "APPLICATIONIDSEARCHONSFDC" -> {
                if (!ensureElementPresent("APPLICATIONIDSEARCHONSFDC")) {
                    return;
                }
                element.sendKeys(applicationID, Keys.ENTER);
            }

            case "APPVERSION" -> {
                appVersion = getText;
            }

            case "MOBILEGETTEXT" -> {
                try {
                    getText = element.getAttribute("content-desc");
                } catch (Exception e) {
                    getText = element.getAttribute("text");
                }
                System.out.println("gettext============================================================" + getText);
            }

            case "WEBGETTEXT" -> {
                webGetText = element.getText();
                System.out
                        .println("WEBgettext============================================================" + webGetText);
            }

            case "UPDATEAPPLICANTNAME" -> {
                applicantName = extractFirstWord(getText, "UPDATEAPPLICANTNAME");
                System.out.println("applicantName = " + applicantName);
            }

            case "UPDATECOAPPLICANTNAME" -> {
                coApplicantName = extractFirstWord(getText, "UPDATECOAPPLICANTNAME");
                System.out.println("coApplicantName = " + coApplicantName);
            }

            case "UPDATEGUARANTORNAME" -> {
                guarantorName = extractFirstWord(getText, "UPDATEGUARANTORNAME");
                System.out.println("guarantorName = " + guarantorName);
            }

            case "UPDATECONTRACTNUMBER" -> {
                contractNumber = getText;
                // System.out.println("contractNumber = " + contractNumber);
                logger.info("ContractNumber = {}\n\n\n", contractNumber);
            }

            case "ENTERCONTRACTNUMBER" -> {
                if (element == null) {
                    logger.error("❌ Element is null for action: ENTERCONTRACTNUMBER at SI_No={}", Si_No);
                    return;
                }
                element.sendKeys(contractNumber);
            }

            case "STOREAPPLICATIONID" -> {
                UtilsActivity.CreateExcelSheetToStoreApplicationID();
                applicationID = getText.substring(getText.indexOf(":") + 2, getText.indexOf("generated!") - 1);
                // ✅ Si_No = auto-increment row count | also store ScenarioNo + VerticalName
                // so the Excel clearly shows WHICH scenario & vertical produced each
                // Application ID
                UtilsActivity.writeApplicationIDToExcel(
                        Integer.toString(ConnectToDataSheet.globallySheetTwoRowCount),
                        applicationID,
                        ConnectToMainController.ScenarioNo, // e.g. SC_01
                        ConnectToMainController.VerticalName); // e.g. XPL / SALPL
                System.out.println("applicationID => " + applicationID);
                logger.info("ApplicationID = {}\n\n\n", applicationID);
            }

            case "UPDATEAPPLICATIONID" -> {
                UtilsActivity.readApplicationIDToExcel();
                applicationID = UtilsActivity.assignApplicationId(ConnectToDataSheet.globallySheetTwoRowCount - 1);
                System.out.println("applicationID => " + applicationID);
                // logger.info("ApplicationID = " + applicationID + "\n");
            }

            case "QUIT" -> {
                if (driver != null) {
                    try {
                        if (ConnectToMainController.PlatForm.equalsIgnoreCase("Mobile")) {
                            ((AndroidDriver) driver).terminateApp(MobileConfiguration.App_PackageName);
                            fnStopAppiumServer();
                        }
                    } catch (Exception e) {
                        System.out.println("App termination failed: " + e.getMessage());
                        logger.info("App termination failed: {}", e.getMessage());
                    } finally {
                        driver.quit();
                    }
                }
            }

            case "KEYBOARDSENDKEYS" -> {
                String testData = null;
                try {
                    testData = ConfigManager.get(dataSheet2Value);
                } catch (Exception e) {
                    // dataSheet2Value is a raw value, not a key in config.yaml
                }
                act = new Actions(driver);
                act.sendKeys((testData == null) ? dataSheet2Value : testData).perform();
            }

            case "CLICK" -> {
                // ✅ GUARD: elementFoundByLocator = false means LocatorManager never found
                // the element (visibility timed out). Skip immediately — no point waiting
                // another 30s for clickability when the element isn't even on screen.
                // elementFoundByLocator = true means element IS visible → proceed to click.
                if (!LocatorManager.elementFoundByLocator) {
                    logger.warn(
                            "⚠️ CLICK skipped: element was not found by LocatorManager (elementFoundByLocator=false).");
                    return;
                }
                // ✅ CLICKABILITY CHECK: element is confirmed VISIBLE by LocatorManager.
                // Some buttons are visible but take time to become enabled (OTP, submit, etc.).
                // Full ExplicitWait (30s) gives them enough time to become interactable.
                try {
                    locatorManager.getWait().until(ExpectedConditions.elementToBeClickable(element)).click();
                } catch (Exception e) {
                    // Let the exception bubble up to LocatorManager's retry loop.
                    // If it's the last retry, LocatorManager will take the screenshot and log the
                    // error.
                    throw e;
                }
            }

            case "JAVASCRIPTCLICK" -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", element);
            }

            case "CHECKANDCLICK" -> {
                if (ConnectToMainController.PlatForm.equalsIgnoreCase("Web"))
                    locatorManager.waitForPageReady();
                logActionStart("CHECKANDCLICK", PropertyValue);
                try {
                    WebDriverWait wait = new WebDriverWait(driver, DEFAULT_ACTION_TIMEOUT);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PropertyValue))).click();
                    logActionSuccess("CHECKANDCLICK", PropertyValue);
                } catch (TimeoutException | org.openqa.selenium.NoSuchElementException | ElementClickInterceptedException e) {
                    logger.warn("CHECKANDCLICK skipped: element not found/clickable within timeout. xpath={}, SI_No={}, reason={}",
                            PropertyValue, Si_No, e.getMessage());
                    System.out.println("Element is not There...");
                }
            }

            case "SENDKEYS" -> {
                if (!ensureElementPresent("SENDKEYS")) {
                    return;
                }
                element.sendKeys(dataSheet2Value);
            }

            case "SAPCODE_SENDKEYS", "PASSWORD_SENDKEYS" -> {
                if (element == null) {
                    logger.error("❌ Element is null for action: {} at SI_No={}", Action, Si_No);
                    return;
                }
                String credentialKey = Action.equalsIgnoreCase("SAPCODE_SENDKEYS") ? "SAPCODE" : "PASSWORD";
                String credentialValue;
                try {
                    credentialValue = System.getProperty(credentialKey);
                } catch (Exception e) {
                    // Fallback for backward compatibility if key is not configured.
                    credentialValue = dataSheet2Value;
                }
                element.sendKeys(credentialValue);
                logger.info("{} sent to element at SI_No={}", credentialKey, Si_No);
            }

            case "CLICKCLEARSENDKEYS" -> {
                if (!ensureElementPresent("CLICKCLEARSENDKEYS")) {
                    return;
                }
                WebDriverWait wait = new WebDriverWait(driver, SHORT_ACTION_TIMEOUT);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                wait.until(ExpectedConditions.visibilityOf(element));
                element.clear();
                wait.until(ExpectedConditions.elementToBeClickable(element));
                element.sendKeys(dataSheet2Value);
            }

            case "GETATTRIBUTEVALUE" -> {
                assert element != null;
                System.out.println(element.getDomProperty("value"));
            }

            case "CLEAR" -> {
                assert element != null;
                element.clear();
            }

            case "JS_CLEARSENDKEYS" -> {

                JavascriptExecutor js = (JavascriptExecutor) driver;

                js.executeScript(
                        "var element = arguments[0];" +
                                "var value = arguments[1];" +

                                // ✅ Get native setter
                                "var setter = Object.getOwnPropertyDescriptor(element.__proto__, 'value').set;" +

                                // ✅ Set value properly (important!)
                                "setter.call(element, value);" +

                                // ✅ trigger React update
                                "element.dispatchEvent(new Event('input', { bubbles: true }));",

                        element,
                        dataSheet2Value);
            }

            case "CLEARANDUPDATEVALUEUSINGJAVASCRIPT" -> {
                WebScrolling.ClearFieldUsingJavaScript(driver, element, dataSheet2Value);
            }

            case "OPENAPP_USINGONLYAPPPACKAGE" -> {
                ((AndroidDriver) driver).activateApp(MobileConfiguration.App_PackageName);
                ((AndroidDriver) driver).rotate(ScreenOrientation.PORTRAIT);
            }

            case "TERMINATEAPP_USINGONLYAPPPACKAGE" -> {
                ((AndroidDriver) driver).terminateApp(MobileConfiguration.App_PackageName);
            }

            case "CAMERAIMAGEINJECTION" -> {
                JavascriptExecutor jse = (JavascriptExecutor) driver;
                jse.executeScript(
                        "browserstack_executor: {\"action\": \"cameraImageInjection\", \"arguments\": {\"imageUrl\": \"media://"
                                + dataSheet2Value + "\"}}");
            }

            case "PUSHFILETOBROWSERSTACKDEVICE" -> {
                ((AndroidDriver) driver).pushFile("/sdcard/Download/" + dataSheet2Value,
                        new File(ConnectToMainController.dataSheetFolderPath + File.separator + dataSheet2Value));
            }

            case "SELECTUPLOADFILE" -> {
                driver.findElement(By.xpath("//*[@text='" + dataSheet2Value + "']")).click();
            }

            case "GENERATERANDOMNUMBER" -> {
                int numDigits = Integer.parseInt(dataSheet2Value);
                int min = (int) Math.pow(10, numDigits - 1);
                int max = (int) Math.pow(10, numDigits) - 1;
                Random random = new Random();
                randomNumber = min + random.nextInt(max - min + 1);
                System.out.println("Generating random Number = " + randomNumber);
            }

            case "MPIN" -> {
                String mpin = String.valueOf(randomNumber);
                assert element != null;
                element.sendKeys(mpin);
            }

            case "CHECKVISIBILITY" -> {
                boolean Verify = Boolean.parseBoolean(dataSheet2Value);
                if (Verify) {
                    try {
                        assert element != null;
                        ActualResult = element.isDisplayed();
                        if (ActualResult) {
                            ConnectToDataSheet.status = "PASS";
                            utilsActivity.passTestCase();
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                        logger.error("At check visibility= " + e);
                        ActualResult = false;
                        ConnectToDataSheet.status = "FAIL";
                        fail++;
                        failedValidations++;
                        utilsActivity.failTestCase();
                    }
                }
            }

            case "ISENABLE" -> {
                boolean Verify = Boolean.parseBoolean(dataSheet2Value);
                if (Verify) {
                    try {
                        assert element != null;
                        ActualResult = element.isEnabled();
                        if (ActualResult) {
                            ConnectToDataSheet.status = "PASS";
                            utilsActivity.passTestCase();
                        }
                    } catch (Exception e) {
                        ActualResult = false;
                        ConnectToDataSheet.status = "FAIL";
                        ConnectToDataSheet.failedValidations++;
                        utilsActivity.failTestCase();
                    }
                }
            }

            case "BACKPAGE" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
                for (int i = 0; i < 10; i++) {
                    try {
                        boolean isVisible = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(PropertyValue)))
                                .isDisplayed();
                        if (isVisible) {
                            break;
                        }
                    } catch (Exception e) {
                        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
                    }
                }
            }

            case "GOBACK"-> {
                ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
            }

            case "HIDEKEYBOARDUSINGENTERKEY" -> {
                if (((AndroidDriver) driver).isKeyboardShown()) {
                    ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER));
                }
            }

            case "HIDEKEYBOARDIFITOPEN", "HIDEKEYBOARD" -> {
                if (((AndroidDriver) driver).isKeyboardShown()) {
                    ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
                }
            }

            case "SEARCHAPPLICATION" -> {
                assert element != null;
                element.sendKeys(applicationID);
            }

            case "CLICKONSEARCHAPPLICATION" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//android.view.View[contains(@content-desc,'ID: " + applicationID + "' )]"))).click();
            }

            case "CLICKONSEARCHLEADID" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//android.view.View[contains(@content-desc,'ID: " + dataSheet2Value + "' )]")))
                        .click();
            }

            case "PENNYDROP" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                try {
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//android.widget.Button[@content-desc='Retry']")))
                            .click();
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//android.widget.Button[@content-desc='Retry']")))
                            .click();
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//android.widget.Button[@content-desc='Okay']")))
                            .click();
                } catch (Exception e) {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("android.widget.CheckBox")))
                            .click();
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//android.widget.Button[@content-desc='Proceed']")))
                            .click();
                }
            }

            case "AGAINCLICKONSEARCHBAR" -> {
                driver.findElement(By.xpath("//button[contains(@aria-label,'" + applicationID + "')]")).click();
                By searchInputPrimary = By.xpath("//input[@placeholder='Search...']");
                By searchInputFallback = By.xpath("//input[@placeholder='Search Cases and more...']");
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(searchInputPrimary),
                        ExpectedConditions.visibilityOfElementLocated(searchInputFallback)));
                try {
                    driver.findElement(searchInputPrimary).sendKeys(Keys.ENTER);
                } catch (Exception e) {
                    driver.findElement(searchInputFallback)
                            .sendKeys(Keys.ENTER);
                }
            }

            case "CLICKONCPCMOREBUTTON" -> {
                try {
                    driver.findElement(
                            By.xpath("(//*[@class='forcegenerated-flexipage-module']//button[@title='More Tabs'])[5]"))
                            .click();
                } catch (Exception e) {
                    driver.findElement(
                            By.xpath("(//*[@class='forcegenerated-flexipage-module']//button[@title='More Tabs'])[3]"))
                            .click();
                }
            }

            case "CLICKONPERSONA" -> {
                driver.findElement(By.xpath("//android.widget.Button[@content-desc='" + dataSheet2Value + "']"))
                        .click();
            }

            case "CPC_BANKINGDETAILS" -> {
                try {
                    driver.findElement(By.xpath("(//span[text()='Banking Details'])[2]")).click();
                    System.out.println("(//span[text()='Banking Details'])[2]");
                } catch (Exception e) {
                    try {
                        driver.findElement(By.xpath("//span[text()='Banking Details']")).click();
                        System.out.println("//span[text()='Banking Details']");
                    } catch (Exception e1) {
                        driver.findElement(By.xpath("//a[text()='Banking Details']")).click();
                    }
                }
            }

            case "ACCESSIBILITYID_CLICK" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId(dataSheet2Value)));
                element.click();
            }

            case "UNTILSCROLLDOWNELEMENTVIEW" -> {
                boolean isElementVisible = false;
                int scrollCount = 0;
                final int maxScrollCount = 15;
                Throwable lastException = null;

                while (!isElementVisible && scrollCount < maxScrollCount) {
                    try {
                        String xpathExpression = PropertyValue;
                        element = driver.findElement(By.xpath(xpathExpression));

                        if (element.isDisplayed()) {
                            isElementVisible = true;
                            logger.info(
                                    "UNTILSCROLLDOWNELEMENTVIEW: Element found after {} downward scroll attempt(s). xpath={}",
                                    scrollCount, PropertyValue);
                            break;
                        }
                    } catch (org.openqa.selenium.NoSuchElementException | StaleElementReferenceException e) {
                        lastException = e;
                        scrollCount++;

                        int screenHeight = driver.manage().window().getSize().getHeight();
                        int screenWidth = driver.manage().window().getSize().getWidth();
                        int startX = screenWidth / 2;
                        int startY = (int) (screenHeight * 0.6);
                        int endY = (int) (screenHeight * 0.35);

                        // ✅ W3C Swipe replacing TouchAction
                        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                        Sequence swipe = new Sequence(finger, 1);
                        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX,
                                startY));
                        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                        swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(),
                                startX, endY));
                        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                        ((AndroidDriver) driver).perform(Collections.singletonList(swipe));
                    }
                }

                if (!isElementVisible) {
                    logger.error(
                            "UNTILSCROLLDOWNELEMENTVIEW failed: element not found after {} downward scroll(s). xpath={}",
                            maxScrollCount, PropertyValue, lastException);
                    throw new org.openqa.selenium.NoSuchElementException(
                            "Element not found after " + maxScrollCount + " downward scrolls. xpath=" + PropertyValue);
                }
            }

            case "SCROLLDOWNLITTLEBIT" -> {
                int screenHeight = driver.manage().window().getSize().getHeight();
                int screenWidth = driver.manage().window().getSize().getWidth();
                int startX = screenWidth / 2;
                int startY = (int) (screenHeight * 0.6);
                int endY = (int) (screenHeight * 0.5);

                // ✅ W3C PointerInput finger swipe replacing deprecated TouchAction
                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                Sequence swipe = new Sequence(finger, 1);
                swipe.addAction(
                        finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
                swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                swipe.addAction(
                        finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), startX, endY));
                swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                ((AndroidDriver) driver).perform(Collections.singletonList(swipe));
            }

            case "UNTILSCROLLUPELEMENTVIEW" -> {
                boolean isElementVisible = false;
                int scrollCount = 0;
                final int maxScrollCount = 15;
                Throwable lastException = null;

                while (!isElementVisible && scrollCount < maxScrollCount) {
                    try {
                        String xpathExpression = PropertyValue;
                        element = driver.findElement(By.xpath(xpathExpression));

                        if (element.isDisplayed()) {
                            isElementVisible = true;
                            logger.info("UNTILSCROLLUPELEMENTVIEW: Element found after {} upward scroll attempt(s). xpath={}",
                                    scrollCount, PropertyValue);
                            break;
                        }
                    } catch (org.openqa.selenium.NoSuchElementException | StaleElementReferenceException e) {
                        lastException = e;
                        scrollCount++;

                        int screenHeight = driver.manage().window().getSize().getHeight();
                        int screenWidth = driver.manage().window().getSize().getWidth();
                        int startX = screenWidth / 2;
                        int startY = (int) (screenHeight * 0.35);
                        int endY = (int) (screenHeight * 0.6);

                        // ✅ W3C Swipe replacing TouchAction
                        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                        Sequence swipe = new Sequence(finger, 1);
                        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX,
                                startY));
                        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                        swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(),
                                startX, endY));
                        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                        ((AndroidDriver) driver).perform(Collections.singletonList(swipe));
                    }
                }

                if (!isElementVisible) {
                    logger.error(
                            "UNTILSCROLLUPELEMENTVIEW failed: element not found after {} upward scroll(s). xpath={}",
                            maxScrollCount, PropertyValue, lastException);
                    throw new org.openqa.selenium.NoSuchElementException(
                            "Element not found after " + maxScrollCount + " upward scrolls. xpath=" + PropertyValue);
                }
            }

            case "SCROLLUPDOWNELEMENTUNTILLVISIABLE" -> {

                boolean elementIsVisible = false;
                int currentScrollCount = 0;
                int maxScrollCount = 30;
                boolean scrollingDown = true;

                int screenHeight = driver.manage().window().getSize().getHeight();
                int screenWidth = driver.manage().window().getSize().getWidth();

                // ✅ Track page source to detect if scroll hit the edge
                String previousPageSource = "";

                while (!elementIsVisible && currentScrollCount < maxScrollCount) {
                    try {
                        element = driver.findElement(By.xpath(PropertyValue));
                        webElement = element;

                        if (element.isDisplayed()) {
                            elementIsVisible = true;
                            int elementY = element.getLocation().getY();

                            // Slight adjustment if element is too close to bottom of screen
                            if (screenHeight - elementY <= screenHeight * 0.25) {
                                int scrollStartY = (int) (screenHeight * 0.6);
                                int scrollEndY = (int) (screenHeight * 0.5);

                                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                                Sequence swipe = new Sequence(finger, 1);
                                swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                                        screenWidth / 2, scrollStartY));
                                swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                                swipe.addAction(finger.createPointerMove(Duration.ofMillis(300),
                                        PointerInput.Origin.viewport(), screenWidth / 2, scrollEndY));
                                swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                                ((AndroidDriver) driver).perform(Collections.singletonList(swipe));

                            } else {
                                logger.info("Element is visible and positioned correctly!\n\n");
                            }
                            break;
                        }
                    } catch (Exception e) {
                        // Element not found yet, continue scrolling
                    }

                    // Perform scroll in current direction
                    int startX = screenWidth / 2;
                    int startY, endY;

                    if (scrollingDown) {
                        startY = (int) (screenHeight * 0.6);
                        endY = (int) (screenHeight * 0.45);
                    } else {
                        startY = (int) (screenHeight * 0.45);
                        endY = (int) (screenHeight * 0.6);
                    }

                    // ✅ Capture page source BEFORE swipe
                    String currentPageSource = driver.getPageSource();

                    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                    Sequence swipe = new Sequence(finger, 1);
                    swipe.addAction(
                            finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
                    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                    swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(),
                            startX, endY));
                    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                    ((AndroidDriver) driver).perform(Collections.singletonList(swipe));

                    // ✅ Capture page source AFTER swipe
                    String newPageSource = driver.getPageSource();

                    // ✅ If page didn't change → hit the edge → immediately reverse direction
                    // Same logic as HORIZONTALSCROLL uses oldX == newX
                    assert newPageSource != null;
                    if (newPageSource.equals(previousPageSource)) {
                        logger.info("Reached scroll edge, reversing direction...");
                        scrollingDown = !scrollingDown;
                    }

                    // ✅ Store current page source for next iteration comparison
                    previousPageSource = newPageSource;

                    // ✅ EXISTING: Alternate direction after half the scroll count (kept as-is)
                    if (currentScrollCount == maxScrollCount / 2) {
                        scrollingDown = !scrollingDown;
                    }

                    currentScrollCount++;
                }

                if (!elementIsVisible) {
                    logger.error("Scrolling limit completed. Element not found.\n\n");
                }
            }

            case "SWIPELEFTUNTILELEMENTFOUND" -> {

                int maxSwipes = 5;
                int attempt = 0;

                int screenWidth = driver.manage().window().getSize().width;
                int screenHeight = driver.manage().window().getSize().height;

                int startX = (int) (screenWidth * 0.8);
                int endX = (int) (screenWidth * 0.2);
                int y = screenHeight / 2;

                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

                while (attempt < maxSwipes) {
                    try {
                        WebElement element = driver.findElement(By.xpath(PropertyValue));
                        if (element.isDisplayed()) {
                            System.out.println("Element found!");
                            break;
                        }
                    } catch (Exception e) {
                        // Element not found, perform swipe
                        Sequence swipe = new Sequence(finger, 1);
                        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(),
                                startX, y));
                        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(),
                                endX, y));
                        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

                        // ((AndroidDriver) driver).perform(Arrays.asList(swipe));
                        ((AndroidDriver) driver).perform(Collections.singletonList(swipe));
                        attempt++;
                    }
                }

                if (attempt == maxSwipes) {
                    System.out.println("Element not found after 5 swipes.");
                }
            }

            case "TAPELEMENTCENTER" -> {

                // Step 1: Check element is found and displayed
                if (element == null || !element.isDisplayed()) {
                    System.out.println("[tapElementCenter] Element not found or not displayed. Skipping tap.");
                    return;
                }
                // System.out.println("[tapElementCenter] Element found and displayed.");

                // Step 2: Get element bounding rectangle and calculate center
                Rectangle rect = element.getRect();
                int centerX = rect.x + (rect.width / 2);
                int centerY = rect.y + (rect.height / 2);
                // System.out.println("[tapElementCenter] Rect → x=" + rect.x + ", y=" + rect.y
                // + ", width=" + rect.width + ", height=" + rect.height);
                // System.out.println("[tapElementCenter] Calculated Center → x=" + centerX + ",
                // y=" + centerY);

                // Step 3: Tap exactly at center coordinates
                PointerInput finger = new PointerInput(
                        PointerInput.Kind.TOUCH, "finger");

                Sequence tap = new Sequence(finger, 1)
                        .addAction(finger.createPointerMove(
                                Duration.ZERO,
                                PointerInput.Origin.viewport(),
                                centerX,
                                centerY))
                        .addAction(finger.createPointerDown(
                                PointerInput.MouseButton.LEFT.asArg()))
                        .addAction(finger.createPointerUp(
                                PointerInput.MouseButton.LEFT.asArg()));

                ((AndroidDriver) driver).perform(List.of(tap));
                // System.out.println("[tapElementCenter] Tap fired at center → x=" + centerX +
                // ", y=" + centerY);
            }

            case "UPDATETHREEPERSONANAME" -> {
                String[] personasNames = dataSheet2Value.split(" ");
                applicantName = personasNames[0];
                coApplicantName = personasNames[1];
                guarantorName = personasNames[2];
            }

            case "HORIZONTALSCROLL" -> {

                if (dataSheet2Value.equalsIgnoreCase("applicant")) {
                    dataSheet2Value = applicantName;
                } else if (dataSheet2Value.equalsIgnoreCase("coApplicant")) {
                    dataSheet2Value = coApplicantName;
                } else if (dataSheet2Value.equalsIgnoreCase("guarantor")) {
                    dataSheet2Value = guarantorName;
                }

                boolean isElementVisible = false;
                boolean scrollLeftDirection = true; // start with left scroll
                int maxScroll = 10;
                int scrollCount = 0;

                WebElement scrollableElement = driver.findElement(By.xpath(PropertyValue));
                Point location = scrollableElement.getLocation();
                Dimension size = scrollableElement.getSize();

                int centerY = location.getY() + (size.getHeight() / 2);

                int startX_Left = location.getX() + (int) (size.getWidth() * 0.80);
                int endX_Left = location.getX() + (int) (size.getWidth() * 0.20);

                int startX_Right = location.getX() + (int) (size.getWidth() * 0.20);
                int endX_Right = location.getX() + (int) (size.getWidth() * 0.80);

                while (!isElementVisible && scrollCount < maxScroll) {
                    try {
                        WebElement element = driver.findElement(By.xpath(
                                "//android.widget.Button[starts-with(@content-desc,'" + dataSheet2Value + "')]"));
                        if (element.isDisplayed()) {
                            element.click();
                            isElementVisible = true;
                            break;
                        }
                    } catch (Exception ignore) {
                    }

                    // store old location before swipe
                    int oldX = scrollableElement.getLocation().getX();

                    // perform swipe based on current direction
                    if (scrollLeftDirection) {
                        performHorizontalSwipe((AndroidDriver) driver, startX_Left, endX_Left, centerY);
                    } else {
                        performHorizontalSwipe((AndroidDriver) driver, startX_Right, endX_Right, centerY);
                    }

                    Thread.sleep(300);
                    scrollCount++;

                    // get new location after swipe
                    int newX = scrollableElement.getLocation().getX();

                    // If position didn't change → border reached → reverse direction
                    if (oldX == newX) {
                        scrollLeftDirection = !scrollLeftDirection;
                    }
                }

                if (!isElementVisible) {
                    System.out.println(
                            "❌ Element `" + dataSheet2Value + "` not found even after 10 alternating scrolls!");
                }

                break;
            }

            case "SELECTDROPDOWNVALUE" -> {

                boolean isElementVisible = false;
                int scrollCount = 0;

                WebElement scrollableElement = null;
                boolean isScrollablePresent = true;

                // ✅ Step 1: Try to find ScrollView
                try {
                    scrollableElement = driver.findElement(By.xpath(PropertyValue));
                } catch (Exception e) {
                    isScrollablePresent = false;
                }

                // ✅ Step 2: If NO ScrollView → direct click
                if (!isScrollablePresent) {
                    driver.findElement(AppiumBy.accessibilityId(dataSheet2Value)).click();
                    break;
                }

                while (!isElementVisible && scrollCount < 15) {
                    try {
                        WebElement element = driver.findElement(AppiumBy.accessibilityId(dataSheet2Value));
                        if (element.isDisplayed()) {
                            isElementVisible = true;
                            element.click(); // Optional: click when found
                            Thread.sleep(500);
                            break;
                        }
                    } catch (Exception e) {
                        scrollCount++;
                        // System.out.println("Scroll attempt #" + scrollCount);

                        try {
                            // Get dropdown position and size
                            Point location = scrollableElement.getLocation();
                            Dimension size = scrollableElement.getSize();

                            int startX = location.getX() + (size.getWidth() / 2);
                            int startY = location.getY() + (int) (size.getHeight() * 0.8); // near bottom
                            int endY = location.getY() + (int) (size.getHeight() * 0.2); // near top

                            // Perform W3C swipe inside dropdown
                            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                            Sequence swipe = new Sequence(finger, 1);

                            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                                    startX, startY));
                            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                            swipe.addAction(finger.createPointerMove(Duration.ofMillis(500),
                                    PointerInput.Origin.viewport(), startX, endY));
                            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

                            ((AndroidDriver) driver).perform(Collections.singletonList(swipe));

                        } catch (Exception w3cFail) {
                            System.out.println("Swipe failed on attempt #" + scrollCount);
                            logger.warn("SELECTDROPDOWNVALUE swipe failed at attempt {} for xpath={}. reason={}",
                                    scrollCount, PropertyValue, w3cFail.getMessage());
                        }
                    }
                }

            }

            case "DEDUPE" -> {
                try {
                    driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"Select\"]")).click();
                    driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"Proceed\"]")).click();
                } catch (Exception e) {
                    try {
                        driver.findElement(By.xpath("//android.widget.Button[@content-desc='No Match']")).click();
                    } catch (Exception e1) {
                        driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"Proceed\"]")).click();
                    }
                }
            }

            case "DEDUPE_PL" -> {
                try {
                    driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"select\"]")).click();
                    driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"Proceed\"]")).click();
                } catch (Exception e) {
                    driver.findElement(By.xpath("//android.widget.Button[@content-desc=\"Proceed\"]")).click();
                }
            }

            case "OFFERPOPUP" -> {

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                try {
                    // ✅ Try to find Offer Popup
                    WebElement offerPopup = wait.until(
                            ExpectedConditions.visibilityOfElementLocated(
                                    AppiumBy.accessibilityId("Insurance\nAdd-ons")));
                    if (offerPopup != null && offerPopup.isDisplayed()) {
                        System.out.println("⚠️ Offer popup NOT displayed, handling fallback");

                    }
                } catch (Exception e) {
                    System.out.println("✅ Offer Popup displayed");
                    // ✅ Check "Generating Offers"
                    try {
                        WebElement generating = new WebDriverWait(driver, Duration.ofSeconds(5))
                                .until(ExpectedConditions.visibilityOfElementLocated(
                                        AppiumBy.accessibilityId("Generating Offers!")));

                        if (generating.isDisplayed()) {
                            driver.findElement(AppiumBy.accessibilityId("Back to Home")).click();

                            new WebDriverWait(driver, Duration.ofSeconds(20))
                                    .until(ExpectedConditions.elementToBeClickable(
                                            AppiumBy.accessibilityId(
                                                    "In Progress\nSanction\nAdd customer details to process Offer")))
                                    .click();
                        }
                    } catch (Exception ex) {
                        System.out.println("❌ Neither Offer popup nor Generating Offers found");
                    }
                }
            }

            case "DETAILSINCOMPLETEPOPUP" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//android.view.View[@content-desc='Details Incomplete']"))).isDisplayed();
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.xpath("//android.widget.Button[@content-desc='Proceed']")))
                            .click();
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//android.widget.Button[@content-desc='Proceed']"))).click();
                } catch (Exception e) {
                    logger.info("DETAILSINCOMPLETEPOPUP not shown at SI_No={} for xpath={}", Si_No, PropertyValue);
                    System.out.println("Thanks God Details Incomplete popup not came");
                }
            }

            case "NAVIGATEBACK" -> {
                driver.navigate().back();
            }

            case "PAGEREFRESH" -> {
                driver.navigate().refresh();
            }

            case "INITILISEDDUPLICATEPERSONALISTDETAILS" -> {
                duplicatePersonaListDetails = new HashMap<String, Integer>();
                LogicalUserQCduplicatePersonaListDetails = new HashMap<String, Integer>();
            }

            case "KYCVERIFY" -> {
                try {
                    By secondInput = By.xpath(PropertyValue + "[2]");
                    WebDriverWait wait = new WebDriverWait(driver, DEFAULT_ACTION_TIMEOUT);
                    wait.until(ExpectedConditions.elementToBeClickable(secondInput)).click();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(secondInput)).sendKeys(dataSheet2Value);
                } catch (Exception e) {
                    By firstInput = By.xpath(PropertyValue + "[1]");
                    WebDriverWait wait = new WebDriverWait(driver, DEFAULT_ACTION_TIMEOUT);
                    wait.until(ExpectedConditions.elementToBeClickable(firstInput)).click();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(firstInput)).sendKeys(dataSheet2Value);
                }
            }

            case "MOUSEHOVER" -> {
                act = new Actions(driver);
                act.moveToElement(element).build().perform();
            }

            case "MOUSEHOVERCLICK" -> {
                act = new Actions(driver);
                act.moveToElement(element).click().perform();
            }

            case "CLICKONAPPLICATIONID" -> {
                String AppID;
                for (WebElement ele : elements) {
                    AppID = ele.getText();
                    System.out.println(AppID);
                    if (AppID.equalsIgnoreCase(applicationID)) {
                        ele.click();
                        break;
                    }
                }
            }

            case "SELECTCO_APPLICANTORGUARANTOR" -> {
                driver.findElement(
                        By.xpath("//android.widget.Button[contains(@content-desc,'" + dataSheet2Value + "')]")).click();
            }


            case "CPVSCROLL" -> {

                if (dataSheet2Value.equalsIgnoreCase("applicant")) {
                    dataSheet2Value = applicantName;
                } else if (dataSheet2Value.equalsIgnoreCase("coApplicant")) {
                    dataSheet2Value = coApplicantName;
                } else if (dataSheet2Value.equalsIgnoreCase("guarantor")) {
                    dataSheet2Value = guarantorName;
                }

                boolean elementIsVisible = false;
                int currentScrollCount = 0;
                int maxScrollCount = 12;

                while (!elementIsVisible && currentScrollCount < maxScrollCount) {
                    try {
                        // element = driver.findElement(By.xpath("//android.view.View[@content-desc='" +
                        // dataSheet2Value + "']//android.widget.Button"));

                        element = driver.findElement(By.xpath("//android.view.View[starts-with(@content-desc,'"
                                + dataSheet2Value + "')]/android.view.View/android.widget.Button"));

                        int screenHeight = driver.manage().window().getSize().getHeight();
                        int screenWidth = driver.manage().window().getSize().getWidth();
                        int elementY = element.getLocation().getY();

                        if (element.isDisplayed()) {
                            elementIsVisible = true;
                            if (screenHeight - elementY <= screenHeight * 0.25) {
                                int scrollStartY = (int) (screenHeight * 0.6);
                                int scrollEndY = (int) (screenHeight * 0.5);

                                // ✅ W3C Swipe replacing TouchAction (adjustment scroll)
                                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                                Sequence swipe = new Sequence(finger, 1);
                                swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                                        screenWidth / 2, scrollStartY));
                                swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                                swipe.addAction(finger.createPointerMove(Duration.ofMillis(300),
                                        PointerInput.Origin.viewport(), screenWidth / 2, scrollEndY));
                                swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                                ((AndroidDriver) driver).perform(Collections.singletonList(swipe));
                                element.click();
                            } else {
                                element.click();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Element not found yet
                    }

                    int screenHeight = driver.manage().window().getSize().getHeight();
                    int screenWidth = driver.manage().window().getSize().getWidth();
                    int startX = screenWidth / 2;
                    int startY = currentScrollCount < maxScrollCount / 2 ? (int) (screenHeight * 0.6)
                            : (int) (screenHeight * 0.45);
                    int endY = currentScrollCount < maxScrollCount / 2 ? (int) (screenHeight * 0.45)
                            : (int) (screenHeight * 0.6);

                    // ✅ W3C Swipe replacing TouchAction (main scroll)
                    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                    Sequence swipe = new Sequence(finger, 1);
                    swipe.addAction(
                            finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
                    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                    swipe.addAction(finger.createPointerMove(Duration.ofMillis(300), PointerInput.Origin.viewport(),
                            startX, endY));
                    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                    ((AndroidDriver) driver).perform(Collections.singletonList(swipe));

                    currentScrollCount++;
                }

                if (!elementIsVisible) {
                    System.out.println("Ohh Sorry... Scrolling limit completed. Element not found.");
                }
            }

            case "HOMEPAGECHECK" -> {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
                for (int i = 0; i < 10; i++) {
                    try {
                        boolean isVisible = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(AppiumBy.accessibilityId("Applications\nTab 2 of 3")))
                                .isDisplayed();
                        if (isVisible) {
                            break;
                        }
                    } catch (Exception e) {
                        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
                    }
                }
            }

            case "CPC_CASEASSIGN_CROSSBUTTON" -> {
                driver.findElement(By.xpath(PropertyValue)).click();
            }

            case "REMOVEAPPLICATIONUSINGBUNDLEID" -> {
                ((AndroidDriver) driver).removeApp(MobileConfiguration.AppPath);
            }

            case "SELECTNEXTMONTH" -> {
                LocalDate today = LocalDate.now();
                LocalDate nextMonthDate = today.plusMonths(1);
                String nextMonth = nextMonthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                driver.findElement(AppiumBy.accessibilityId(nextMonth)).click();
            }

            case "SELECTVISIBLETEXT" -> {
                assert element != null;
                Select select = new Select(element);
                select.selectByVisibleText(dataSheet2Value);
            }

            case "SELECTBYVALUE" -> {
                assert element != null;
                Select select = new Select(element);
                Thread.sleep(3000);
                select.selectByValue(dataSheet2Value);
            }

            case "SELECTBYINDEX" -> {
                assert element != null;
                Select select = new Select(element);
                select.selectByIndex(Integer.parseInt(dataSheet2Value));
            }

            case "FRAMESWITCHUSINGLOCATOR" -> {
                driver.switchTo().frame(element);
                // System.out.println("Frame Switch Successfully Using Locator");
            }

            case "DEFAULTCONTENT" -> {
                driver.switchTo().defaultContent();
            }

            case "PARENTFRAME" -> {
                driver.switchTo().parentFrame();
            }

            case "FRAMECOUNT" -> {
                List<WebElement> count = elements;
                System.out.println("Iframe size are => " + count.size());
            }

            case "CLICKONPERTICULARAPPLICATIONID" -> {
                List<WebElement> Application_Elements = new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(PropertyValue)));
                System.out.println("===================================================" + Application_Elements.size());
                logger.info("CLICKONPERTICULARAPPLICATIONID: found {} candidate elements for xpath={}",
                        Application_Elements.size(), PropertyValue);
                for (WebElement ele : Application_Elements) {
                    String applicationNo = ele.getText();
                    System.out.println(applicationNo);
                    logger.info("CLICKONPERTICULARAPPLICATIONID: checking applicationNo={}", applicationNo);
                    if (applicationNo.equalsIgnoreCase(applicationID)) {
                        ele.click();
                        logger.info("CLICKONPERTICULARAPPLICATIONID: matched and clicked applicationID={}", applicationID);
                    }
                }
            }

            case "SCROLLWEBELEMENTUNTILVISIBLECENTER" -> {
                locatorManager.waitForPageReady();
                WebScrolling.ScrollwebElementUntilVisible(driver, element);
            }

            case "SCROLLWEBELEMENTUNTILVISIBLE" -> {
                locatorManager.waitForPageReady();
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView();", element);
            }

            case "BROWSERSTACKAI" -> {

                JavascriptExecutor js = (JavascriptExecutor) driver;

                String command = String.format(
                        "browserstack_executor: {\"action\":\"ai\",\"arguments\":[\"%s\"]}",
                        PropertyValue
                );

                boolean success = false;

                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {
                        Thread.sleep(3000);
                        Object result = js.executeScript(command);
                        String response = String.valueOf(result);
                        System.out.println("AI Attempt " + attempt + " Response : " + response);
                        logger.info("BROWSERSTACKAI attempt {} response={}", attempt, response);
                        if (response.contains("\"execution_status\":\"completed\"")) {
                            success = true;
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("AI Attempt " + attempt + " Failed : " + e.getMessage());
                        logger.warn("BROWSERSTACKAI attempt {} failed. reason={}", attempt, e.getMessage());
                    }
                    Thread.sleep(2000);
                }

                if (!success) {
                    throw new RuntimeException(
                            "BrowserStack AI action failed after 3 attempts. Command : "
                                    + PropertyValue);
                }
            }


            // SuperApp Custom Function =========================================
            case "SELECT_AUTOCOMPLETE_DROPDOWN" -> {
                locatorManager.waitForPageReady();
                By optionLocator = By.xpath("//li[@role='option' and normalize-space(.)='" + dataSheet2Value + "']");
                WebElement option = locatorManager.getWait()
                        .until(ExpectedConditions.presenceOfElementLocated(optionLocator));
                // ✅ Scroll into view
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", option);
                // ✅ Wait clickable
                locatorManager.getWait().until(ExpectedConditions.elementToBeClickable(option));
                // ✅ Click safely
                try {
                    option.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                }

            }

            case "WAIT_FOR_ELEMENT_VISIBLE" -> {
                locatorManager.waitForPageReady();
                int timeout = 300; // default 2 minutes 120

                By locator = By.xpath(PropertyValue);
                new WebDriverWait(driver, Duration.ofSeconds(timeout))
                        .until(ExpectedConditions.visibilityOfElementLocated(locator));

            }

            case "UNCHECK_IF_CHECKED" -> {
                locatorManager.waitForPageReady();
                for (int i = 1; i < elements.size(); i++) {
                    elements.get(i).click(); // uncheck
                    Thread.sleep(100);
                    // System.out.println("✅ Unchecked");

                }
            }

            case "SELECT_LISTBOX" -> {
                locatorManager.waitForPageReady();
                locatorManager.getWait()
                        .until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//li[@role='option' and normalize-space(.)='" + dataSheet2Value + "']")))
                        .click();
            }


            case "SCROLL_SELECTLISTBOX" -> {

                WebElement option = locatorManager.getWait()
                        .until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//li[normalize-space(.)='" + dataSheet2Value + "']")));

                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView({block:'center'});", option);

                option.click();
            }


            case "DIGILOCKER_PIN" -> {
                sendOtpPin(dataSheet2Value, PropertyValue);
            }

            default -> {

                boolean handled = false;

                if (Action.contains("WAIT")) {
                    handled = true;
                    String input = Action;
                    String digits = input.replaceAll("\\D+", "");
                    Thread.sleep(Integer.parseInt(digits));
                }

                if (Action.contains("WindowHandelByIndex")) {
                    handled = true;
                    String digit = getOnlyDigit(Action); // call the getdigit method to get the data
                    int window = Integer.parseInt(digit);
                    ArrayList<String> windowHandles = new ArrayList<>(driver.getWindowHandles());
                    System.out.println("Total window are ==============================> " + windowHandles.size());
                    driver.switchTo().window(windowHandles.get(window));
                }

                if (Action.contains("FRAMEINDEX")) {
                    handled = true;
                    String digit = getOnlyDigit(Action); // call the getdigit method to get the data
                    int index = Integer.parseInt(digit);
                    driver.switchTo().frame(index);
                    System.out.println("Frame Switch Successfully Using Index");
                }

                if (Action.contains("ScrollDown")) {
                    handled = true;
                    String digit = getOnlyDigit(Action); // call the getdigit method to get the data
                    int Scroll = Integer.parseInt(digit);
                    WebScrolling.scrollDown(driver, Scroll);
                }

                if (Action.contains("ScrollUp")) {
                    handled = true;
                    String digit = getOnlyDigit(Action); // call the getdigit method to get the data
                    int Scroll = Integer.parseInt(digit);
                    WebScrolling.scrollUp(driver, Scroll);
                }

                if (Action.contains("scrollwebElementUpAndDownUntilVisible")) { // Scrolling down the page till the
                    // webElement
                    handled = true;
                    String input = Action;
                    String digits = input.replaceAll("\\D+", "");
                    int Scroll = Integer.parseInt(digits);

                    WebScrolling.ScrollUpAndDownwebElementUntilVisible(driver, PropertyValue, Scroll);
                }

                if (Action.contains("UniversalPerfectScrollUpAndDownWebElementUntilVisible")) { // Scrolling down the
                                                                                                // page till the
                                                                                                // webElement
                    handled = true;
                    String input = Action;
                    String digits = input.replaceAll("\\D+", "");
                    int Scroll = Integer.parseInt(digits);

                    WebScrolling.UniversalPerfectScrollUpAndDownWebElementUntilVisible(driver, PropertyValue, Scroll);
                }

                // ✅ FIX: Log warning if no handler matched this action
                if (!handled) {
                    logger.warn("⚠️ Unknown/unhandled action: '{}' at SI_No={}, ScenarioID={}", Action, Si_No,
                            ScenarioID);
                    System.out.println("⚠️ Warning: Unknown action '" + Action + "' at SI_No=" + Si_No);
                }
            }
        }

        // ✅ DYNAMIC PAGE-NAVIGATION FLAG (Web only)
        // Instead of writing LocatorManager.pageNavigated=true inside every case block,
        // we check ONCE here after the action completes. Only triggers for Web
        // platform.
        // To add a new navigation action, just add it to PAGE_NAVIGATION_ACTIONS set
        // above.
        if (ConnectToMainController.PlatForm != null
                && ConnectToMainController.PlatForm.equalsIgnoreCase("Web")) {
            if (PAGE_NAVIGATION_ACTIONS.contains(Action.toUpperCase())
                    || Action.contains("WindowHandelByIndex")) {
                LocatorManager.pageNavigated = true;
            }
        }

    }

    public static String getOnlyDigit(String Action) { ///////// inside the bracket get only the digit

        String digit = null;
        Pattern pattern = Pattern.compile("\\((\\d+)\\)"); // Matches digits enclosed in parentheses
        Matcher matcher = pattern.matcher(Action);

        if (matcher.find()) {
            digit = matcher.group(1); // Extracts the digit(s) within the parentheses
        }
        // System.out.println("digit = " + digit);
        return digit;
    }

    private static boolean ensureElementPresent(String actionName) {
        if (element == null) {
            logger.error("Element is null for action={} at SI_No={}, PropertyName={}, PropertyValue={}",
                    actionName, Si_No, PropertyName, PropertyValue);
            return false;
        }
        return true;
    }

    private static void logActionStart(String actionName, String locatorValue) {
        logger.info("{} started at SI_No={}, locator={}", actionName, Si_No, locatorValue);
    }

    private static void logActionSuccess(String actionName, String locatorValue) {
        logger.info("{} succeeded at SI_No={}, locator={}", actionName, Si_No, locatorValue);
    }

    private static String extractFirstWord(String source, String actionName) {
        if (source == null || source.trim().isEmpty()) {
            logger.warn("{} skipped first-word extraction: source text is empty at SI_No={}", actionName, Si_No);
            return "";
        }

        String trimmed = source.trim();
        int idx = trimmed.indexOf(' ');
        if (idx <= 0) {
            logger.warn("{} fallback used: no space separator found in text='{}' at SI_No={}",
                    actionName, trimmed, Si_No);
            return trimmed;
        }
        return trimmed.substring(0, idx);
    }

    public static void performHorizontalSwipe(AndroidDriver driver, int startX, int endX, int startY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, startY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    // superApp Functions ========================

    public static void sendOtpPin(String pin, String idPrefix) throws InterruptedException {
        for (int i = 0; i < pin.length(); i++) {
            WebElement field = locatorManager.getWait().until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.id(idPrefix + (i + 1))));

            field.click(); // ensure focus
            field.sendKeys(String.valueOf(pin.charAt(i)));
            Thread.sleep(100); // give JS time to execute `move()`
        }
        // System.out.println("✅ PIN entered: ******");
    }

}
