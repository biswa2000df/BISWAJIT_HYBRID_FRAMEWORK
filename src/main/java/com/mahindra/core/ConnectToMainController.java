package com.mahindra.core;


import com.mahindra.config.*;
import com.mahindra.actions.*;
import com.mahindra.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

public class ConnectToMainController {

	// ── MAIN_CONTROLLER fields ──
	public static String Si_No;
	public static String ExecutionType;
	public static String PlatForm;
	public static String ApplicationName;
	public static String Process;
	public static String Abort;
	public static String StepsScreenshot;
	public static String SkipLine;
	public static String Retry;

	// ── Scenario-level fields from MAIN_CONTROLLER ──
	public static String ScenarioNo; // e.g. SC_01
	public static String VerticalName; // e.g. XPL / SALPL
	public static String Scenario; // e.g. "Till UW" — description of what this scenario performs

	// ── DATASHEET fields ──
	public static String dataSheetProcess = null;
	public static String TestDataSheet = null;
	public static String ImplicityWait = null;
	public static String ExplicityWait = null;
	public static String RepeatedFailed = null;
	public static String RetryCount = null;

	// ── File Paths ──
	public static String mainControllerFilePath;
	public static String dataSheetFilePath;
	public static String dataSheetFolderPath;

	// ── Scenario-level counters (accumulate across all scenarios) ──
	public static int totalScenarios = 0;
	public static int passScenarios = 0;
	public static int failScenarios = 0;

	// ── Flag: set true by LocatorManager.AbortCondition() when abort fires ──
	// ConnectToMainController reads this after each Process completes.
	// If true → scenario is marked FAILED → remaining processes in this scenario
	// skipped.
	public static boolean currentScenarioAborted = false;

	// ── Tracks the process/file that caused the abort for the summary email ──
	public static String lastAbortedProcess = "-";

	// ════════════════════════════════════════════════════════════════
	// Per-scenario result record — accumulated for the summary email
	// ════════════════════════════════════════════════════════════════
	public static class ScenarioResult {
		public final String scenarioNo; // e.g. SC_01 (mandatory)
		public final String verticalName; // e.g. XPL / SALPL (mandatory)
		public final String verticalScenario; // e.g. "Till UW" — what this scenario performs
		public final String verdict; // PASS / FAIL
		public final String failedProcess; // process that caused abort/fail (or "")
		public final String executionTime; // human-readable e.g. "02m 14s"
		public final long durationMs;

		/**
		 * @param scenarioNo       Mandatory scenario ID (e.g. SC_01)
		 * @param verticalName     Mandatory vertical name (e.g. XPL)
		 * @param verticalScenario Description of what the scenario performs (e.g. "Till
		 *                         UW")
		 * @param verdict          PASS or FAIL
		 * @param failedProcess    Process that triggered the abort (empty string if
		 *                         none)
		 * @param startEpoch       Epoch ms at scenario start
		 * @param endEpoch         Epoch ms at scenario end
		 */
		public ScenarioResult(String scenarioNo, String verticalName, String verticalScenario,
		                      String verdict, String failedProcess, long startEpoch, long endEpoch) {
			this.scenarioNo = (scenarioNo != null) ? scenarioNo.trim() : "";
			this.verticalName = (verticalName != null) ? verticalName.trim() : "";
			this.verticalScenario = (verticalScenario != null) ? verticalScenario.trim() : "";
			this.verdict = verdict;
			this.failedProcess = (failedProcess != null) ? failedProcess.trim() : "";
			long elapsed = endEpoch - startEpoch;
			// Truncate to exact seconds so that the total sum exactly matches the displayed
			// individual values
			this.durationMs = (elapsed / 1000) * 1000;
			long minutes = this.durationMs / 60000;
			long seconds = (this.durationMs % 60000) / 1000;
			this.executionTime = String.format("%02dm %02ds", minutes, seconds);
		}
	}

	// All scenario results — populated during execution, consumed by MailSend
	public static final java.util.List<ScenarioResult> scenarioResults = new java.util.ArrayList<>();

	public final static Logger logger = LogManager.getLogger(ConnectToMainController.class.getName());

