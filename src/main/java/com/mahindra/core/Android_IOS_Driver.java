package com.mahindra.core;

import com.mahindra.actions.*;
import com.mahindra.config.*;
import com.mahindra.core.*;
import com.mahindra.utils.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Android_IOS_Driver {

	public static RemoteWebDriver driver;
	public static UiAutomator2Options options;
	public static URL url;
	public static Map<String, Object> browserstackOptions;

	public static String browserDriverFolderPath;
	public static String browserDriverPath;
	public static String OS;

	public final static Logger logger = LogManager.getLogger(Android_IOS_Driver.class.getName());

	public static void InitialisationDriverRemote() throws Exception {
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
		String formattedDate = formatter.format(now);
		String buildTagTime = new SimpleDateFormat("HH:mm:ss").format(now);
		String nameDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(now); // ✅ Date-Time for session name

		String RemoteURL = "https://" + MobileConfiguration.UserName + ":" + MobileConfiguration.AccessKey;

		browserstackOptions = new HashMap<String, Object>();
		options = new UiAutomator2Options();

		try {

			if(MobileConfiguration.TestingPlatform.equalsIgnoreCase("BrowserStack")) {
				String BrowserStackUrl = RemoteURL + "@hub-cloud.browserstack.com/wd/hub";

				if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("Android")) {


					// 1. Basic Device Capabilities
					options.setCapability("platformName", MobileConfiguration.DevicePlatform);
					options.setCapability("deviceName", MobileConfiguration.DeviceName);
					options.setCapability("platformVersion", MobileConfiguration.DevicePlatformVersion);

					String sapCode = System.getProperty("SAPCODE");
					String userDirName = (sapCode != null && !sapCode.trim().isEmpty())
							? sapCode
							: System.getProperty("user.name").toUpperCase();

					// 2. Test Identification
					options.setCapability("name", ConnectToMainController.ApplicationName + " - " + ConnectToMainController.VerticalName + " - " + ConnectToMainController.Scenario + " - "  + ConnectToMainController.ScenarioNo + " - " + nameDateTime);
					options.setCapability("buildName", ConnectToMainController.ApplicationName + " - " + ConnectToMainController.VerticalName + " - " +ConnectToMainController.ScenarioNo + " - " + GetUserName(userDirName) + "-QA");
					browserstackOptions.put("buildTag", "SANITY " + buildTagTime);


					// 3. App Management
					options.setCapability("cleanUp", true);

					if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
						options.setCapability("noReset", true);
						options.setCapability("fullReset", false);
					}

					if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
						options.setCapability("noReset", false);
						options.setCapability("fullReset", true);
					}

					if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
						options.setCapability("app", MobileConfiguration.AppPath);// bs://a1a4c73044c410ff61b3d725e6f510b031088676
//						capabilities.setCapability("app", MobileConfiguration.AppPath);// "BiswajitApp"   like this is the "custome id" for app upload
					}

//					options.setCapability("app", "bs://a1a4c73044c410ff61b3d725e6f510b031088676");


					//India location and GPS setup
//				    options.setCapability("geoLocation", "IN"); // Simulate country as India

					// 4. Location Services
					options.setCapability("locationServicesEnabled", true);
					options.setCapability("locationServicesAuthorized", true);


					// 5. BrowserStack Specific Features
					options.setCapability("browserstack.enableCameraImageInjection", true);
					options.setCapability("interactiveDebugging", true);
					options.setCapability("autoGrantPermissions", true);
					options.setCapability("disableWindowAnimation", true);



					// 6. Security Settings
					options.setCapability("acceptSslCerts", true);
					options.setCapability("ignoreUntrustedCertificate", true);
					options.setCapability("acceptInsecureCerts", true);




					// 7. BrowserStack Options (must be nested under bstack:options)
					browserstackOptions.put("timezone", "Kolkata");
					browserstackOptions.put("gpsLocation", "18.5451816,73.9096848");  // Set exact GPS to pune
					browserstackOptions.put("geoLocation", "IN");
					browserstackOptions.put("networkLogs", "true");
					browserstackOptions.put("debug", "true");
					browserstackOptions.put("appProfiling", "true");
					browserstackOptions.put("networkProfile", "4g-lte-good");
					browserstackOptions.put("deviceLogs", true);
					browserstackOptions.put("appiumLogs", true);
					browserstackOptions.put("consoleLogs", "verbose");
					browserstackOptions.put("projectName", ConnectToMainController.ApplicationName + "- Biswajit Framework");
					browserstackOptions.put("sessionName", "UDAAN_" + MobileConfiguration.Process +
							"_ANDROID_APP_" + formattedDate);
					


					//eSIM
//		            browserstackOptions.put("enableSim", "true");
//		            HashMap<String, String> simOptions = new HashMap<String, String>();
//		            simOptions.put("region","USA");
//		            browserstackOptions.put("simOptions", simOptions);

					// 9. Device Settings
					options.setCapability("orientation", "PORTRAIT");

					browserstackOptions.put("aiAuthoring", "true");


					// 10. Attach BrowserStack options
					options.setCapability("bstack:options", browserstackOptions);

//				capabilities.setCapability("appPackage", MobileConfiguration.App_PackageName);
//				capabilities.setCapability("appActivity", MobileConfiguration.App_PackageActivityName);

					url = new URL(BrowserStackUrl);
					driver = new AndroidDriver(url, options);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));

