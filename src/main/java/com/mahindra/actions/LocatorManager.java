package com.mahindra.actions;

import com.mahindra.core.*;
import io.appium.java_client.AppiumBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LocatorManager extends ConnectToDataSheet {

	public static int failedPassedLocatorCount;
	public static boolean isElementFailedReachedMaxLimit;
	public static String locatorElementName;

	// ✅ True ONLY when LocatorManager successfully located the element.
	// CLICK checks this flag — if false, element was never found, so skip
	// the elementToBeClickable wait immediately (no extra 30s on a failed step).
	public static boolean elementFoundByLocator = false;

	// ✅ Page-ready checks only run after navigation actions (BROWSERURL,
	// PAGEREFRESH, etc.)
	// This avoids running readyState + loader + jQuery checks on every single
	// element find
	public static boolean pageNavigated = false;

	// Element cache to avoid re-locating same element consecutively
	public static String cachePropertyName = null;
	public static String cachePropertyValue = null;

	final By LOADER_POPUP = By.xpath(
			"//div[contains(translate(@class, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'loader') or contains(translate(@class, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'spinner')]");
	final By LOADER_TEXT = By.xpath(
			"//*[contains(normalize-space(text()),'Please wait while we are processing') or contains(normalize-space(text()),'We are fetching your details') or contains(normalize-space(text()),'Please do not close the app')]");

	public final static Logger logger = LogManager.getLogger(LocatorManager.class.getName());

	private WebDriverWait sharedWait = null;
	private WebDriver lastDriver = null;

	public LocatorManager() {
		// Constructor intentional empty. Wait is lazy-loaded to prevent null driver.
	}

	public WebDriverWait getWait() {
		if (sharedWait == null || driver != lastDriver) {
			if (driver == null) {
				throw new IllegalStateException(
						"WebDriver is null! Ensure 'APP LUNCH' or 'BROWSER LUNCH' action is executed before finding elements.");
			}
			long waitTime = Long.parseLong(ConnectToMainController.ExplicityWait);
			sharedWait = new WebDriverWait(driver, Duration.ofSeconds(waitTime));
			lastDriver = driver;
		}
		return sharedWait;
	}

	/**
	 * Resolves the retry count from DATASHEET config.
	 * Returns 0 if Retry is not "Y" or RetryCount is not configured.
	 */
	private int getMaxRetryCount() {
		if (ConnectToMainController.Retry == null ||
				!ConnectToMainController.Retry.equalsIgnoreCase("Y")) {
			return 0;
		}
		try {
			int count = Integer.parseInt(ConnectToMainController.RetryCount);
			return Math.max(0, count);
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}

	/**
	 * Core locator resolution method.
	 * Maps PropertyName/PropertyValue from the DataSheet to a Selenium By locator,
	 * waits for the element, then delegates to extractTestData() for action
	 * execution.
	 * ✅ Optimizations applied:
	 * - Removed redundant object creation (LocatorManager/Function created once in
	 * ConnectToDataSheet)
	 * - Removed duplicate presenceOfElementLocated + visibilityOfElementLocated for
	 * Web (only visibility needed)
	 * - Uses element cache to avoid re-locating consecutively identical locators
	 * - Retry mechanism: if Retry=Y in MainController, retries element find up to
	 * RetryCount times per step
	 */
	public void mapToLocator() {

		// Reset flag at the start of every step — element not found yet
		elementFoundByLocator = false;

		locatorElementName = Si_No + "-" + Module + "-" + ScenarioID + "-" + TestCaseID + "-" + TestCaseStepID;

		// ── Use cached element if PropertyName+PropertyValue haven't changed ──
		if (PropertyName != null && !PropertyName.isEmpty() &&
				PropertyValue != null && !PropertyValue.isEmpty() &&
				cachePropertyName != null && cachePropertyValue != null &&
				cachePropertyName.equalsIgnoreCase(PropertyName) &&
				cachePropertyValue.equalsIgnoreCase(PropertyValue) &&
				webElement != null) {

			// ✅ FIX: Cache hit — element IS found (same as the retry-loop success path).
			// Must set elementFoundByLocator = true here too, otherwise the NEXT step's
			// CLICK guard sees false and skips the click with 0ms execution time.
			elementFoundByLocator = true;
			try {
				ConnectToDataSheet.extractTestData();
				return;
			} catch (StaleElementReferenceException staleEx) {
				logger.warn("StaleElementReferenceException: cached element invalid, re-fetching...");
				cachePropertyName = null;
				cachePropertyValue = null;
				webElement = null;
				webElements = null;
				mapToLocator();
				return;
			} catch (Exception e) {
				logger.warn("Cached element error: {}. Clearing cache and re-fetching...", e.getMessage());
				cachePropertyName = null;
				cachePropertyValue = null;
				webElement = null;
				webElements = null;
				mapToLocator();
				return;
			}
		}

		// ── Clear cache and locate fresh element ──
		webElement = null;
		webElements = null;
		cachePropertyName = null;
		cachePropertyValue = null;

		By by = null;

		if (PropertyName != null && !PropertyName.isEmpty() &&
				PropertyValue != null && !PropertyValue.isEmpty()) {

			// ── Build the By locator from PropertyName ──
			try {
				by = switch (PropertyName.toLowerCase()) {
					case "xpath" -> By.xpath(PropertyValue);
					case "id" -> By.id(PropertyValue);
					case "name" -> By.name(PropertyValue);
					case "classname" -> By.className(PropertyValue);
					case "css" -> By.cssSelector(PropertyValue);
					case "tagname" -> By.tagName(PropertyValue);
					case "linktext" -> By.linkText(PropertyValue);
					case "partiallinktext" -> By.partialLinkText(PropertyValue);
					case "accessibilityid" -> AppiumBy.accessibilityId(PropertyValue);
					case "uiautomator" -> AppiumBy.androidUIAutomator(PropertyValue);
					default -> throw new IllegalArgumentException("Unsupported PropertyName type: " + PropertyName);
				};
			} catch (IllegalArgumentException e) {
				logger.error("❌ Locator creation failed: {}", e.getMessage());
				return;
			}

			// ── Retry loop: attempt = 0 is the first try, then retries up to maxRetry ──
			int maxRetry = getMaxRetryCount();
			Exception lastException = null;
			boolean elementFound = false;

			for (int attempt = 0; attempt <= maxRetry; attempt++) {

				try {
					// ── Wait for element based on platform ──
					if (ConnectToMainController.PlatForm.equalsIgnoreCase("Mobile")) {
						webElement = getWait().until(ExpectedConditions.visibilityOfElementLocated(by));

					} else if (ConnectToMainController.PlatForm.equalsIgnoreCase("Web")) {
						// Only run page-ready checks after navigation actions
						if (pageNavigated) {
							new WebDriverWait(driver, Duration.ofSeconds(15))
									.until(d -> ((JavascriptExecutor) d)
											.executeScript("return document.readyState").equals("complete"));
							waitForPageReady();
							pageNavigated = false;
						}
						// visibilityOfElementLocated already ensures presence + visibility
						webElement = getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
						webElements = driver.findElements(by);
					}

					// ✅ Element was located successfully → set flag so CLICK knows
					// the element IS on screen (and can use full 30s clickability wait)
					elementFoundByLocator = true;

					// ── SUCCESS: Execute the action ──
					// Moving this INSIDE the retry loop. If the action (e.g. CLICK) fails,
					// it throws an exception, gets caught, and triggers the next retry.
					ConnectToDataSheet.extractTestData();

					// ✅ Element found and action executed successfully
					elementFound = true;
					cachePropertyName = PropertyName;
					cachePropertyValue = PropertyValue;
					failedPassedLocatorCount = 0;
					break; // exit retry loop on success

				} catch (Exception e) {
					lastException = e;

					if (attempt < maxRetry) {
						// ── Retry: log attempt and wait briefly before next try ──
						logger.warn("🔄 RETRY [{}/{}] Action/Locate failed → {} = {} | Reason: {}",
								(attempt + 1), maxRetry, PropertyName, PropertyValue, e.getMessage());
						System.out.println("🔄 RETRY [" + (attempt + 1) + "/" + maxRetry
								+ "] Action/Locate failed → " + PropertyName + " = " + PropertyValue);

						// ✅ FIX: Force the next attempt to wait for loaders (waitForPageReady)
						// If the click failed because a loader popped up and blocked the element,
						// we need to wait for it to disappear on the next attempt!
						pageNavigated = true;

						try {
							Thread.sleep(2000); // 2 second wait between retries
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
					}
					// on last attempt, fall through to failure handling below
				}
			}

			if (!elementFound) {
				// ── ALL ATTEMPTS FAILED: Detailed error reporting ──
				cachePropertyName = null;
				cachePropertyValue = null;
				fail++;
				// scenarioHasFailed is ONLY set by AbortCondition() when the configured
				// threshold of consecutive failures is reached. A single step failure
				// does NOT mark the scenario as failed on its own.
				// ✅ Detailed structured error log (not single-line)
				printDetailedError(lastException, maxRetry);

				AbortCondition();

				if (!Action.equalsIgnoreCase("CheckVisibility")) {
					try {
						utilsActivity.withOutValidationFailTestCase(lastException);
						ConnectToDataSheet.extractTestData();// if element is failed, then also call to extractTestData() method.
					} catch (Exception utilEx) {
						logger.error("❌ Fail screenshot error: {}", utilEx.getMessage());
					}
				} else {
					// ✅ FIX: CheckVisibility element not found → still route to Action class
					// so the failure is properly recorded in the Extent Report and
					// failedValidations is incremented. Without this, the step was silently
					// skipped and never marked as failed in the report.
					try {
						ConnectToDataSheet.extractTestData();
					} catch (Exception cvEx) {
						logger.error("❌ CheckVisibility failure reporting error: {}", cvEx.getMessage());
					}
				}
			}

		} else {
			// No locator specified — direct action execution (e.g., WAIT, QUIT, BROWSERURL)
			try {
				ConnectToDataSheet.extractTestData();
			} catch (Exception dataEx) {
				logger.error("❌ Data extraction failed (no locator): {}", dataEx.getMessage());
			}
		}
	}

	/**
	 * Prints a detailed, structured error block to console and logger
	 * when element finding fails — much more useful than a single-line error.
	 */
	private void printDetailedError(Exception e, int retryAttempts) {
		String rootCause = extractRootCause(e);

		String errorBlock = String.format(
				"%n╔══════════════════════════════════════════════════════════════════╗%n" +
						"║  ❌ ELEMENT NOT FOUND                                            ║%n" +
						"╠══════════════════════════════════════════════════════════════════╣%n" +
						"║  Step ID       : %-46s  %n" +
						"║  SI_No         : %-46s  ║%n" +
						"║  ScenarioID    : %-46s  ║%n" +
						"║  Action        : %-46s  ║%n" +
						"║  PropertyName  : %-46s  ║%n" +
						"║  PropertyValue : %-46s  ║%n" +
						"║  Retry         : %-46s  ║%n" +
						"║  Root Cause    : %-46s  %n" +
						"╚══════════════════════════════════════════════════════════════════╝",
				locatorElementName != null ? locatorElementName : "N/A",
				Si_No != null ? Si_No : "N/A",
				ScenarioID != null ? ScenarioID : "N/A",
				Action != null ? Action : "N/A",
				PropertyName != null ? PropertyName : "N/A",
				truncate(PropertyValue, 46),
				retryAttempts > 0 ? "YES (" + retryAttempts + " retries exhausted)" : "OFF",
				truncate(rootCause, 100));

		System.out.println(errorBlock);
		logger.error(errorBlock);
	}

	/**
	 * Extracts the actual root cause from nested exceptions.
	 * Converts common Selenium exceptions to human-readable messages.
	 */
	private String extractRootCause(Exception e) {
		if (e == null)
			return "Unknown error";

		String message = e.getMessage();
		if (message == null)
			message = e.getClass().getSimpleName();

		// ── Map common Selenium exceptions to clear messages ──
		if (e instanceof TimeoutException) {
			return "Element not visible within ExplicitWait timeout (" + ConnectToMainController.ExplicityWait + "s)";
		}
		if (e instanceof NoSuchElementException) {
			return "Element does not exist in DOM";
		}
		if (e instanceof StaleElementReferenceException) {
			return "Element was found but became stale (page refreshed/DOM changed)";
		}
		if (e instanceof ElementNotInteractableException) {
			return "Element exists but is not interactable (hidden/disabled/overlapped)";
		}
		if (e instanceof InvalidSelectorException) {
			return "Invalid locator syntax: " + truncate(PropertyValue, 30);
		}
		if (message.contains("element is null") || message.contains("\"element\" is null")) {
			return "Element was null — locator did not match any element on page";
		}

		// ── Fallback: first line of error message ──
		int newlineIdx = message.indexOf('\n');
		return newlineIdx > 0 ? message.substring(0, Math.min(newlineIdx, 80)) : truncate(message, 80);
	}

	/**
	 * Safely truncates a string to maxLen characters.
	 */
	private String truncate(String str, int maxLen) {
		if (str == null)
			return "N/A";
		return str.length() <= maxLen ? str : str.substring(0, maxLen - 3) + "...";
	}

	public void waitForPageReady() {
		try {
			// 1️⃣ Only wait for loader popup if it IS actually present in DOM
			// FIX: invisibilityOfElementLocated returns true immediately when element is
			// NOT found.
			// This was a false-positive — the loader was present but xpath didn't match,
			// so it returned "invisible" instantly and didn't wait at all.
			waitForInvisibleIfPresent(LOADER_POPUP);

			// 2️⃣ Wait loader text gone (extra safety)
			waitForInvisibleIfPresent(LOADER_TEXT);

			// 3️⃣ Wait AJAX/jQuery if used
			getWait().until(d -> {
				try {
					return (Boolean) ((JavascriptExecutor) d).executeScript(
							"return (window.jQuery == undefined) || (jQuery.active == 0)");
				} catch (Exception e) {
					return true;
				}
			});
		} catch (TimeoutException te) {
			logger.warn("⚠️ Loader did not disappear in time → continuing");
		} catch (Exception e) {
			logger.warn("⚠️ waitForPageReady issue: {}", e.getMessage());
		}
	}

	/**
	 * ✅ FIXED version of waitForInvisible.
	 * OLD BUG: Selenium's invisibilityOfElementLocated() returns TRUE immediately
	 * when the element is NOT found in the DOM at all.
	 * This caused false-positives: loader wasn't found by xpath → treated as
	 * "already invisible" → framework moved on while loader was still on screen.
	 * NEW LOGIC: First check if the loader IS present. If yes, wait for it to go.
	 * If not present → skip (nothing to wait for).
	 */
	private void waitForInvisibleIfPresent(By locator) {
		try {
			// Quick check: is the loader actually in the DOM right now?
			java.util.List<WebElement> loaders = driver.findElements(locator);
			if (loaders.isEmpty()) {
				return; // Not present → nothing to wait for
			}
			// Loader IS present → now wait for it to truly disappear
			boolean anyVisible = loaders.stream().anyMatch(el -> {
				try {
					return el.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			});
			if (!anyVisible) {
				return; // Found in DOM but already hidden (display:none etc.)
			}
			logger.info("⏳ Loader detected → waiting for it to disappear: {}", locator);
			// System.out.println("⏳ Loader detected → waiting for it to disappear: " +
			// locator);
			// Use a long wait (up to ExplicitWait) for the loader to go away
			new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(ConnectToMainController.ExplicityWait)))
					.until(ExpectedConditions.invisibilityOfAllElements(loaders));
			logger.info("✅ Loader disappeared.");
			// System.out.println("✅ Loader disappeared.");
		} catch (Exception ignored) {
			// If anything fails here, continue — don't block the test
		}
	}

	/**
	 * Checks if continuous element failures have reached the configured limit.
	 * If so:
	 * - sets isElementFailedReachedMaxLimit=true → exits current step loop
	 * - sets currentScenarioAborted=true → skips remaining processes
	 * in the scenario (checked by ConnectToMainController between processes)
	 */
	public static void AbortCondition() {
		if (ConnectToMainController.Abort.equalsIgnoreCase("Y")) {
			failedPassedLocatorCount++;

			if (failedPassedLocatorCount >= Integer.parseInt(ConnectToMainController.RepeatedFailed)) {
				isElementFailedReachedMaxLimit = true;
				if (driver != null) {
					try {
						driver.quit();
					} catch (Exception e) {
						logger.warn("⚠️ Driver Quit failed: {}", e.getMessage());
					}
				}
				ConnectToMainController.currentScenarioAborted = true;
				ConnectToDataSheet.scenarioHasFailed = true;
				// ── Record the process/file that triggered the abort ──
				ConnectToMainController.lastAbortedProcess = ConnectToMainController.Process;
				logger.error(
						"❌ Continuously {} times failed to find element. Aborting scenario at Process='{}'. Fix ASAP.",
						ConnectToMainController.RepeatedFailed, ConnectToMainController.Process);
			}
		}
	}

}