	// ════════════════════════════════════════════════════════════════
	// ENTRY POINT
	// Reads MAIN_CONTROLLER, groups rows by ScenarioNo, then
	// executes each scenario's processes in sequence.
	// If AbortCondition fires inside any process → scenario marked FAILED
	// → remaining processes for that scenario are skipped.
	// ════════════════════════════════════════════════════════════════
	public static void mainControllerSheet() {

		String userInputMainController = System.getProperty("userInputMainController");

		if (userInputMainController == null || userInputMainController.trim().isEmpty()) {
			mainControllerFilePath = System.getProperty("user.dir") + File.separator + "MainController.xlsx";
		} else {
			File input = new File(userInputMainController.trim());
			if (input.isAbsolute()) {
				mainControllerFilePath = input.getAbsolutePath();
			} else {
				mainControllerFilePath = System.getProperty("user.dir") + File.separator
						+ userInputMainController.trim();
			}
		}

		File mainControllerFile = new File(mainControllerFilePath);
		if (!mainControllerFile.exists()) {
			String fileName = (userInputMainController == null || userInputMainController.trim().isEmpty())
					? "MainController.xlsx"
					: userInputMainController.trim();
			logger.error("❌ ERROR: Main Controller file '{}' not found at path: {}", fileName, mainControllerFilePath);
			System.out.println("==========================================================================");
			System.out.println("❌ ERROR: Main Controller file not found!");
			System.out.println("   👉 File Name  : " + fileName);
			System.out.println("   👉 Looked At  : " + mainControllerFilePath);
			System.out
					.println("   👉 Resolution : Pass a different file using: -DuserInputMainController=filename.xlsx");
			System.out.println("==========================================================================");
			System.exit(0);
		}

		// ── Load ALL RunStatus=Y rows from MAIN_CONTROLLER grouped by
		// ScenarioNo+VerticalName ──
		// LinkedHashMap preserves insertion order so SC_01|SALPL executes before
		// SC_01|XPL etc.
		// Key = composite "ScenarioNo|VerticalName" (e.g. "SC_01|SALPL")
		// This ensures SC_01 for SALPL and SC_01 for XPL are TWO separate scenarios.
		// Value = ordered list of process rows for that unique scenario
		LinkedHashMap<String, List<Map<String, String>>> scenarioGroups = loadAndGroupMainControllerRows();

		if (scenarioGroups.isEmpty()) {
			System.out.println("⚠️ No rows with RunStatus='Y' found in MAIN_CONTROLLER. Nothing to execute.");
			return;
		}

		totalScenarios = scenarioGroups.size();
		System.out.println("\n📋 Total Scenarios to Execute: " + totalScenarios);

		// ── Execute each scenario ──
		for (Map.Entry<String, List<Map<String, String>>> scenarioEntry : scenarioGroups.entrySet()) {

			// compositeKey = "SC_01|SALPL" → split to recover individual parts
			String compositeKey = scenarioEntry.getKey();
			List<Map<String, String>> processRows = scenarioEntry.getValue();
			String[] keyParts = compositeKey.split("\\|", -1);
			String currentScenarioNo = keyParts[0]; // e.g. SC_01
			String currentVerticalFromKey = (keyParts.length > 1) ? keyParts[1] : ""; // e.g. SALPL

			// ── Reset abort flag before each scenario ──
			currentScenarioAborted = false;
			lastAbortedProcess = "-";
			ConnectToDataSheet.scenarioHasFailed = false;
			long scenarioStartTime = System.currentTimeMillis();

			// Prefer the VerticalName read from the composite key (guaranteed unique);
			// fall back to what is stored in the first row (should always match).
			String currentVerticalName = !currentVerticalFromKey.isEmpty() ? currentVerticalFromKey
					: (processRows.isEmpty() ? "" : processRows.get(0).getOrDefault("VerticalName", ""));
			String currentScenarioDesc = processRows.isEmpty() ? ""
					: processRows.get(0).getOrDefault("Scenario", "");
			String currentAppName = processRows.isEmpty() ? "" : processRows.get(0).getOrDefault("ApplicationName", "");

			// Human-readable label: "SC_01 [SALPL]" for banners and summaries
			String scenarioLabel = currentScenarioNo
					+ (currentVerticalName.isEmpty() ? "" : " [" + currentVerticalName + "]");

			System.out.println("\n╔══════════════════════════════════════════════════════╗");
			System.out.println("║  🚀 SCENARIO START: " + scenarioLabel + " (" + processRows.size() + " processes)");
			System.out.println("╚══════════════════════════════════════════════════════╝");

			// ── Create ONE log file for this entire scenario (not per-process) ──
			// Set ApplicationName BEFORE calling configureLog4jForScenario so the
			// log folder path uses the correct app name instead of 'null'
			if (!currentAppName.isEmpty()) {
				ApplicationName = currentAppName;
			}
			try {
				// Use compositeKey (e.g. "SC_01_SALPL") as the log file name so that
				// SC_01 for SALPL and SC_01 for XPL get separate log files.
				String logScenarioId = currentScenarioNo
						+ (currentVerticalName.isEmpty() ? "" : "_" + currentVerticalName);
				UtilsActivity.configureLog4jForScenario(logScenarioId);
			} catch (Exception logErr) {
				logger.warn("Log4j configuration failed for Scenario={}. Continuing. Error: {}",
						scenarioLabel, logErr.getMessage());
			}

			// ── Re-log CLI filter state into the now-open scenario log file ──────────────
			// The filter check runs in loadAndGroupMainControllerRows() BEFORE this log
			// file exists, so logger.warn() there has no file appender yet.
			// We re-read system properties here (they never change during a JVM run)
			// and write the same message again so it is captured in the scenario log.
			// ─────────────────────────────────────────────────────────────────────────────
			{
				String _rawCliScenario = System.getProperty("runScenario", "").trim();
				String _rawCliVertical = System.getProperty("runVertical", "").trim();
				boolean _bothProvided  = !_rawCliScenario.isEmpty() && !_rawCliVertical.isEmpty();
				boolean _partialOnly   = (!_rawCliScenario.isEmpty() || !_rawCliVertical.isEmpty()) && !_bothProvided;

				if (_bothProvided) {
					logger.info("\n🎯 CLI SCENARIO FILTER IS ACTIVE\n"
							+ "   👉 -DrunScenario = '{}'\n"
							+ "   👉 -DrunVertical = '{}'\n"
							+ "   👉 RunStatus='Y' is still validated in query",
							_rawCliScenario, _rawCliVertical);
				} else if (_partialOnly) {
					logger.warn("\n⚠️  CLI FILTER WARNING: Both -DrunScenario AND -DrunVertical must be provided together.\n"
							+ "   👉 -DrunScenario = '{}'  (provided: {})\n"
							+ "   👉 -DrunVertical = '{}'  (provided: {})\n"
							+ "   👉 Falling back to NORMAL mode: all RunStatus='Y' scenarios will run.",
							_rawCliScenario, !_rawCliScenario.isEmpty(),
							_rawCliVertical, !_rawCliVertical.isEmpty());
				}
				// If neither was provided: nothing to log — normal execution, no noise.
			}
			// ─────────────────────────────────────────────────────────────────────────────

			// ── Execute each process (sheet) within this scenario ──
			for (Map<String, String> row : processRows) {

				// If a previous process triggered AbortCondition → skip remaining processes
				if (currentScenarioAborted) {
					System.out.println("⏭️  Skipping Process '" + row.getOrDefault("Process", "?")
							+ "' because AbortCondition was triggered in this scenario.");
					logger.warn("Skipping Process='{}' for Scenario='{}' due to AbortCondition.",
							row.getOrDefault("Process", "?"), currentScenarioNo);
					continue;
				}

				// ── Populate static fields for this process row ──
				Si_No = row.getOrDefault("Si_No", "");
				PlatForm = row.getOrDefault("PlatForm", "");
				ExecutionType = row.getOrDefault("ExecutionType", "");
				ScenarioNo = row.getOrDefault("ScenarioNo", "");
				VerticalName = row.getOrDefault("VerticalName", "");
				Scenario = row.getOrDefault("Scenario", "");
				ApplicationName = row.getOrDefault("ApplicationName", "");
				Process = row.getOrDefault("Process", "");
				Abort = row.getOrDefault("Abort", "");
				StepsScreenshot = row.getOrDefault("StepsScreenshot", "");
				SkipLine = row.getOrDefault("SkipLine", "");
				Retry = row.getOrDefault("Retry", "");

				// ScenarioNo, VerticalName, and Scenario are mandatory — fail fast if missing
				if (Si_No.isEmpty() || PlatForm.isEmpty() || ExecutionType.isEmpty()
						|| ScenarioNo.isEmpty() || VerticalName.isEmpty()
						|| ApplicationName.isEmpty() || Process.isEmpty() || Abort.isEmpty()
						|| StepsScreenshot.isEmpty() || SkipLine.isEmpty()) {
					logger.error("Empty required column(s) in MAIN_CONTROLLER row Si_No={}. Skipping.", Si_No);
					System.out.println("SORRY!!! Empty column(s) in MAIN_CONTROLLER row Si_No=" + Si_No
							+ ". Required: Si_No, PlatForm, ExecutionType, ScenarioNo, VerticalName,"
							+ " ApplicationName, Process, Abort, StepsScreenshot, SkipLine.");
					continue;
				}

				logger.info(
						"Processing ScenarioNo={}, VerticalName={}, Si_No={}, Process={}, Platform={}, ExecutionType={}",
						ScenarioNo, VerticalName, Si_No, Process, PlatForm, ExecutionType);

				// ── Execute the DataSheet for this process ──
				try {
					MainControlerDataSheet(Process, ScenarioNo, VerticalName);
				} catch (Exception dsErr) {
					logger.error("Error executing DataSheet for ScenarioNo={}, Process={}. Error: {}",
							ScenarioNo, Process, dsErr.getMessage(), dsErr);
					System.out
							.println("⚠️ Error executing DataSheet for Process=" + Process + ": " + dsErr.getMessage());
					// ✅ FIX: Mark scenario as aborted so remaining processes are skipped
					// and the scenario is recorded as FAILED (instead of killing the JVM).
					currentScenarioAborted = true;
					lastAbortedProcess = (Process != null ? Process : "Unknown");
					ConnectToDataSheet.scenarioHasFailed = true;
				}
			}

			// ── Scenario verdict after all its processes are done ──
			boolean scenarioFailed = currentScenarioAborted || ConnectToDataSheet.scenarioHasFailed;
			long scenarioEndTime = System.currentTimeMillis();
			String verdict = scenarioFailed ? "FAIL" : "PASS";

			// Record per-scenario result
			scenarioResults.add(new ScenarioResult(
					currentScenarioNo, currentVerticalName, currentScenarioDesc,
					verdict, lastAbortedProcess, scenarioStartTime, scenarioEndTime));

			if (scenarioFailed) {
				failScenarios++;
				System.out.println("\n🔴 SCENARIO " + scenarioLabel + " FAILED"
						+ (currentScenarioAborted ? " (AbortCondition triggered at: " + lastAbortedProcess + ")"
						: " (step/validation failed)"));
			} else {
				passScenarios++;
				System.out.println(
						"\n🟢 SCENARIO " + scenarioLabel + " PASSED (all processes completed successfully)");
			}

			// ── Persist this scenario's result to ScenarioExecutionReport.xlsx immediately ──
			// Written right after each scenario completes so audit data is saved
			// even if a later scenario or the email step fails.
			try {
				UtilsActivity.saveScenarioAuditToReport();
			} catch (Exception reportErr) {
				logger.warn("Failed to write ScenarioExecutionReport.xlsx after Scenario={}: {}",
						scenarioLabel, reportErr.getMessage());
			}
		}

		// ── Final summary across all scenarios ──
		System.out.println("\n╔══════════════════════════════════════════════════════╗");
		System.out.println("║         FINAL SCENARIO EXECUTION SUMMARY             ║");
		System.out.println("╠══════════════════════════════════════════════════════╣");
		System.out.println("║  Total Scenarios   : " + String.format("%-32s", totalScenarios) + "║");
		System.out.println("║  ✅ Passed         : " + String.format("%-32s", passScenarios) + "║");
		System.out.println("║  ❌ Failed         : " + String.format("%-32s", failScenarios) + "║");
		System.out.println("╚══════════════════════════════════════════════════════╝\n");



		// ── Send consolidated scenario summary email (if SendScenarioSummary=Y) ──
		try {
			MailSend.sendScenarioSummaryEmail();
		} catch (Exception mailErr) {
			logger.warn("Scenario summary email failed: {}", mailErr.getMessage());
		}
	}

