package com.mahindra.core;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.mahindra.actions.*;
import com.mahindra.config.*;
import com.mahindra.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.*;

public class ConnectToDataSheet extends Android_IOS_Driver {

	public static String Si_No;
	public static String Module;
	public static String PageName;
	public static String RunStatus;
	public static String PropertyName;
	public static String PropertyValue;
	public static String DataField;
	public static String Action;
	public static String ActionType;
	public static String ScenarioID;
	public static String ScenarioDescription;
	public static String TestCaseID;
	public static String TestCaseDescription;
	public static String TestCaseStepID;
	public static String TestCaseStepDescription;

	// TestData fields
	public static String dataSheetSi_No;
	public static String ApplicationName;
	public static String Proceed;
	public static String Verify;

	public static WebElement webElement;
	public static List<WebElement> webElements;
	public static String dataSheet2Value;
	public static String status;

	public static LocatorManager locatorManager;
	public static Function function;
	public static UtilsActivity utilsActivity;

	public static int totalTestStep, pass, fail;
	public static int totalValidations, passValidations, failedValidations;
	// ✅ Dirty flag: set true when any step/validation fails inside a SINGLE process
	// run.
	// ConnectToMainController reads this between processes to determine scenario
	// verdict.
	public static boolean scenarioHasFailed;
	public static int globallySheetTwoRowCount;
	public static String everyStepExecutionTime;
	public static List<List<String>> everyStepScreenShot;

	// ✅ OPTIMIZATION: Cached test data rows from TestData.xlsx (loaded once)
	private static List<Map<String, String>> cachedTestDataRows = null;

	// ✅ OPTIMIZATION: LocatorHub cache — entire LocatorsHub.xlsx loaded ONCE into memory.
	// Key = LocatorName (e.g. "proceedBtn"), Value = LocatorValue (e.g. "//*[text()='Proceed']")
	// null  → not yet loaded;  empty map → file missing / sheet empty (pass-through mode)
	private static Map<String, String> locatorHubCache = null;

	public final static Logger logger = LogManager.getLogger(ConnectToDataSheet.class.getName());