//					((AndroidDriver) driver).removeApp("bs://a1a4c73044c410ff61b3d725e6f510b031088676");  for removing the app




				} else if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("IOS")) {

					browserstackOptions.put("platformName", MobileConfiguration.DevicePlatform);
					browserstackOptions.put("deviceName", MobileConfiguration.DeviceName);
					browserstackOptions.put("platformVersion", MobileConfiguration.DevicePlatformVersion);
					browserstackOptions.put("name", MobileConfiguration.Process);

					if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
						browserstackOptions.put("noReset", true);
						browserstackOptions.put("fullReset", false);
					} else if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
						browserstackOptions.put("noReset", false);
						browserstackOptions.put("fullReset", true);
					} else if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
						browserstackOptions.put("APP", MobileConfiguration.AppPath);
					}

					browserstackOptions.put("appPackage", MobileConfiguration.App_PackageName);
					browserstackOptions.put("appActivity", MobileConfiguration.App_PackageActivityName);

					options.setCapability("bstack:options", browserstackOptions);

					url = new URL(BrowserStackUrl);
					driver = new IOSDriver(url, options);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));


				}
			}

			//**************************************************LambdaTest**************************************************

			else if(MobileConfiguration.TestingPlatform.equalsIgnoreCase("LambdaTest")) {

				String LambdaTestUrl = RemoteURL + "@mobile-hub.lambdatest.com/wd/hub";

				if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("Android")) {



					options.setCapability("platformName", MobileConfiguration.DevicePlatform);
					options.setCapability("deviceName", MobileConfiguration.DeviceName);
					options.setCapability("platformVersion", MobileConfiguration.DevicePlatformVersion);
					options.setCapability("name", MobileConfiguration.Process + " Biswajit Sahoo");

					if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
						options.setCapability("noReset", true);
						options.setCapability("fullReset", false);
					}

					if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
						options.setCapability("noReset", false);
						options.setCapability("fullReset", true);
					}

					if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
						options.setCapability("app", MobileConfiguration.AppPath);// bs://a1a4c73044c410ff61b3d725e6f510b031088676
					}