	// ════════════════════════════════════════════════════════════════
	// Load MAIN_CONTROLLER rows, validate columns, and group by the
	// COMPOSITE KEY "ScenarioNo|VerticalName".
	//
	// WHY composite key?
	// SC_01 + SALPL → one scenario
	// SC_01 + XPL → a DIFFERENT scenario (same number, diff vertical)
	// SC_02 + SALPL → another scenario
	// SC_02 + XPL → yet another scenario
	// Total = 4, not 2.
	//
	// Backward compatible: if ScenarioNo is absent each row is its own
	// auto-generated scenario ("AUTO_SC_1", etc.).
	// ════════════════════════════════════════════════════════════════
	private static LinkedHashMap<String, List<Map<String, String>>> loadAndGroupMainControllerRows() {

		// ── CLI filter: -DrunScenario=SC_01  -DrunVertical=XPL ──────────────────────
		// When BOTH are provided and non-empty → the main query adds AND ScenarioNo
		// AND VerticalName so only that specific scenario's rows are returned.
		// When only one (or neither) is provided → query stays RunStatus='Y' only
		// (existing behaviour, nothing changes).
		String cliScenario = System.getProperty("runScenario", "").trim();
		String cliVertical = System.getProperty("runVertical", "").trim();
		boolean cliFilterActive = !cliScenario.isEmpty() && !cliVertical.isEmpty();

		if (cliFilterActive) {
			System.out.println("\n╔══════════════════════════════════════════════════════╗");
			System.out.println("║        🎯 CLI SCENARIO FILTER IS ACTIVE              ║");
			System.out.println("╠══════════════════════════════════════════════════════╣");
			System.out.println("║  ScenarioNo  : " + String.format("%-38s", cliScenario) + "║");
			System.out.println("║  Vertical    : " + String.format("%-38s", cliVertical) + "║");
			System.out.println("║  RunStatus='Y' is still validated in query           ║");
			System.out.println("╚══════════════════════════════════════════════════════╝\n");
			logger.info("CLI filter active → runScenario='{}', runVertical='{}'", cliScenario, cliVertical);
		} else if (!cliScenario.isEmpty() || !cliVertical.isEmpty()) {
			// Only one of the two was provided — warn and fall back to full execution
			System.out.println("\n⚠️  CLI FILTER WARNING: Both -DrunScenario AND -DrunVertical must be provided together.");
			System.out.println("   👉 -DrunScenario = '" + cliScenario + "'  (provided: " + !cliScenario.isEmpty() + ")");
			System.out.println("   👉 -DrunVertical = '" + cliVertical + "'  (provided: " + !cliVertical.isEmpty() + ")");
			System.out.println("   👉 Falling back to NORMAL mode: all RunStatus='Y' scenarios will run.\n");
			logger.warn("\n⚠️  CLI FILTER WARNING: Both -DrunScenario AND -DrunVertical must be provided together.\n"
					+ "   👉 -DrunScenario = '{}'  (provided: {})\n"
					+ "   👉 -DrunVertical = '{}'  (provided: {})\n"
					+ "   👉 Falling back to NORMAL mode: all RunStatus='Y' scenarios will run.",
					cliScenario, !cliScenario.isEmpty(),
					cliVertical, !cliVertical.isEmpty());
			cliScenario = "";
			cliVertical = "";
			cliFilterActive = false;
		}
		// ─────────────────────────────────────────────────────────────────────────────

		LinkedHashMap<String, List<Map<String, String>>> groups = new LinkedHashMap<>();
		Connection connection = null;
		Recordset recordset = null;

		try {
			Fillo fillo = new Fillo();
			connection = fillo.getConnection(mainControllerFilePath);
			logger.info("Successfully connected to MainController.xlsx");

			// ── 1. Validate mandatory columns ──
			List<String> expectedColumns = Arrays.asList(
					"Si_No", "RunStatus", "PlatForm", "ExecutionType",
					"ScenarioNo", "VerticalName", "Scenario",
					"ApplicationName", "Process", "Abort", "StepsScreenshot", "SkipLine", "Retry");

			try {
				recordset = connection.executeQuery("SELECT * FROM MAIN_CONTROLLER");
				if (recordset != null) {
					List<String> actualColumns = recordset.getFieldNames();
					List<String> missing = new ArrayList<>();
					for (String col : expectedColumns) {
						if (!actualColumns.contains(col))
							missing.add(col);
					}
					if (!missing.isEmpty()) {
						logger.error("SORRY!!! Missing columns in MAIN_CONTROLLER: {}", missing);
						System.out.println("SORRY!!! Missing columns in MAIN_CONTROLLER sheet: " + missing);
						System.exit(0);
					}

					// ── ScenarioNo, VerticalName, Scenario are now mandatory columns ──
					boolean hasScenarioNo = actualColumns.contains("ScenarioNo");
					boolean hasVerticalName = actualColumns.contains("VerticalName");
					boolean hasScenario = actualColumns.contains("Scenario");

					if (!hasScenarioNo) {
						System.out.println("⚠️  WARNING: 'ScenarioNo' column not found in MAIN_CONTROLLER. "
								+ "Each process row will be treated as an independent scenario.");
					}
					if (!hasScenario) {
						System.out.println("⚠️  WARNING: 'Scenario' column not found in MAIN_CONTROLLER. "
								+ "Scenario description will be blank in the summary email.");
					}

					recordset.close();

					// ── 2. Build the main data query ─────────────────────────────────────────────
					// Base:   RunStatus='Y'  (always applied — existing behaviour)
					// Extra:  AND ScenarioNo / AND VerticalName added ONLY when the CLI filter
					//         is active AND the column actually exists in the sheet.
					// ─────────────────────────────────────────────────────────────────────────────
					StringBuilder mainQuery = new StringBuilder(
							"SELECT * FROM MAIN_CONTROLLER Where RunStatus='Y'");

					if (cliFilterActive && hasScenarioNo) {
						mainQuery.append(" AND ScenarioNo='").append(cliScenario).append("'");
					}
					if (cliFilterActive && hasVerticalName) {
						mainQuery.append(" AND VerticalName='").append(cliVertical).append("'");
					}

					logger.info("MAIN_CONTROLLER query: {}", mainQuery);
					recordset = connection.executeQuery(mainQuery.toString());

					if (recordset == null) {
						System.out.println("⚠️ No rows with RunStatus='Y' in MAIN_CONTROLLER.");
						return groups;
					}

					int autoScenarioCounter = 0;
					while (recordset.next()) {
						Map<String, String> rowMap = new LinkedHashMap<>();
						for (String col : recordset.getFieldNames()) {
							rowMap.put(col, recordset.getField(col));
						}

						// ── Determine composite scenario key: "ScenarioNo|VerticalName" ──
						// This ensures SC_01+SALPL and SC_01+XPL are counted as TWO separate
						// scenarios instead of being collapsed into one group.
						String scenarioNo;
						String verticalNo;

						if (hasScenarioNo) {
							scenarioNo = rowMap.getOrDefault("ScenarioNo", "").trim();
							if (scenarioNo.isEmpty()) {
								autoScenarioCounter++;
								scenarioNo = "AUTO_SC_" + autoScenarioCounter;
								rowMap.put("ScenarioNo", scenarioNo);
							}
						} else {
							// Backward compatible: each row = its own scenario
							autoScenarioCounter++;
							scenarioNo = "SC_" + String.format("%02d", autoScenarioCounter);
							rowMap.put("ScenarioNo", scenarioNo);
						}

						if (!hasVerticalName) {
							rowMap.put("VerticalName", "");
						}
						if (!hasScenario) {
							rowMap.put("Scenario", "");
						}

						// Vertical part of the composite key (may be blank for old sheets)
						verticalNo = rowMap.getOrDefault("VerticalName", "").trim();

						// Composite key: "SC_01|SALPL", "SC_01|XPL", "SC_02|SALPL", "SC_02|XPL"
						// Each unique combination becomes its own scenario group.
						String compositeKey = scenarioNo + "|" + verticalNo;

						groups.computeIfAbsent(compositeKey, k -> new ArrayList<>()).add(rowMap);
					}
					recordset.close();
				}
			} catch (FilloException e) {
				logger.error("SORRY!!! 'MAIN_CONTROLLER' sheet not found or inaccessible. Error: {}", e.getMessage(),
						e);
				System.out.println("SORRY!!! 'MAIN_CONTROLLER' sheet not found. Check the sheet tab name.");
				System.exit(0);
			}

		} catch (FilloException e) {
			logger.error("Failed to open MainController.xlsx. Error: {}", e.getMessage(), e);
			System.out.println("SORRY!!! Cannot connect to 'MainController.xlsx'. Ensure it is not open in Excel.");
			System.exit(0);
		} catch (Exception e) {
			logger.error("Unexpected error in loadAndGroupMainControllerRows(). Error: {}", e.getMessage(), e);
			System.out.println("SORRY!!! Unexpected error loading MainController: " + e.getMessage());
			System.exit(0);
		} finally {
			try {
				if (recordset != null)
					recordset.close();
			} catch (Exception ignored) {
			}
			try {
				if (connection != null)
					connection.close();
			} catch (Exception ignored) {
			}
		}

		return groups;
	}