	/**
	 * Main entry point: reads all step rows from the DataSheet ONCE,
	 * caches TestData ONCE, then iterates Sheet2 data rows executing the cached
	 * steps.
	 */
	public static void extractAllData(int sheet2rowCount) throws Exception {

		try {
			// Skip row logic variables
			int stepMatchedSkipRow = 0;
			boolean stepSkipWithNewDataSheet = false;

			// Actions that skip locator resolution (they don't need element lookup)
			Set<String> exactMatchActions = new HashSet<>(Arrays.asList(
					"HIDEKEYBOARD",
					"HIDEKEYBOARDUSINGENTERKEY",
					"HIDEKEYBOARDIFITOPEN",
					"SCROLLUPDOWNELEMENTUNTILLVISIABLE",
					"UNTILSCROLLUPELEMENTVIEW",
					"UNTILSCROLLDOWNELEMENTVIEW"));

			// ✅ OPTIMIZATION: Create helper objects ONCE (not per data row iteration)
			locatorManager = new LocatorManager();
			utilsActivity = new UtilsActivity();

			// ══════════════════════════════════════════════════════════════
			// ✅ OPTIMIZATION: Read ALL step rows from Sheet1 ONCE before the loop
			// Converts it to a robust Map matching string columns to preserve integrity if
			// Excel shifts.
			// ══════════════════════════════════════════════════════════════
			List<Map<String, String>> cachedStepRows = readAndValidateStepRows();

			// ✅ OPTIMIZATION: Cache ALL test data rows from TestData.xlsx ONCE
			cacheAllTestDataRows();

			// ✅ OPTIMIZATION: Load entire LocatorsHub.xlsx into HashMap ONCE
			// All getLocatorValue() calls become O(1) in-memory lookups — zero file I/O per step
			loadLocatorHubCache();

			// ══════════════════════════════════════════════════════════════
			// Sheet2 Data-Row Loop (iterates test data, executes same steps)
			// ══════════════════════════════════════════════════════════════
			int SheetTwoRowCount;
			for (SheetTwoRowCount = 1; SheetTwoRowCount <= sheet2rowCount; SheetTwoRowCount++) {

				// for the testcaseNumber Store to not create Same testcase at report
				Set<String> testCasesNumbers = new HashSet<>();

				// Reset for each data row
				LocatorManager.failedPassedLocatorCount = 0;
				LocatorManager.isElementFailedReachedMaxLimit = false;
				stepSkipWithNewDataSheet = true;
				everyStepScreenShot = new ArrayList<>();
				globallySheetTwoRowCount = SheetTwoRowCount;

				totalTestStep = 0;
				pass = 0;
				fail = 0;
				totalValidations = 0;
				passValidations = 0;
				failedValidations = 0;
				// Reset dirty flag per data-row (ConnectToMainController resets it per
				// scenario)
				scenarioHasFailed = false;

				try {
					utilsActivity.extentReport();
				} catch (Exception e) {
					logger.error("Extent report initialization failed: {}", e.getMessage(), e);
					System.out.println("Execution Error: Extent report initialization failed.");
					// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
					throw new RuntimeException("Extent report initialization failed: " + e.getMessage(), e);
				}

				// Fresh execution timer for this data row
				Function.executionStartTime = System.nanoTime();
				Function.executionStartTimeProperFormat = UtilsActivity.fetchSystemCurrentTime();

				int i;
				for (i = 0; i < cachedStepRows.size(); i++) {

					// ── Read step data from cached row ──
					Map<String, String> row = cachedStepRows.get(i);
					Si_No = row.getOrDefault("Si_No", "");
					Module = row.getOrDefault("Module", "");
					PageName = row.getOrDefault("PageName", "");
					RunStatus = row.getOrDefault("RunStatus", "");
					PropertyName = row.getOrDefault("PropertyName", "");
					PropertyValue = getLocatorValue(row.getOrDefault("PropertyValue", ""));
					DataField = row.getOrDefault("DataField", "");
					Action = row.getOrDefault("Action", "");
					ActionType = row.getOrDefault("ActionType", "");
					ScenarioID = row.getOrDefault("ScenarioID", "");
					ScenarioDescription = row.getOrDefault("ScenarioDescription", "");
					TestCaseID = row.getOrDefault("TestCaseID", "");
					TestCaseDescription = row.getOrDefault("TestCaseDescription", "");
					TestCaseStepID = row.getOrDefault("TestCaseStepID", "");
					TestCaseStepDescription = row.getOrDefault("TestCaseStepDescription", "");

					// ── SkipRow logic for BrowserStack remote multi-data runs ──
					if (ConnectToMainController.SkipLine != null
							&& ConnectToMainController.SkipLine.equalsIgnoreCase("Y")) {
						if (stepSkipWithNewDataSheet) {
							if (ConnectToMainController.PlatForm != null
									&& ConnectToMainController.PlatForm.equalsIgnoreCase("Mobile") &&
									ConnectToMainController.ExecutionType != null
									&& ConnectToMainController.ExecutionType.equalsIgnoreCase("remote") &&
									sheet2rowCount > 1 && SheetTwoRowCount != 1) {
								i = stepMatchedSkipRow;
								stepSkipWithNewDataSheet = false;
							}
						}
					}

					// ── Abort logic: break out of step loop immediately if abort triggered ──
					if (LocatorManager.isElementFailedReachedMaxLimit
							|| ConnectToMainController.currentScenarioAborted) {
						LocatorManager.isElementFailedReachedMaxLimit = false;
						i = cachedStepRows.size() - 1;
						logger.warn("🛑 Abort triggered at SI_No={}...", Si_No);
						System.out.println("🛑 Abort triggered at SI_No=" + Si_No + "...");
						break; // ← line 183 — step loop exits immediately here
					}

					// Ensure Action isn't evaluated if null/empty to avoid exception
					if (Action == null || Action.trim().isEmpty()) {
						continue;
					}

					// SkipRow marker for remote execution
					if (Action.equalsIgnoreCase("RowSkipForRemote")) {
						stepMatchedSkipRow = i + 1;
					}

					try {
						// Create test case node in extent report if TestCaseID changes
						if (TestCaseID != null && !testCasesNumbers.contains(TestCaseID)) {
							testCasesNumbers.add(TestCaseID);
							utilsActivity.testCaseCreate();
							// NOTE: totalScenarios is set once in
							// ConnectToMainController.mainControllerSheet()
							// as scenarioGroups.size(). Do NOT increment it here.
						}
						totalTestStep++;

						if (Action.equalsIgnoreCase("CheckVisibility")) {
							totalValidations++;
						}

						// ✅ Reset TestData for every step — steps without a DataField always show empty
						// { }
						dataSheet2Value = "";

						// ✅ Print step info BEFORE execution — so you know what's about to run
						System.out.println();
						System.out.println("SI_No             ====================> " + Si_No);
						System.out.println("ScenarioID        ====================> " + ScenarioID);
//						System.out.println("PropertyName      ====================> " + PropertyName);
//						System.out.println("PropertyValue     ====================> " + PropertyValue);
						Optional.ofNullable(PropertyName)
								.filter(s -> !s.isBlank())
								.ifPresent(v -> System.out.println("PropertyName      ====================> " + v));
						Optional.ofNullable(PropertyValue)
								.filter(s -> !s.isBlank())
								.ifPresent(v -> System.out.println("PropertyValue     ====================> " + v));
						Optional.ofNullable(DataField)
								.filter(s -> !s.isBlank())
								.ifPresent(v -> System.out.println("DataField         ====================> " + v));
						System.out.println("Action            ====================> " + Action);

						long currentTime = System.nanoTime();

						// ✅ OPTIMIZATION: Skip locator lookup for actions that don't need it
						if (exactMatchActions.stream().anyMatch(
								action -> action.equalsIgnoreCase(Action) || Action.toUpperCase().startsWith(action))) {
							Function.ActionRDS();
						} else {
							locatorManager.mapToLocator();
						}

						everyStepExecutionTime = UtilsActivity.calculateEveryStepExecutionTime(currentTime);

						Optional.ofNullable(dataSheet2Value)
								.filter(s -> !s.isBlank())
								.ifPresent(v -> System.out
										.println("TestData          ====================> { " + v + " }"));
						System.out.println("StepExecution     ====================> ⏰" + everyStepExecutionTime);

						// ✅ Log full step details to log file
						logStepFormat(Si_No, everyStepExecutionTime, ScenarioID, PropertyName, PropertyValue, DataField,
								Action);

						pass = totalTestStep - fail;
						passValidations = totalValidations - failedValidations;

						// SkipRow: quit driver on last data row for remote mobile
						if (ConnectToMainController.SkipLine != null
								&& ConnectToMainController.SkipLine.equalsIgnoreCase("Y") &&
								i == cachedStepRows.size() - 1
								&& ConnectToMainController.PlatForm != null
								&& ConnectToMainController.PlatForm.equalsIgnoreCase("Mobile") &&
								ConnectToMainController.ExecutionType != null
								&& ConnectToMainController.ExecutionType.equalsIgnoreCase("remote")) {
							if (SheetTwoRowCount == sheet2rowCount && driver != null) {
								driver.quit();
							}
						}

					} catch (Exception e) {
						logger.error("Step execution error at SI_No={}: {}", Si_No, e.getMessage(), e);
					}
				}

				if (i == cachedStepRows.size()) {
					System.out.println("\n");
					System.out.println("********************  Successfully Completed  ********************\n");
				}

				// ── Process-level step verdict (printed per data-row within a process) ──
				if (scenarioHasFailed || ConnectToMainController.currentScenarioAborted) {
					System.out.println("🔴 Process '" + ConnectToMainController.Process
							+ "' TestData Row-" + SheetTwoRowCount + " had FAILURES (step/validation/abort)");
				} else {
					System.out.println("🟢 Process '" + ConnectToMainController.Process
							+ "' TestData Row-" + SheetTwoRowCount + " PASSED");
				}

				// Finalize reports for this data row
				try {
					utilsActivity.ExecutionTime();
					utilsActivity.ExtentFlush();
					if (ConnectToMainController.StepsScreenshot != null
							&& ConnectToMainController.StepsScreenshot.equalsIgnoreCase("Y")) {
						UtilsActivity.createEveryStepHtmlReport();
					}
					UtilsActivity.webUIReport();
					MailSend.mailSend();

				} catch (Exception e) {
					logger.error("Report finalization error: {}", e.getMessage(), e);
				}
			}

		} catch (Exception e) {
			logger.error("⚠️ Critical error in extractAllData execution: {}", e.getMessage(), e);
			System.out.println("⚠️ Critical execution error in extractAllData: " + e.getMessage());
			throw e;
		} finally {
			// Safely check driver state to prevent resource leaks
			try {
				if (driver != null && isDriverAlive(driver)) {
					logger.info("Driver session is still alive after extractAllData completion.");
				}
			} catch (Exception ignored) {
				// Silently ignore cleanup check errors
			}
		}
	}