//				capabilities.setCapability("app", "lt://APP1016025291735894808487336");
					options.setCapability("enableCameraImageInjection", true);
					options.setCapability("interactiveDebugging", true);
					options.setCapability("build", "Udaan_" + MobileConfiguration.Process + " - BiswajitSahoo-QA");
					options.setCapability("name",
							"UDAAN_" + MobileConfiguration.Process + "_ANDROID_APP_" + formattedDate);
					options.setCapability("autoGrantPermissions", true);
					options.setCapability("debug", true);
					options.setCapability("video", true);
					options.setCapability("visual", true);
					options.setCapability("isRealMobile", true);
					options.setCapability("realMobileInteraction", true);
//					options.setCapability("acceptSslCerts", true);
//					options.setCapability("acceptInsecureCerts", true);
					options.setCapability("console", true); // Enable console logs
//			        options.setCapability("network", true); // Enable network logs


//				options.setCapability("appPackage", MobileConfiguration.App_PackageName);
//				options.setCapability("appActivity", MobileConfiguration.App_PackageActivityName);

					url = new URL(LambdaTestUrl);
					driver = new AndroidDriver(url, options);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));
					System.out.println("This is android remote site");

				} else if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("IOS")) {

					browserstackOptions.put("platformName", MobileConfiguration.DevicePlatform);
					browserstackOptions.put("deviceName", MobileConfiguration.DeviceName);
					browserstackOptions.put("platformVersion", MobileConfiguration.DevicePlatformVersion);
					browserstackOptions.put("name", MobileConfiguration.Process);

					if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
						browserstackOptions.put("noReset", true);
						browserstackOptions.put("fullReset", false);
					} else if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
						browserstackOptions.put("noReset", false);
						browserstackOptions.put("fullReset", true);
					} else if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
						browserstackOptions.put("APP", MobileConfiguration.AppPath);
					}

					browserstackOptions.put("appPackage", MobileConfiguration.App_PackageName);
					browserstackOptions.put("appActivity", MobileConfiguration.App_PackageActivityName);

					options.setCapability("bstack:options", browserstackOptions);

					url = new URL(LambdaTestUrl);
					driver = new IOSDriver(url, options);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));


				}
			}
		} catch (Exception e) {
			logger.error("❌ REMOTE driver initialization FAILED. Error: {}", e.getMessage(), e);
			e.printStackTrace();
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
			// mark this scenario FAILED, and continue with the next scenario.
			throw new RuntimeException("Remote driver initialization failed: " + e.getMessage(), e);
		}

	}

	public static void InitialisationDriverLocal() throws Exception {

		logger.info("Initializing LOCAL driver. Platform={}, DeviceName={}, PlatformVersion={}",
				MobileConfiguration.DevicePlatform, MobileConfiguration.DeviceName, MobileConfiguration.DevicePlatformVersion);

		try {

			if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("Android")) {

				options = new UiAutomator2Options();

				options.setAutomationName("UiAutomator2");
				options.setPlatformName(MobileConfiguration.DevicePlatform);
				options.setDeviceName(MobileConfiguration.DeviceName);
				//options.setUdid(MobileConfiguration.DeviceName);
				options.setPlatformVersion(MobileConfiguration.DevicePlatformVersion);
				options.setCapability("name", MobileConfiguration.Process); // custom capability if needed

				if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
					options.setNoReset(true);
					options.setFullReset(false);
					logger.info("App reset mode: NO (noReset=true, fullReset=false)");
				} else if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
					options.setNoReset(false);
					options.setFullReset(true);
					logger.info("App reset mode: YES (noReset=false, fullReset=true)");
				} else if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
					options.setApp(MobileConfiguration.AppPath); // set APK path
					logger.info("Setting APK path: {}", MobileConfiguration.AppPath);
				}

				options.setAppPackage(MobileConfiguration.App_PackageName);
				options.setAppActivity(MobileConfiguration.App_PackageActivityName);
				options.setCapability("forceAppLaunch", true);
				logger.info("AppPackage={}, AppActivity={}", MobileConfiguration.App_PackageName, MobileConfiguration.App_PackageActivityName);


