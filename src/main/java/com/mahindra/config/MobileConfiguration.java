package com.mahindra.config;

import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.mahindra.actions.*;
import com.mahindra.config.*;
import com.mahindra.core.*;
import com.mahindra.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MobileConfiguration {

	public static String Si_No;
	public static String Process;
	public static String App_PackageName;
	public static String App_PackageActivityName;
	public static String DeviceName;
	public static String DevicePlatform;
	public static String DevicePlatformVersion;
	public static String AppReset;
	public static String Pre_InstalledApp;
	public static String AppPath;
	public static String AppiumPort;
	public static String TestingPlatform;
	public static String UserName;
	public static String AccessKey;

	// FilePath
	public static String mainControllerFilePath;

	public final static Logger logger = LogManager.getLogger(MobileConfiguration.class.getName());

	public static void mobileConfigurationSheet() {
		// ✅ Fix: Use the globally resolved mainControllerFilePath from
		// ConnectToMainController
		mainControllerFilePath = ConnectToMainController.mainControllerFilePath;

		try {
			Fillo fillo = new Fillo();
			Connection connection = fillo.getConnection(mainControllerFilePath);

			String queryForProcessName = "SELECT * FROM MOBILE_CONFIGURATION Where RunStatus='Y' and Process = '"
					+ ConnectToMainController.Process + "'";
			Recordset recordset = null;

			try {
				recordset = connection.executeQuery(queryForProcessName);
				if (recordset != null) {

					List<List<Object>> rowlist = new ArrayList<>();

					while (recordset.next()) {
						List<String> columns = recordset.getFieldNames();
						List<Object> rowvalues = new ArrayList<Object>();

						for (String column : columns) {
							rowvalues.add(recordset.getField(column));
						}
						rowlist.add(rowvalues);
					}

					// ✅ Guard: If query returned 0 rows, fail immediately with a clear message
					if (rowlist.isEmpty()) {
						logger.error(
								"❌ ERROR: No matching row found in MOBILE_CONFIGURATION sheet. Process='{}', RunStatus='Y'",
								ConnectToMainController.Process);
						System.out
								.println("==========================================================================");
						System.out.println("❌ ERROR: No data found in MOBILE_CONFIGURATION sheet!");
						System.out.println("   👉 Process Searched : " + ConnectToMainController.Process);
						System.out.println("   👉 Condition        : RunStatus = 'Y'");
						System.out.println("   👉 Resolution       : Check that MOBILE_CONFIGURATION sheet has a row");
						System.out.println("                         where Process = '"
								+ ConnectToMainController.Process + "' and RunStatus = 'Y'");
						System.out
								.println("==========================================================================");
						// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
						// mark this scenario FAILED, and continue with the next scenario.
						throw new RuntimeException("No matching row in MOBILE_CONFIGURATION for Process='" + ConnectToMainController.Process + "'");
					}

					for (int i = 0; i < rowlist.size(); i++) {
						List<Object> row = (List<Object>) rowlist.get(i);

						Si_No = (String) row.get(0);
						Process = (String) row.get(2);
						App_PackageName = (String) row.get(3);
						App_PackageActivityName = (String) row.get(4);
						DeviceName = (String) row.get(5);
						DevicePlatform = (String) row.get(6);
						DevicePlatformVersion = (String) row.get(7);
						AppReset = (String) row.get(8);
						Pre_InstalledApp = (String) row.get(9);
						AppPath = (String) row.get(10);
						AppiumPort = (String) row.get(11);
						TestingPlatform = (String) row.get(12);
						UserName = (String) row.get(13);
						AccessKey = (String) row.get(14);

						logger.info("Process Name = " + Process + "\n");
						logger.info("DataSheetFile Name= " + ConnectToMainController.TestDataSheet + "\n\n");
					}
				}
			} catch (Exception e) {
				logger.error("❌ ERROR: MOBILE_CONFIGURATION sheet query failed. Process='{}'. Query='{}'. Error: {}",
						ConnectToMainController.Process, queryForProcessName, e.getMessage());
				System.out.println("==========================================================================");
				System.out.println("❌ ERROR: Failed to read MOBILE_CONFIGURATION sheet!");
				System.out.println("   👉 Process Searched : " + ConnectToMainController.Process);
				System.out.println("   👉 Possible Causes  :");
				System.out.println("       1. 'MOBILE_CONFIGURATION' sheet/tab does NOT exist in MainController.xlsx");
				System.out.println("       2. Process name mismatch — check spelling and case");
				System.out.println("       3. RunStatus column is not set to 'Y' for this Process");
				System.out.println("   👉 Error Detail     : " + e.getMessage());
				System.out.println("==========================================================================");
				// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
				// mark this scenario FAILED, and continue with the next scenario.
				throw new RuntimeException("MOBILE_CONFIGURATION query failed for Process='" + ConnectToMainController.Process + "': " + e.getMessage(), e);
			} finally {
				// ✅ Properly close Fillo resources
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

		} catch (Exception e) {
			System.out.println("==========================================================================");
			System.out.println("❌ ERROR: Failed to read Mobile Configuration from Main Controller file!");
			System.out.println("   👉 Path  : " + mainControllerFilePath);
			System.out.println("   👉 Error : " + e.getMessage());
			System.out.println("==========================================================================");
			logger.error("❌ ERROR: Failed to read Mobile Configuration. Path: {}. Error: {}", mainControllerFilePath,
					e.getMessage(), e);
			// ✅ FIX: throw instead of System.exit(0) so the scenario loop can catch it,
			// mark this scenario FAILED, and continue with the next scenario.
			throw new RuntimeException("Failed to read Mobile Configuration from: " + mainControllerFilePath + ": " + e.getMessage(), e);
		}

	}

}