	/**
	 * ✅ OPTIMIZATION: Reads and validates Sheet1 step rows from the DataSheet file
	 * ONCE.
	 * Converts the data into robust Key-Value maps rather than fragile Index arrays
	 * to prevent crashes
	 * if the Excel sheet modifies the column order.
	 */
	private static List<Map<String, String>> readAndValidateStepRows() {
		List<Map<String, String>> cachedStepRows = new ArrayList<>();
		Fillo fillo = new Fillo();
		Connection conn = null;
		Recordset recordset = null;

		try {
			conn = fillo.getConnection(ConnectToMainController.dataSheetFilePath);

			// ── Validate Sheet1 columns ──
			try {
				recordset = conn.executeQuery("SELECT * FROM Sheet1");
				if (recordset != null) {
					List<String> actualColumnName = recordset.getFieldNames();
					List<String> expectedColumnName = Arrays.asList(
							"Si_No", "Module", "PageName", "RunStatus", "PropertyName",
							"PropertyValue", "DataField", "Action", "ActionType",
							"ScenarioID", "ScenarioDescription", "TestCaseID",
							"TestCaseDescription", "TestCaseStepID", "TestCaseStepDescription");

					List<String> notPresentColumn = new ArrayList<>();
					for (String columnName : expectedColumnName) {
						if (!actualColumnName.contains(columnName)) {
							notPresentColumn.add(columnName);
						}
					}

					if (!notPresentColumn.isEmpty()) {
						logger.error("SORRY!!! '{}' columns missing or case mismatch in Sheet1", notPresentColumn);
						System.out.println(
								"SORRY!!! '" + notPresentColumn + "' columns missing or case mismatch in Sheet1");
						// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
						throw new RuntimeException("Missing columns in Sheet1: " + notPresentColumn);
					}
					recordset.close();
				}
			} catch (FilloException e) {
				logger.error("Error reading Sheet1 for column validation: {}", e.getMessage(), e);
				System.out.println("SORRY!!! Validation failed for Sheet1.");
				// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
				throw new RuntimeException("Validation failed for Sheet1: " + e.getMessage(), e);
			}

			// ── Read all matching step rows ──
			try {
				String queryForModule = "SELECT * FROM Sheet1 WHERE RUNSTATUS='Y' and MODULE='"
						+ ConnectToMainController.Process + "'";
				recordset = conn.executeQuery(queryForModule);

				if (recordset != null) {
					while (recordset.next()) {
						List<String> columns = recordset.getFieldNames();
						Map<String, String> rowMap = new LinkedHashMap<>();
						for (String column : columns) {
							rowMap.put(column, recordset.getField(column));
						}
						cachedStepRows.add(rowMap);
					}
					recordset.close();
				}
			} catch (FilloException e) {
				logger.error("Error querying 'Y' runstatus module step rows in Sheet1: {}", e.getMessage(), e);
				System.out.println(
						"SORRY!!! Query failed for Runstatus Sheet1 matching: " + ConnectToMainController.Process);
				// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
				throw new RuntimeException("Query failed for Sheet1 Module='" + ConnectToMainController.Process + "': " + e.getMessage(), e);
			}

		} catch (Exception e) {
			logger.error("Connection error extracting Step Rows in ConnectToDataSheet: {}", e.getMessage(), e);
			System.out.println("SORRY!!! Connection error extracting DataSheet Sheet1 ...");
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
			throw new RuntimeException("Connection error extracting DataSheet Sheet1: " + e.getMessage(), e);
		} finally {
			if (recordset != null) {
				recordset.close();
			}
			if (conn != null) {
				conn.close();
			}
		}

		if (cachedStepRows.isEmpty()) {
			logger.error("No matching steps found for Module='{}' in Sheet1", ConnectToMainController.Process);
			System.out.println(
					"SORRY!!! No matching steps found for Module='" + ConnectToMainController.Process + "' in Sheet1");
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
			throw new RuntimeException("No matching steps found for Module='" + ConnectToMainController.Process + "' in Sheet1");
		}

		return cachedStepRows;
	}