//				url = new URL("http://" + MobileConfiguration.AppiumPort + "/wd/hub");
//				url = new URL("http://" + MobileConfiguration.AppiumPort + "/");  //for commandline appium url
//				url = new URL("http://localhost:4723/"); for cmd
				logger.info("Starting local Appium server on port: {}", MobileConfiguration.AppiumPort);
				url = new URL(fnStartAppiumServerLocal(Integer.parseInt(MobileConfiguration.AppiumPort)).toString());
				logger.info("Appium server URL resolved: {}", url);


				driver = new AndroidDriver(url, options);
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));
				logger.info("✅ Android LOCAL driver initialized successfully. ImplicitWait={}s", ConnectToMainController.ImplicityWait);

			} else if (MobileConfiguration.DevicePlatform.equalsIgnoreCase("IOS")) {

				XCUITestOptions options = new XCUITestOptions();
				options.setPlatformName(MobileConfiguration.DevicePlatform);
				options.setDeviceName(MobileConfiguration.DeviceName);
				options.setPlatformVersion(MobileConfiguration.DevicePlatformVersion);
				options.setCapability("name", MobileConfiguration.Process); // custom capability if needed

				if (MobileConfiguration.AppReset.equalsIgnoreCase("NO")) {
					options.setNoReset(true);
					options.setFullReset(false);
				} else if (MobileConfiguration.AppReset.equalsIgnoreCase("YES")) {
					options.setNoReset(false);
					options.setFullReset(true);
				} else if (MobileConfiguration.Pre_InstalledApp.equalsIgnoreCase("NO")) {
					options.setApp(MobileConfiguration.AppPath); // set IPA path
				}

				// appPackage and appActivity are Android specific, no need for iOS.
				// For iOS, you usually set "bundleId" (if needed):
				// options.setBundleId(MobileConfiguration.App_PackageName);

				url = new URL("http://" + MobileConfiguration.AppiumPort + "/wd/hub");
				driver = new IOSDriver(url, options);
				driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));
				logger.info("✅ iOS LOCAL driver initialized successfully. ImplicitWait={}s", ConnectToMainController.ImplicityWait);
			}
		} catch (Exception e) {
			logger.error("❌ LOCAL driver initialization FAILED. Platform={}, Port={}, Error: {}",
					MobileConfiguration.DevicePlatform, MobileConfiguration.AppiumPort, e.getMessage(), e);
			e.printStackTrace();
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
			// mark this scenario FAILED, and continue with the next scenario.
			throw new RuntimeException("Local driver initialization failed: " + e.getMessage(), e);
		}


	}




	public static void Initialisation(String browser) throws IOException {

		if (browser.equalsIgnoreCase("chrome")) {

//				File tempProfile = Files.createTempDirectory("chromeProfile").toFile();
//				tempProfile.deleteOnExit(); // Optional: clean up after test

			//BrowserDriverFolder(browser);
			//System.setProperty("webdriver.chrome.driver", browserDriverPath);
			ChromeOptions option = new ChromeOptions();
			option.addArguments("--remote-allow-origins=*");
			option.addArguments("--no-sandbox");
			option.addArguments("--incognito");
			option.addArguments("--disable-notifications");
			option.addArguments("--disable-cache");
			option.addArguments("--disable-popup-blocking");

//				option.addArguments("--user-data-dir=" + tempProfile.getAbsolutePath());


//				 option.addArguments("--disable-application-cache");
//				 option.addArguments("--no-default-browser-check");
//				 option.addArguments("--no-first-run");
//				 option.addArguments("--disable-infobars");
//			     option.addArguments("--disable-sync");


			// Optional: Remove "Chrome is being controlled by automated test software" message
			option.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
			option.setExperimentalOption("useAutomationExtension", false);
			option.addArguments("--disable-background-networking");
			option.addArguments("--disable-background-timer-throttling");
			option.addArguments("--disable-renderer-backgrounding");


			// Disable password manager
			option.addArguments("--disable-save-password-bubble");
			option.setExperimentalOption("prefs", new HashMap<String, Object>() {{
				put("credentials_enable_service", false);
				put("profile.password_manager_enabled", false);
			}});



			option.setPageLoadStrategy(PageLoadStrategy.NORMAL);
//				option.addArguments("--headless");

			driver = new ChromeDriver(option);

			Dimension dimension = new Dimension(1296, 688);
//			driver.manage().window().setSize(dimension);
			driver.manage().deleteAllCookies();

//				Dimension newSize = driver.manage().window().getSize();
//				System.out.println("Browser width : " + dimension.getWidth());
//				System.out.println("Browser height: " + dimension.getHeight());


			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));


		}

		else if (browser.equalsIgnoreCase("GitHubChrome")) {

			ChromeOptions option = new ChromeOptions();

// Essential for GitHub Actions / headless Linux
			option.addArguments("--headless=new");  // Modern headless mode (recommended since Chrome 109+)
			option.addArguments("--no-sandbox");    // Required in root/container environments
			option.addArguments("--disable-dev-shm-usage");  // Avoids shared memory issues
			option.addArguments("--disable-gpu");   // Often needed in headless
			option.addArguments("--remote-debugging-port=9222");  // Helps avoid port conflicts

// Your existing arguments (keep these)
			option.addArguments("--remote-allow-origins=*");
			option.addArguments("--incognito");
			option.addArguments("--disable-notifications");
			option.addArguments("--disable-cache");
			option.addArguments("--disable-popup-blocking");
			option.addArguments("--disable-background-networking");
			option.addArguments("--disable-background-timer-throttling");
			option.addArguments("--disable-renderer-backgrounding");
			option.addArguments("--disable-save-password-bubble");

// Experimental options (keep these)
			option.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
			option.setExperimentalOption("useAutomationExtension", false);
			option.setExperimentalOption("prefs", new HashMap<String, Object>() {{
				put("credentials_enable_service", false);
				put("profile.password_manager_enabled", false);
			}});

			option.setPageLoadStrategy(PageLoadStrategy.NORMAL);

// Remove or comment out the temp profile line if you were using it (not needed in CI)
// option.addArguments("--user-data-dir=" + tempProfile.getAbsolutePath());

			driver = new ChromeDriver(option);

// Your window size (headless respects this)
			Dimension dimension = new Dimension(1296, 688);
			driver.manage().window().setSize(dimension);
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));
		}

		else if (browser.equalsIgnoreCase("edge")) {
			EdgeOptions option = new EdgeOptions();
			option.addArguments("--remote-allow-origins=*");
			option.addArguments("--no-sandbox");
			option.addArguments("--incognito");
			option.addArguments("--disable-notifications");
			option.addArguments("--disable-cache");
			option.addArguments("--disable-popup-blocking");
			option.addArguments("--disable-dev-shm-usage");
			option.addArguments("--disable-extensions");
			option.addArguments("--disable-blink-features=AutomationControlled");
			option.addArguments("--start-maximized");
			option.addArguments("--disable-infobars");
			option.addArguments("--disable-gpu");
			//option.addArguments("--headless=new");
			option.addArguments("--disable-background-networking");
			option.addArguments("--disable-background-timer-throttling");
			option.addArguments("--disable-renderer-backgrounding");

			// Automation flags
			option.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
			option.setExperimentalOption("useAutomationExtension", false);

			// Optional: Set custom preferences (e.g. download location)
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("download.prompt_for_download", false);
			prefs.put("profile.default_content_settings.popups", 0);
			option.setExperimentalOption("prefs", prefs);

			option.setPageLoadStrategy(PageLoadStrategy.NORMAL);

			driver = new EdgeDriver(option);

			Dimension dimension = new Dimension(1296, 688);
			driver.manage().window().setSize(dimension);
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));


		}

		else if (browser.equalsIgnoreCase("BrowserStack")) {

			String USERNAME = "biswajitsahoo_0n9ypv";
			String ACCESS_KEY = "qZHZfSFttvdThCVVX6Ki";
			String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

			// BrowserStack capabilities
			Map<String, Object> browserstackOptions = new HashMap<>();
			browserstackOptions.put("os", "Windows");
			browserstackOptions.put("osVersion", "11");
			browserstackOptions.put("browserName", "Chrome");
			browserstackOptions.put("browserVersion", "137");
			browserstackOptions.put("projectName", ConnectToMainController.ApplicationName + "- Biswajit Framework");
			browserstackOptions.put("buildName", "Udaan_" + ConnectToDataSheet.Module + " - " + System.getProperty("user.name").toUpperCase());
			browserstackOptions.put("sessionName", "UDAAN_SFDC_" + ConnectToDataSheet.Module + " - WEB_APP");
			browserstackOptions.put("buildTag", "SANITY");

			browserstackOptions.put("debug", "true");
			browserstackOptions.put("networkLogs", "true");
			browserstackOptions.put("consoleLogs", "verbose");
//			browserstackOptions.put("performance", "report");
			browserstackOptions.put("selfHeal", "true");

			// Chrome options to disable cache/popups/etc.
			Map<String, Object> chromeOptions = new HashMap<>();
			chromeOptions.put("args", new String[]{
					"--incognito", // this ensures fresh session every time
					"--disable-notifications",
					"--disable-popup-blocking",
					"--disable-extensions",
					"--disable-cache",
					"--disable-application-cache",
					"--disable-gpu",
					"--no-sandbox",
					"--start-maximized"
			});


			MutableCapabilities caps = new MutableCapabilities();
			caps.setCapability("bstack:options", browserstackOptions);
			caps.setCapability("goog:chromeOptions", chromeOptions);

			// Start remote browser
			driver = new RemoteWebDriver(new URL(URL), caps);

			// Set screen size manually
			Dimension dimension = new Dimension(1296, 688);
			driver.manage().window().setSize(dimension);

			// Set implicit wait
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Long.parseLong(ConnectToMainController.ImplicityWait)));

		}

		else {
			logger.error("SORRY!!! Invalid browser selected: '{}'", browser);
			System.out.println("SORRY!!! U Choose InvalidBrowser: " + browser);
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
			// mark this scenario FAILED, and continue with the next scenario.
			throw new RuntimeException("Invalid browser selected: " + browser);
		}

	}

	/*

	// Store service as a static field so it can be accessed and stopped anytime
	private static AppiumDriverLocalService service;

	public static URL  fnStartAppiumServerLocal(int intPortNumber) {

		logger.info("===== Starting Local Appium Server on Port: {} =====", intPortNumber);

		// ✅ STEP 1: Stop existing service if already running
		if (service != null && service.isRunning()) {
			logger.info("Appium service already running. Stopping it first...");
			System.out.println("Appium service already running. Stopping it first...");
			try {
				service.stop();
				logger.info("Existing Appium service stopped successfully.");
				System.out.println("Existing Appium service stopped.");
			} catch (Exception e) {
				logger.error("Failed to stop existing Appium service: {}", e.getMessage(), e);
			}
		}

		// ✅ STEP 2: Also kill any zombie process occupying the port (safety net)
		logger.info("Checking for zombie processes on port: {}", intPortNumber);
		fnKillPortIfOccupied(intPortNumber);

		// ✅ STEP 3: Build and start fresh service
		try {
			String logFilePath = System.getProperty("user.dir") + File.separator + "appiumLogs.txt";
			logger.info("Building Appium service. IP=127.0.0.1, Port={}, LogFile={}", intPortNumber, logFilePath);

			service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					.withIPAddress("127.0.0.1")
					.usingPort(intPortNumber)
					.withArgument(GeneralServerFlag.SESSION_OVERRIDE)
					.withLogFile(new File(logFilePath)));

			// ✅ This one line stops ALL Appium logs from printing to console
			service.clearOutPutStreams();

			logger.info("Starting Appium service...");
			service.start();

			logger.info("✅ Local Appium Server STARTED successfully. URL: {}", service.getUrl());
			System.out.println("******************************************************");
			System.out.println("Local Appium is Started URL :- " + service.getUrl());
			System.out.println("******************************************************");

		} catch (Exception e) {
			logger.error("❌ FAILED to start Local Appium Server on port {}. Error: {}", intPortNumber, e.getMessage(), e);
			System.out.println("❌ FAILED to start Appium server on port " + intPortNumber + ": " + e.getMessage());
			throw new RuntimeException("Appium server start failed on port " + intPortNumber, e);
		}

		// ✅ STEP 4: Register shutdown hook — auto stop when JVM exits
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (service != null && service.isRunning()) {
				service.stop();
				logger.info("Appium service stopped via shutdown hook.");
				System.out.println("Appium service stopped via shutdown hook.");
			}
		}));

		return service.getUrl();
	}
*/

	// ═══════════════════════════════════════════════════════════════