	// ════════════════════════════════════════════════════════════════
	// Read DATASHEET sheet for the given Process+ScenarioNo+VerticalName.
	// The query now additionally filters by ScenarioNo AND VerticalName
	// so the correct TestDataSheet file is selected per scenario.
	// ════════════════════════════════════════════════════════════════
	public static void MainControlerDataSheet(String dataSheetProcess_param, String scenarioNo, String verticalName)
			throws FilloException, InterruptedException, IOException {

		Connection conn = null;
		Recordset recordset = null;

		try {
			Fillo fillo = new Fillo();
			conn = fillo.getConnection(mainControllerFilePath);
			logger.info(
					"Connected to MainController.xlsx for DATASHEET reading (Process={}, ScenarioNo={}, VerticalName={})",
					dataSheetProcess_param, scenarioNo, verticalName);

			// ── 1. Validate DATASHEET columns ──
			List<String> expectedDatasheetCols = Arrays.asList(
					"Si_No", "RunStatus", "Process", "TestDataSheet",
					"ImplicityWait", "ExplicityWait", "RepeatedFailed", "RetryCount");

			try {
				recordset = conn.executeQuery("SELECT * FROM DATASHEET");
				if (recordset != null) {
					List<String> actualCols = recordset.getFieldNames();
					List<String> missing = new ArrayList<>();
					for (String col : expectedDatasheetCols) {
						if (!actualCols.contains(col))
							missing.add(col);
					}
					if (!missing.isEmpty()) {
						logger.error("SORRY!!! '{}' columns missing from DATASHEET sheet.", missing);
						System.out.println("SORRY!!! '" + missing + "' columns missing from MainController DATASHEET.");
						// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
						throw new RuntimeException("Missing columns in DATASHEET: " + missing);
					}

					boolean dsHasScenarioNo = actualCols.contains("ScenarioNo");
					boolean dsHasVerticalName = actualCols.contains("VerticalName");
					recordset.close();

					// ── 2. Build query — filter by Process + ScenarioNo + VerticalName if columns
					// exist ──
					StringBuilder queryBuilder = new StringBuilder(
							"SELECT * FROM DATASHEET Where RunStatus = 'Y' and Process='" + dataSheetProcess_param
									+ "'");

					if (dsHasScenarioNo && scenarioNo != null && !scenarioNo.trim().isEmpty()) {
						queryBuilder.append(" and ScenarioNo='").append(scenarioNo.trim()).append("'");
					}
					if (dsHasVerticalName && verticalName != null && !verticalName.trim().isEmpty()) {
						queryBuilder.append(" and VerticalName='").append(verticalName.trim()).append("'");
					}

					String queryForProcessName = queryBuilder.toString();
					logger.info("DATASHEET query: {}", queryForProcessName);

					// ── 3. Fetch the matching row ──
					try {
						recordset = conn.executeQuery(queryForProcessName);
						if (recordset != null) {
							boolean foundProcess = false;

							while (recordset.next()) {
								foundProcess = true;
								dataSheetProcess = recordset.getField("Process");
								TestDataSheet = recordset.getField("TestDataSheet");
								ImplicityWait = recordset.getField("ImplicityWait");
								ExplicityWait = recordset.getField("ExplicityWait");
								RepeatedFailed = recordset.getField("RepeatedFailed");
								RetryCount = recordset.getField("RetryCount");

								if (dataSheetProcess != null && !dataSheetProcess.isEmpty()
										&& TestDataSheet != null && !TestDataSheet.isEmpty()) {

									logger.info(
											"DATASHEET entry: Process={}, TestDataSheet={}, ScenarioNo={}, VerticalName={}",
											dataSheetProcess, TestDataSheet, scenarioNo, verticalName);

									try {
										testDataSheetCheck(System.getProperty("userInput", TestDataSheet));
									} catch (Exception e) {
										logger.error("Error executing DataSheet for Process={}. Error: {}",
												dataSheetProcess_param, e.getMessage(), e);
										System.out.println("SORRY!!! Error during DataSheet execution for Process="
												+ dataSheetProcess_param + ". File: '" + TestDataSheet + "'.");
									}

								} else {
									logger.error(
											"SORRY!!! Missing Process or TestDataSheet value in DATASHEET row for Process='{}'.",
											dataSheetProcess_param);
									System.out
											.println("SORRY!!! Process or TestDataSheet column is empty in DATASHEET.");
									// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
									throw new RuntimeException(
											"Process or TestDataSheet column is empty in DATASHEET for Process='"
													+ dataSheetProcess_param + "'");
								}
							}

							if (!foundProcess) {
								logger.warn(
										"No matching row in DATASHEET for Process='{}', ScenarioNo='{}', VerticalName='{}'.",
										dataSheetProcess_param, scenarioNo, verticalName);
								System.out.println("⚠️ No matching row in DATASHEET for Process='"
										+ dataSheetProcess_param
										+ "', ScenarioNo='" + scenarioNo + "', VerticalName='" + verticalName + "'.");
							}

							recordset.close();
						}
					} catch (FilloException e) {
						logger.error("Error querying DATASHEET for Process='{}'. Error: {}", dataSheetProcess_param,
								e.getMessage(), e);
						System.out.println(
								"SORRY!!! Error reading DATASHEET for Process='" + dataSheetProcess_param + "'.");
						// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
						throw new RuntimeException("Error reading DATASHEET for Process='" + dataSheetProcess_param
								+ "': " + e.getMessage(), e);
					}
				}
			} catch (FilloException e) {
				logger.error("SORRY!!! 'DATASHEET' sheet not present or inaccessible. Error: {}", e.getMessage(), e);
				System.out.println("SORRY!!! 'DATASHEET' sheet not present. Check the sheet tab name.");
				// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
				throw new RuntimeException("DATASHEET sheet not present or inaccessible: " + e.getMessage(), e);
			}

		} catch (FilloException e) {
			logger.error("Failed to connect to MainController.xlsx for DATASHEET. Error: {}", e.getMessage(), e);
			System.out.println("SORRY!!! Cannot connect to MainController.xlsx. Ensure it is not open in Excel.");
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
			throw new RuntimeException("Cannot connect to MainController.xlsx for DATASHEET: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("Unexpected error in MainControlerDataSheet(Process={}). Error: {}", dataSheetProcess_param,
					e.getMessage(), e);
			System.out.println("SORRY!!! Unexpected error extracting DataSheet for Process=" + dataSheetProcess_param);
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
			throw new RuntimeException("Unexpected error extracting DataSheet for Process=" + dataSheetProcess_param
					+ ": " + e.getMessage(), e);
		} finally {
			try {
				if (recordset != null)
					recordset.close();
			} catch (Exception ignored) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ignored) {
			}
		}
	}

	// ════════════════════════════════════════════════════════════════
	// Verify DataSheet folder + file exist, count Sheet2 Y rows,
	// then invoke the execution loop.
	// ════════════════════════════════════════════════════════════════
	// ════════════════════════════════════════════════════════════════
	// Verify DataSheet folder + file exist via two-level subfolder:
	// DataSheet/{VerticalName}/{ScenarioNo}/{TestDataSheet}.xlsx
	// Both VerticalName and ScenarioNo come from MAIN_CONTROLLER.
	//
	// ⚠️ IMPORTANT: folder/file errors do NOT call System.exit(0).
	// Instead they set currentScenarioAborted=true + return so that
	// the scenario loop in mainControllerSheet() marks this scenario
	// FAILED, skips its remaining processes, and continues with the
	// NEXT scenario — exactly the same as AbortCondition behavior.
	//
	// Only the base DataSheet/ folder missing calls System.exit(0)
	// because that is a global config problem; no scenario can run.
	// ════════════════════════════════════════════════════════════════
	public static void testDataSheetCheck(String DataSheetFile) {
		String userInputDataSheetFolderPath = System.getProperty("userInputDataSheetFolderPath");

		// ── Resolve base DataSheet folder path ──
		if (userInputDataSheetFolderPath == null || userInputDataSheetFolderPath.trim().isEmpty()) {
			dataSheetFolderPath = System.getProperty("user.dir") + File.separator + "DataSheet";
		} else {
			File folder = new File(userInputDataSheetFolderPath.trim());
			if (folder.isAbsolute()) {
				dataSheetFolderPath = folder.getAbsolutePath();
			} else {
				dataSheetFolderPath = System.getProperty("user.dir") + File.separator
						+ userInputDataSheetFolderPath.trim();
			}
		}

		// ── Check 1: Base DataSheet/ folder exists ──
		// This is a global config issue — no scenario can run without it → System.exit
		File dataSheetFolder = new File(dataSheetFolderPath);
		if (!dataSheetFolder.exists() || !dataSheetFolder.isDirectory()) {
			logger.error("❌ ERROR: 'DataSheet' folder not found at path: {}", dataSheetFolderPath);
			System.out.println("==========================================================================");
			System.out.println("❌ ERROR: DataSheet folder not found!");
			System.out.println("   👉 Looked At  : " + dataSheetFolderPath);
			System.out.println("   👉 Resolution : -DuserInputDataSheetFolderPath=path/to/DataSheetFolder");
			System.out.println("==========================================================================");
			System.exit(0);
		}

		// ── Extract VerticalName and ScenarioNo from MAIN_CONTROLLER static fields ──
		String verticalName = (VerticalName != null && !VerticalName.trim().isEmpty()) ? VerticalName.trim() : "";
		String scenarioNo = (ScenarioNo != null && !ScenarioNo.trim().isEmpty()) ? ScenarioNo.trim() : "";

		// ── Guard: VerticalName must not be empty ──
		if (verticalName.isEmpty()) {
			logger.error("❌ SCENARIO FAIL: VerticalName is empty for Process='{}'. Skipping scenario.", Process);
			System.out.println("==========================================================================");
			System.out.println("❌ SCENARIO FAIL: VerticalName is empty in MAIN_CONTROLLER!");
			System.out.println("   👉 Process     : " + Process);
			System.out.println("   👉 Action      : Scenario marked FAILED — skipping remaining processes.");
			System.out.println("   👉 Resolution  : Fill in the VerticalName column in MAIN_CONTROLLER sheet.");
			System.out.println("==========================================================================");
			currentScenarioAborted = true;
			lastAbortedProcess = (Process != null ? Process : "Unknown");
			return;
		}

		// ── Guard: ScenarioNo must not be empty ──
		if (scenarioNo.isEmpty()) {
			logger.error("❌ SCENARIO FAIL: ScenarioNo is empty for Process='{}'. Skipping scenario.", Process);
			System.out.println("==========================================================================");
			System.out.println("❌ SCENARIO FAIL: ScenarioNo is empty in MAIN_CONTROLLER!");
			System.out.println("   👉 Process     : " + Process);
			System.out.println("   👉 Action      : Scenario marked FAILED — skipping remaining processes.");
			System.out.println("   👉 Resolution  : Fill in the ScenarioNo column in MAIN_CONTROLLER sheet.");
			System.out.println("==========================================================================");
			currentScenarioAborted = true;
			lastAbortedProcess = (Process != null ? Process : "Unknown");
			return;
		}

		// ── Check 2: DataSheet/{VerticalName}/ folder exists ──
		String verticalFolderPath = dataSheetFolderPath + File.separator + verticalName;
		File verticalFolder = new File(verticalFolderPath);
		if (!verticalFolder.exists() || !verticalFolder.isDirectory()) {
			logger.error("❌ SCENARIO FAIL: Vertical folder '{}' not found inside DataSheet/. Skipping scenario.",
					verticalName);
			System.out.println("==========================================================================");
			System.out.println("❌ SCENARIO FAIL: Vertical folder not found inside DataSheet/!");
			System.out.println("   👉 Vertical    : " + verticalName);
			System.out.println("   👉 Looked At   : " + verticalFolderPath);
			System.out.println("   👉 Action      : Scenario marked FAILED — skipping remaining processes.");
			System.out.println("   👉 Resolution  : Create the folder  →  DataSheet"
					+ File.separator + verticalName);
			System.out.println("==========================================================================");
			currentScenarioAborted = true;
			lastAbortedProcess = (Process != null ? Process : "Unknown");
			return;
		}

		// ── Check 3: DataSheet/{VerticalName}/{ScenarioNo}/ folder exists ──
		String scenarioFolderPath = verticalFolderPath + File.separator + scenarioNo;
		File scenarioFolder = new File(scenarioFolderPath);
		if (!scenarioFolder.exists() || !scenarioFolder.isDirectory()) {
			logger.error("❌ SCENARIO FAIL: Scenario folder '{}' not found inside DataSheet/{}/. Skipping scenario.",
					scenarioNo, verticalName);
			System.out.println("==========================================================================");
			System.out.println("❌ SCENARIO FAIL: Scenario folder not found inside DataSheet/" + verticalName + "/!");
			System.out.println("   👉 ScenarioNo  : " + scenarioNo);
			System.out.println("   👉 Looked At   : " + scenarioFolderPath);
			System.out.println("   👉 Action      : Scenario marked FAILED — skipping remaining processes.");
			System.out.println("   👉 Resolution  : Create the folder  →  DataSheet"
					+ File.separator + verticalName + File.separator + scenarioNo);
			System.out.println("==========================================================================");
			currentScenarioAborted = true;
			lastAbortedProcess = (Process != null ? Process : "Unknown");
			return;
		}

		// ── Check 4: Test script file exists inside
		// DataSheet/{VerticalName}/{ScenarioNo}/ ──
		dataSheetFilePath = scenarioFolderPath + File.separator + DataSheetFile;
		File dataSheetFileObj = new File(dataSheetFilePath);
		if (!dataSheetFileObj.exists()) {
			logger.error("❌ SCENARIO FAIL: File '{}' not found inside DataSheet/{}/{}/ at: {}. Skipping scenario.",
					DataSheetFile, verticalName, scenarioNo, dataSheetFilePath);
			System.out.println("==========================================================================");
			System.out.println("❌ SCENARIO FAIL: Test Data Excel file not found!");
			System.out.println("   👉 File Name   : " + DataSheetFile);
			System.out.println("   👉 Vertical    : " + verticalName);
			System.out.println("   👉 ScenarioNo  : " + scenarioNo);
			System.out.println("   👉 Looked At   : " + dataSheetFilePath);
			System.out.println("   👉 Action      : Scenario marked FAILED — skipping remaining processes.");
			System.out.println("   👉 Resolution  : Place the Excel file at:");
			System.out.println("                    DataSheet" + File.separator + verticalName
					+ File.separator + scenarioNo + File.separator + DataSheetFile);
			System.out.println("==========================================================================");
			currentScenarioAborted = true;
			lastAbortedProcess = (Process != null ? Process : "Unknown");
			return;
		}

		// ── All checks passed — log the resolved path ──
		logger.info("✅ DataSheet file resolved → {}", dataSheetFilePath);
		// System.out.println("✅ DataSheet file resolved: DataSheet" + File.separator
		// + verticalName + File.separator + scenarioNo + File.separator +
		// DataSheetFile);

		try {
			int sheet2rowCount = dataSheetTwoRowCount(DataSheetFile);

			if (sheet2rowCount == 0) {
				logger.warn("No data rows found in Sheet2 for Process='{}'. Check RunStatus and ApplicationName.",
						Process);
				System.out.println("⚠️ No data rows found in Sheet2. Check RunStatus and ApplicationName columns.");
				return;
			}

			logger.info("Found {} data row(s) in Sheet2 for Process='{}'", sheet2rowCount, Process);
			ExecutionLogs();
			ConnectToDataSheet.extractAllData(sheet2rowCount);

		} catch (Exception e) {
			logger.error("Exception during DataSheet extraction/execution for file '{}'. Error: {}",
					DataSheetFile, e.getMessage(), e);
			System.out.println("Execution Error for file '" + DataSheetFile + "': " + e.getMessage());
		}
	}

	// ════════════════════════════════════════════════════════════════
	// Count Sheet2 rows with RunStatus=Y and matching ApplicationName.
	// ════════════════════════════════════════════════════════════════
	public static int dataSheetTwoRowCount(String DataSheetFile) throws Exception {
		Connection conn = null;
		Recordset recordset = null;
		int count = 0;
		try {
			Fillo fillo = new Fillo();
			conn = fillo.getConnection(dataSheetFilePath);

			String query = "SELECT * FROM Sheet2 WHERE RUNSTATUS='Y' and ApplicationName='" + Process + "'";
			recordset = conn.executeQuery(query);

			while (recordset.next()) {
				count++;
			}
			logger.info("Sheet2 row count for Process='{}': {}", Process, count);

		} catch (FilloException e) {
			logger.error("Error counting Sheet2 rows for Process='{}'. Error: {}", Process, e.getMessage());
			System.out.println("SORRY!!! Error reading Sheet2 in '" + DataSheetFile
					+ "'. Check 'Sheet2' tab, ApplicationName='" + Process + "', RunStatus='Y'.");
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it.
			throw new RuntimeException(
					"Error reading Sheet2 in '" + DataSheetFile + "' for Process='" + Process + "': " + e.getMessage(),
					e);
		} finally {
			try {
				if (recordset != null)
					recordset.close();
			} catch (Exception ignored) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception ignored) {
			}
		}
		return count;
	}

	// ════════════════════════════════════════════════════════════════
	// Prints a formatted execution banner before each process runs.
	// ════════════════════════════════════════════════════════════════
	public static void ExecutionLogs() {
		String output = "\n\n╔══════════════════════════════════════════════╗\n"
				+ "║               EXECUTION DETAILS              ║\n"
				+ "╠══════════════════════════════════════════════╣\n"
				+ "║ ScenarioNo:      " + String.format("%-28s", ScenarioNo != null ? ScenarioNo : "NULL") + "║\n"
				+ "║ VerticalName:    " + String.format("%-28s", VerticalName != null ? VerticalName : "NULL") + "║\n"
				+ "║ Platform:        " + String.format("%-28s", PlatForm != null ? PlatForm.toUpperCase() : "NULL")
				+ "║\n"
				+ "║ Execution Type:  "
				+ String.format("%-28s", ExecutionType != null ? ExecutionType.toUpperCase() : "NULL") + "║\n"
				+ "║ Test Case:       " + String.format("%-28s", Process != null ? Process : "NULL") + "\n"
				+ "║ Script File:     " + String.format("%-28s", System.getProperty("userInput", TestDataSheet)) + "\n"
				+ "╚══════════════════════════════════════════════╝";

		System.out.println(output);
		logger.info("{}\n\n", output);
	}

}