	/**
	 * ✅ OPTIMIZATION: Caches ALL matching test data rows from TestData.xlsx ONCE.
	 * Previously a new Fillo connection was opened for EVERY step that had a
	 * DataField.
	 */
	private static void cacheAllTestDataRows() {
		cachedTestDataRows = new ArrayList<>();
		Fillo fillo = new Fillo();
		Connection conn = null;
		Recordset recordset = null;

		try {
			conn = fillo.getConnection(ConnectToMainController.dataSheetFilePath);

			// ✅ Base query: ApplicationName matches Process
			StringBuilder queryBuilder = new StringBuilder(
					"SELECT * FROM Sheet2 WHERE RUNSTATUS='Y' and ApplicationName='"
							+ ConnectToMainController.Process + "'");

			// ✅ Optionally also filter by ScenarioNo if the column exists in Sheet2
			// This ensures the right test data set is loaded for each scenario/vertical.
			try {
				Recordset schemaCheck = conn.executeQuery("SELECT * FROM Sheet2");
				if (schemaCheck != null) {
					List<String> cols = schemaCheck.getFieldNames();
					schemaCheck.close();
					if (cols.contains("ScenarioNo")
							&& ConnectToMainController.ScenarioNo != null
							&& !ConnectToMainController.ScenarioNo.trim().isEmpty()) {
						queryBuilder.append(" and ScenarioNo='").append(ConnectToMainController.ScenarioNo.trim())
								.append("'");
					}
					if (cols.contains("VerticalName")
							&& ConnectToMainController.VerticalName != null
							&& !ConnectToMainController.VerticalName.trim().isEmpty()) {
						queryBuilder.append(" and VerticalName='").append(ConnectToMainController.VerticalName.trim())
								.append("'");
					}
				}
			} catch (Exception schemaEx) {
				logger.warn("Could not check Sheet2 columns for ScenarioNo/VerticalName filter: {}",
						schemaEx.getMessage());
			}

			String query = queryBuilder.toString();
			logger.info("Sheet2 TestData query: {}", query);

			try {
				recordset = conn.executeQuery(query);

				if (recordset != null) {
					while (recordset.next()) {
						Map<String, String> rowMap = new LinkedHashMap<>();
						for (String fieldName : recordset.getFieldNames()) {
							rowMap.put(fieldName, recordset.getField(fieldName));
						}
						cachedTestDataRows.add(rowMap);
					}
				}

				logger.info(
						"TestData cached: {} rows loaded for ApplicationName='{}', ScenarioNo='{}', VerticalName='{}'",
						cachedTestDataRows.size(), ConnectToMainController.ApplicationName,
						ConnectToMainController.ScenarioNo, ConnectToMainController.VerticalName);

			} catch (FilloException e) {
				System.out.println("⚠️ TestData Sheet2 caching FilloException: " + e.getMessage());
				logger.warn("TestData Sheet2 caching failed. Steps without DataField will still work. Error: {}",
						e.getMessage(), e);
			}

		} catch (Exception e) {
			System.out.println("⚠️ Connection error caching TestData: " + e.getMessage());
			logger.warn("TestData Connection failed completely. Steps without DataField will still work. Error: {}",
					e.getMessage(), e);
		} finally {
			if (recordset != null) {
				recordset.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * ✅ OPTIMIZATION: Uses cached test data instead of opening a new Fillo
	 * connection per step.
	 * Previously each call opened TestData.xlsx, queried, iterated to find the
	 * correct row.
	 * Now it's a simple in-memory map lookup.
	 */
	public static void extractTestData() throws Exception {

		dataSheet2Value = "";

		// ✅ Update Function element references without creating new objects
		Function.element = LocatorManager.webElement;
		Function.elements = LocatorManager.webElements;

		if (DataField != null && !DataField.trim().isEmpty()) {

			// ✅ FAST: In-memory lookup instead of Fillo query
			if (cachedTestDataRows != null && !cachedTestDataRows.isEmpty()) {
				// Prevent IndexOutOfBoundsException by fallback
				int targetIndex = globallySheetTwoRowCount - 1;
				if (targetIndex >= 0 && targetIndex < cachedTestDataRows.size()) {
					Map<String, String> testDataRow = cachedTestDataRows.get(targetIndex);

					dataSheetSi_No = testDataRow.getOrDefault("Si_No", "");
					ApplicationName = testDataRow.getOrDefault("ApplicationName", "");
					Proceed = testDataRow.getOrDefault("Proceed", "");
					Verify = testDataRow.getOrDefault("Verify", "");

					dataSheet2Value = testDataRow.getOrDefault(DataField, "");
				} else {
					logger.warn("TestData row parameter '{}' out of bounds for cache size: {}",
							targetIndex, cachedTestDataRows.size());
				}
			} else {
				System.out.println("⚠️ TestData row " + globallySheetTwoRowCount + " not found in cache completely.");
				logger.warn("TestData row {} not found in cache (cache size: 0)", globallySheetTwoRowCount);
			}

			UtilsActivity.testcaseInfoWithDataField();
			Function.ActionRDS();

		} else {
			UtilsActivity.testcaseInfoWithoutDataField();
			Function.ActionRDS();
		}
	}

	// Log each step in a structured format for the log file
	public static void logStepFormat(String si_no, String stepExecution, String scenarioID, String propertyName,
			String propertyValue, String dataField, String action) {

		String logMessage = String.format(
				"SI_No             ====================> %s%n"
						+ "ScenarioID        ====================> %s%n"
						+ "PropertyName      ====================> %s%n"
						+ "PropertyValue     ====================> %s%n"
						+ "DataField         ====================> %s%n"
						+ "Action            ====================> %s%n"
						+ "TestData          ====================> %s%n"
						+ "StepExecution     ====================> ⏰%s%n",
				si_no != null ? si_no : "",
				scenarioID != null ? scenarioID : "",
				(propertyName == null || propertyName.isEmpty() ? "" : propertyName),
				(propertyValue == null || propertyValue.isEmpty() ? "" : propertyValue),
				(dataField == null || dataField.isEmpty() ? "" : dataField),
				action != null ? action : "",
				(dataSheet2Value == null || dataSheet2Value.isEmpty() ? "" : "{ " + dataSheet2Value + " }"),
				stepExecution != null ? stepExecution : "");

		// System.out.println(logMessage);
		logger.info("\n{}\n", logMessage);
	}

	// ══════════════════════════════════════════════════════════════════════════
	// ✅ LOCATOR HUB — Dynamic Locator Resolution (Cache-First, Zero I/O per step)
	// ══════════════════════════════════════════════════════════════════════════
	/**
	 * Loads the <b>entire</b> LocatorsHub.xlsx into {@code locatorHubCache} exactly <b>once</b>
	 * per process execution (called from {@link #extractAllData}).
	 *
	 * <p>Strategy — identical to {@link #cacheAllTestDataRows}:
	 * <ul>
	 *   <li>Open the file once → read all rows → store in {@code HashMap<LocatorName, LocatorValue>} → close file.</li>
	 *   <li>All subsequent {@link #getLocatorValue} calls are pure in-memory map lookups — <b>zero file I/O</b>.</li>
	 *   <li>If the file is missing, unreadable, or the sheet is empty, the cache is set to an empty map
	 *       so {@code getLocatorValue()} silently falls back to pass-through mode.</li>
	 * </ul>
	 *
	 * <p>LocatorsHub.xlsx path resolution (highest priority wins):
	 * <ol>
	 *   <li>JVM property {@code -DuserInputLocatorHub} — absolute or relative path to the file</li>
	 *   <li>Auto-derived: {@code <dataSheetFolderPath>/LocatorsHub.xlsx} (same base folder as DataSheets)</li>
	 *   <li>Fallback: {@code <user.dir>/DataSheet/LocatorsHub.xlsx}</li>
	 * </ol>
	 *
	 * <p>Expected sheet name: {@code LocatorHub} &nbsp;|&nbsp; Expected columns: {@code LocatorName}, {@code LocatorValue}
	 */
	private static void loadLocatorHubCache() {

		locatorHubCache = new java.util.HashMap<>();   // always initialise — even on failure → pass-through mode

		// ── Resolve path ──
		String locatorHubPath = resolveLocatorHubPath();

		// ── Guard: file doesn't exist ──
		File hubFile = new File(locatorHubPath);
		if (!hubFile.exists() || !hubFile.isFile()) {
			logger.info("[LocatorHub] LocatorsHub.xlsx not found at '{}'. All PropertyValues will be used as-is.",
					locatorHubPath);
			System.out.println("[LocatorHub] LocatorsHub.xlsx not found → pass-through mode active.");
			return;
		}

		// ── Load once ──
		Fillo fillo = new Fillo();
		Connection connection = null;
		Recordset rs = null;

		try {
			connection = fillo.getConnection(locatorHubPath);
			rs = connection.executeQuery("SELECT LocatorName, LocatorValue FROM LocatorHub");

			int count = 0;
			while (rs.next()) {
				String name  = rs.getField("LocatorName");
				String value = rs.getField("LocatorValue");
				if (name != null && !name.trim().isEmpty()) {
					locatorHubCache.put(name.trim(), value != null ? value.trim() : "");
					count++;
				}
			}

			logger.info("[LocatorHub] Cache loaded: {} locator(s) from '{}'", count, locatorHubPath);
			System.out.println("[LocatorHub] ✅ Cache loaded: " + count + " locator(s) from LocatorsHub.xlsx");

		} catch (FilloException e) {
			logger.warn("[LocatorHub] Could not load LocatorsHub.xlsx (Fillo error). Pass-through mode active. Reason: {}",
					e.getMessage());
			System.out.println("[LocatorHub] ⚠️ Could not load LocatorsHub.xlsx → pass-through mode. Reason: " + e.getMessage());
		} catch (Exception e) {
			logger.warn("[LocatorHub] Unexpected error loading LocatorsHub.xlsx. Pass-through mode active. Reason: {}",
					e.getMessage());
			System.out.println("[LocatorHub] ⚠️ Unexpected error loading LocatorsHub.xlsx → pass-through mode.");
		} finally {
			if (rs != null)         rs.close();
			if (connection != null) connection.close();
		}
	}

	/**
	 * Resolves a PropertyValue through the in-memory LocatorHub cache.
	 *
	 * <p><b>This method does ZERO file I/O.</b> The cache is loaded once by
	 * {@link #loadLocatorHubCache()} before the step loop starts.
	 *
	 * <p>Lookup rules:
	 * <ul>
	 *   <li>If {@code locatorName} is a key in the cache → return the mapped {@code LocatorValue}.</li>
	 *   <li>If not found (or cache is empty / null) → return {@code locatorName} unchanged.
	 *       Hard-coded XPath / ID values in the DataSheet continue to work with no changes.</li>
	 * </ul>
	 *
	 * <p>Example:
	 * <pre>
	 * Sheet1 PropertyValue = "proceedBtn"
	 *   → cache hit  → returns "//*[text()='Proceed']"   ✅
	 *
	 * Sheet1 PropertyValue = "//button[@id='login']"
	 *   → cache miss → returns "//button[@id='login']"   ✅ (unchanged)
	 * </pre>
	 *
	 * @param  locatorName the raw value read from Sheet1 PropertyValue column
	 * @return the resolved locator string, or the original input if not found in the hub
	 */
	public static String getLocatorValue(String locatorName) {

		// ── Guard: null / blank → return as-is ──
		if (locatorName == null || locatorName.trim().isEmpty()) {
			return locatorName;
		}

		// ── Guard: cache not loaded (safety net — should never happen in normal flow) ──
		if (locatorHubCache == null || locatorHubCache.isEmpty()) {
			return locatorName;   // pass-through: file was missing or empty
		}

		// ── O(1) HashMap lookup — zero file I/O ──
		String resolvedValue = locatorHubCache.get(locatorName.trim());

		if (resolvedValue != null && !resolvedValue.isEmpty()) {
			logger.info("[LocatorHub] '{}' → '{}'", locatorName, resolvedValue);
//			System.out.println("[LocatorHub]      ====================> '" + locatorName
//					+ "' resolved to '" + resolvedValue + "'");
			return resolvedValue;
		}

		// ── Not in hub → return original value unchanged (pass-through) ──
		logger.debug("[LocatorHub] '{}' not found in cache. Using value as-is.", locatorName);
		return locatorName;
	}

	/**
	 * Resolves the absolute path to LocatorsHub.xlsx (priority order):
	 * <ol>
	 *   <li>{@code -DuserInputLocatorHub} JVM property (absolute or relative to {@code user.dir})</li>
	 *   <li>{@code ConnectToMainController.dataSheetFolderPath}/LocatorsHub.xlsx</li>
	 *   <li>{@code <user.dir>/DataSheet/LocatorsHub.xlsx} (safe fallback)</li>
	 * </ol>
	 */
	private static String resolveLocatorHubPath() {

		// Priority 1 — explicit CLI override
		String userInputLocatorHub = System.getProperty("userInputLocatorHub");
		if (userInputLocatorHub != null && !userInputLocatorHub.trim().isEmpty()) {
			File f = new File(userInputLocatorHub.trim());
			return f.isAbsolute() ? f.getAbsolutePath()
					: System.getProperty("user.dir") + File.separator + userInputLocatorHub.trim();
		}

		// Priority 2 — auto-derive from dataSheetFolderPath
		if (ConnectToMainController.dataSheetFolderPath != null
				&& !ConnectToMainController.dataSheetFolderPath.trim().isEmpty()) {
			return ConnectToMainController.dataSheetFolderPath
					+ File.separator + "LocatorsHub.xlsx";
		}

		// Priority 3 — safe fallback
		return System.getProperty("user.dir")
				+ File.separator + "DataSheet"
				+ File.separator + "LocatorsHub.xlsx";
	}

}