// HELPER METHOD: Runs system command, returns first valid output
// ═══════════════════════════════════════════════════════════════
	private static String getCommandOutput(String command, boolean isWindows) {
		try {
			Process process = Runtime.getRuntime().exec(
					isWindows ? new String[]{"cmd", "/c", command}
							: new String[]{"sh", "-c", command}
			);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			String result = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.endsWith(".cmd")) {
					result = line;
					break;
				}
				if (result == null && !line.isEmpty()) {
					result = line.replace(".cmd", "");
				}
			}
			reader.close();
			process.waitFor();
			if (result != null) return result;
		} catch (Exception e) {
			logger.error("Command failed [{}]: {}", command, e.getMessage());
		}
		throw new RuntimeException("❌ Command failed: " + command + ". Ensure it is installed and in PATH.");
	}


	// ═══════════════════════════════════════════════════════════
// MAIN METHOD: Start Appium Server
// ═══════════════════════════════════════════════════════════
	private static AppiumDriverLocalService service;

	public static URL fnStartAppiumServerLocal(int intPortNumber) {

		logger.info("===== Starting Local Appium Server on Port: {} =====", intPortNumber);

		// STEP 1: Stop existing service if already running
		if (service != null && service.isRunning()) {
			logger.info("Appium service already running. Stopping it first...");
			try {
				service.stop();
				logger.info("Existing Appium service stopped successfully.");
			} catch (Exception e) {
				logger.error("Failed to stop existing Appium service: {}", e.getMessage(), e);
			}
		}

		// STEP 2: Kill any zombie process on the port
		fnKillPortIfOccupied(intPortNumber);

		// STEP 3: Build and start fresh service
		try {
			String logFilePath = System.getProperty("user.dir") + File.separator + "appiumLogs.txt";
			String os = System.getProperty("os.name").toLowerCase();
			boolean isWindows = os.contains("win");

			// ── Dynamic Path Detection ──
			String nodePath = getCommandOutput(isWindows ? "where node" : "which node", isWindows);
			String appiumCmdPath = getCommandOutput(isWindows ? "where appium" : "which appium", isWindows);

			File appiumDir = new File(appiumCmdPath.trim()).getParentFile();

			// Try location 1: <appium_dir>/node_modules/appium/build/lib/main.js
			File mainJS = new File(appiumDir, "node_modules/appium/build/lib/main.js");

			// Try location 2: <appium_dir>/../lib/node_modules/appium/build/lib/main.js
			if (!mainJS.exists()) {
				mainJS = new File(appiumDir.getParent(), "lib/node_modules/appium/build/lib/main.js");
			}

			// Safety check
			if (!mainJS.exists()) {
				throw new RuntimeException("❌ Appium main.js not found! Run: npm install -g appium");
			}

			logger.info("Node: {}", nodePath);
			logger.info("Appium main.js: {}", mainJS.getAbsolutePath());

			// ── Build Service ──
			service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					.withIPAddress("127.0.0.1")
					.usingPort(intPortNumber)
					.usingDriverExecutable(new File(nodePath.trim()))
					.withAppiumJS(mainJS)
					.withArgument(GeneralServerFlag.SESSION_OVERRIDE)
					.withLogFile(new File(logFilePath)));

			service.clearOutPutStreams();
			service.start();

			logger.info("✅ Local Appium Server STARTED. URL: {}", service.getUrl());
			System.out.println("******************************************************");
			System.out.println("Local Appium is Started URL :- " + service.getUrl());
			System.out.println("******************************************************");

		} catch (Exception e) {
			logger.error("❌ FAILED to start Appium on port {}: {}", intPortNumber, e.getMessage(), e);
			throw new RuntimeException("Appium server start failed on port " + intPortNumber, e);
		}

		// STEP 4: Auto-stop on JVM exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (service != null && service.isRunning()) {
				service.stop();
				logger.info("Appium service stopped via shutdown hook.");
			}
		}));

		return service.getUrl();
	}

	// ✅ Kills any process using the given port (Windows + Mac/Linux support)
	private static void fnKillPortIfOccupied(int port) {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			logger.info("Checking if port {} is occupied on OS: {}", port, os);

			if (os.contains("win")) {
				// Windows: find PID using the port and kill it
				Process findProcess = Runtime.getRuntime().exec("cmd /c netstat -ano | findstr :" + port);
				BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("LISTENING")) {
						String[] parts = line.trim().split("\\s+");
						String pid = parts[parts.length - 1];
						Runtime.getRuntime().exec("taskkill /F /PID " + pid);
						logger.info("Killed process with PID {} on port {} (Windows)", pid, port);
						System.out.println("Killed process with PID " + pid + " on port " + port);
					}
				}
			} else {
				// Mac/Linux: use lsof to find and kill process
				Process findProcess = Runtime.getRuntime().exec(new String[]{"sh", "-c", "lsof -ti:" + port});
				BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
				String pid = reader.readLine();
				if (pid != null && !pid.isEmpty()) {
					Runtime.getRuntime().exec(new String[]{"sh", "-c", "kill -9 " + pid});
					logger.info("Killed process with PID {} on port {} (Mac/Linux)", pid, port);
					System.out.println("Killed process with PID " + pid + " on port " + port);
				} else {
					logger.info("No process found occupying port {}. Port is free.", port);
				}
			}

			Thread.sleep(1000); // Wait for port to be fully released

		} catch (Exception e) {
			logger.warn("Could not kill port {}: {}", port, e.getMessage(), e);
			System.out.println("Could not kill port " + port + ": " + e.getMessage());
		}
	}


	// ✅ Call this explicitly in @AfterSuite / teardown
	public static void fnStopAppiumServer() {
		if (service != null && service.isRunning()) {
			service.stop();
//			System.out.println("Appium service stopped.");
			logger.info("Appium service stopped.");
		}
	}

	public static boolean isDriverAlive(WebDriver driver) {
		if (driver == null) return false;
		try {
			driver.getTitle(); // will throw NoSuchSessionException if session is dead
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String GetUserName(String name){


		HashMap<String, String> map = new HashMap<>();
		map.put("100005482", "BISWAJIT");
		map.put("27044316", "VIKRANT");
		map.put("27042554", "NAMRATA");
		map.put("27042579", "DINESH");
		map.put("27026940", "MOHINI");
		map.put("27039698", "KALYANI");
		map.put("27036109", "SHUBHAM");
		map.put("27041437", "SHANTESH");


		for(Map.Entry<String, String> entry: map.entrySet()){
			if(name.contains(entry.getKey())){
				return entry.getValue();
			}
		}
        return name;
    }



}
