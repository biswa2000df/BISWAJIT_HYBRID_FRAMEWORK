package com.mahindra.utils;

import com.mahindra.config.*;
import com.mahindra.core.*;
import com.mahindra.actions.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.github.javafaker.Faker;

public class UtilsActivity extends ConnectToDataSheet {

	ExtentHtmlReporter htmlReport;
	ExtentReports extent;
	static ExtentTest test;
	public static String destFileScrnshot;
	static String yearFormat;
	static String time;
	public static String Extent_ReportFile;
	public static String ssDatafield = null;
	public static String ssDataSheet2Value = null;
	public static long executionEndTime;
	public static String executionEndTimeProperFormat;
	public static String TotalExecutionTime;
	public static String eachStepExecution_ReportFile;

	public final static Logger logger = LogManager.getLogger(UtilsActivity.class.getName());

	public String takeScreenShot(WebDriver driver) throws IOException {

		String screenShotFileName = ScenarioID + "_" + Module + "_" + TestCaseStepID + "_" + PageName + "_";

		File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		String destPath = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "SCREENSHOTs",
				"screenshotMethod");
		destFileScrnshot = destPath + File.separator + screenShotFileName + time + ".png";

		FileUtils.copyFile(srcFile, new File(destFileScrnshot));
		// return destFileScrnshot;
		return ".." + File.separator + "SCREENSHOTs" + File.separator + screenShotFileName + time + ".png";

	}

	public static String getFormat(String Year, String Month, String Date, String projectName, String Type,
	                               String methodName) {

		Date date = new Date();
		SimpleDateFormat yer = new SimpleDateFormat(Year);
		SimpleDateFormat mnt = new SimpleDateFormat(Month);
		SimpleDateFormat dat = new SimpleDateFormat(Date);
		SimpleDateFormat tm = new SimpleDateFormat("HH_mm_ss");
		SimpleDateFormat fullyer = new SimpleDateFormat("yyyy-MM-dd");
		yearFormat = fullyer.format(date);

		String year = yer.format(date);
		String Mnth = mnt.format(date);
		String Dt = dat.format(date);
		time = tm.format(date);

		// System.out.println(year);
		// System.out.println(Mnth);
		// System.out.println(Dt);

		String f = null;

		f = System.getProperty("user.dir") + File.separator + "RESULT" + File.separator + year + File.separator + Mnth
				+ File.separator + Dt + File.separator + projectName + File.separator + Type;
		new File(f).mkdirs();
		// System.out.println("==========================================" + f);
		return f;

	}

	/**
	 * Builds a clean filename-safe scenario tag from ScenarioNo + VerticalName.
	 * Example: ScenarioNo="SC_01", VerticalName="XPL" → "SC_01_XPL"
	 * Falls back to "SC" if both are empty (should never happen in normal flow).
	 */
	public static String buildScenarioTag() {
		String scenarioNo = (ConnectToMainController.ScenarioNo != null
				&& !ConnectToMainController.ScenarioNo.trim().isEmpty())
				? ConnectToMainController.ScenarioNo.trim()
				: "";
		String verticalName = (ConnectToMainController.VerticalName != null
				&& !ConnectToMainController.VerticalName.trim().isEmpty())
				? ConnectToMainController.VerticalName.trim()
				: "";

		String tag;
		if (!scenarioNo.isEmpty() && !verticalName.isEmpty()) {
			tag = scenarioNo + "_" + verticalName;
		} else if (!scenarioNo.isEmpty()) {
			tag = scenarioNo;
		} else if (!verticalName.isEmpty()) {
			tag = verticalName;
		} else {
			tag = "SC";
		}
		// Sanitize: replace any character that is not alphanumeric, hyphen, or
		// underscore
		return tag.replaceAll("[^A-Za-z0-9_\\-]", "_");
	}

	public void extentReport() throws IOException {

		String destFile = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "REPORTs",
				"ReportMethod");
		String scenarioTag = buildScenarioTag();
		Extent_ReportFile = destFile + File.separator + scenarioTag + "_"
				+ ConnectToMainController.Process + "_Report_" + time
				+ ".html";

		htmlReport = new ExtentHtmlReporter(Extent_ReportFile);
		extent = new ExtentReports();
		extent.attachReporter(htmlReport);

		htmlReport.config().setDocumentTitle(" MAHINDRA FINANCE ");// Title of the report
		htmlReport.config().setReportName(ConnectToMainController.ApplicationName + " Automation Report");// Name of the
		// report
		htmlReport.config().setTestViewChartLocation(ChartLocation.BOTTOM);
		htmlReport.config().setChartVisibilityOnOpen(false);
		htmlReport.config().setTheme(Theme.DARK);

		extent.setSystemInfo("Company Name", "MAHINDRA FINANCE");
		extent.setSystemInfo("FrameWork", "Biswajit AI-POWERED Self-Healing Automation Framework");
		extent.setSystemInfo("Project Name", ConnectToMainController.ApplicationName);
		extent.setSystemInfo("SVP", "NARESH YADAV");
		extent.setSystemInfo("Test Lead", "Vikrant & Shankar & Shruti");
		extent.setSystemInfo("Team Members", "Shantesh, Namrata, Shubham, Dinesh, Dhurvesh");
		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.setSystemInfo("Framework Developer Name", "Biswajit Sahoo");
		extent.setSystemInfo("Tester Name", GetUserName(System.getProperty("user.name")));
		extent.setSystemInfo("Execution", ConnectToMainController.ExecutionType.toUpperCase());
		// extent.setSystemInfo("Device", MobileConfiguration.DevicePlatform);

	}

	public void testCaseCreate() {

		test = extent
				.createTest(
						"<font color=\"BlueViolet\"><b>" + ScenarioID + "</b></font> - <font color=\"Brown\"><b>"
								+ Module + "</b></font> - <font color=\"Green\"><b>" + TestCaseID + "</b></font> ( "
								+ TestCaseDescription + " )",
						"</br><h4><font color=\"Lime\"><b>" + Module.toUpperCase() + "</b></font></h4>")
				.createNode("<h5><b>" + TestCaseDescription + "</b></h5>").assignCategory("BISWAJIT");

	}

	public static void testcaseInfoWithDataField() {
		ssDatafield = DataField;
		ssDataSheet2Value = dataSheet2Value;

		test.log(Status.INFO,
				"<font color=\"Aqua\"><b>Module - </b></font>" + Module + "  "
						+ " <font color=\"Lime\"><b>Step - </b></font>" + Si_No + "  "
						+ " <font color=\"Red\"><b>Data Field - </b></font>" + ssDatafield.toUpperCase() + "  "
						+ " <font color=\"MediumSlateBlue\"><b>Test Data - </b></font>" + ssDataSheet2Value
						+ " <font color=\"Yellow\"><b>StepDescription - </b></font>" + TestCaseStepDescription);
	}

	public static void testcaseInfoWithoutDataField() {
		test.log(Status.INFO,
				"<font color=\"Aqua\"><b>Module - </b></font>" + Module + "  "
						+ " <font color=\"Lime\"><b>Step - </b></font>" + Si_No + "  "
						+ " <font color=\"MediumSlateBlue\"><b>Action - </b></font>" + Action.toUpperCase()
						+ " <font color=\"Gold\"><b>StepDescription - </b></font>" + TestCaseStepDescription);

	}

	public void passTestCase() throws IOException {

		String TakeScreenshotPath;
		TakeScreenshotPath = takeScreenShot(driver);

		test.log(Status.PASS,
				"<h6><br><font color=\"Red\"><b>Expected Result is - </b></font></h6>" +
						"<h6><font color=\"Lime\"><b>" + ssDataSheet2Value.toUpperCase() + "</b></font></h6>" +
						"<h6><br><font color=\"Red\"><b>Actual Result is - </b></font></h6>" +
						"<h6><font color=\"Lime\"><b>" + String.valueOf(Function.ActualResult).toUpperCase()
						+ "</b></font></h6><br>",
				MediaEntityBuilder.createScreenCaptureFromPath(TakeScreenshotPath).build());

	}

	public void failTestCase() throws IOException {

		String TakeScreenshotPath;
		TakeScreenshotPath = takeScreenShot(driver);

		test.log(Status.FAIL,
				"<h6><br><font color=\"Red\"><b>Expected Result is - </b></font></h6>" +
						"<h6><font color=\"Lime\"><b>" + ssDataSheet2Value.toUpperCase() + "</b></font></h6>" +
						"<h6><br><font color=\"Red\"><b>Actual Result is - </b></font></h6>" +
						"<h6><font color=\"Red\"><b>" + String.valueOf(Function.ActualResult).toUpperCase()
						+ "</b></font></h6><br>",
				MediaEntityBuilder.createScreenCaptureFromPath(TakeScreenshotPath).build());
	}

	// why i write method if checkvisibility failed or pass then take the screenshot
	// but if there is no checkvisibility and element is not found then also take
	// a screenshot if it failed thats why i write the method
	public void withOutValidationFailTestCase(Exception ERROR) throws IOException {

		String TakeScreenshotPath;
		TakeScreenshotPath = takeScreenShot(driver);

		test.log(Status.FAIL,
				"<h6><br><font color=\"Red\"><b>Expected Result is - </b></font></h6>" +
						"<h6><font color=\"Lime\"><b>" + TestCaseStepDescription + "</b></font></h6>" +
						"<h6><br><font color=\"Red\"><b>Actual Result is - </b></font></h6>" +
						"<h6><font color=\"Red\"><b>" + "Error is " + ERROR + ", So Please Check the ScreenShot"
						+ "</b></font></h6><br>",
				MediaEntityBuilder.createScreenCaptureFromPath(TakeScreenshotPath).build());
	}

	public void ExtentFlush() {
		extent.flush();
	}

	// create html Table here

	public static void CreateHtmlTable() throws IOException {
		try {
			String htmlTable = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "HtmlTables",
					"htmlTableMethod");
			String scenarioTag = buildScenarioTag();
			String filename = htmlTable + File.separator + scenarioTag + "_"
					+ ConnectToMainController.Process + "_"
					+ "HtmlTable_Report_" + time + ".html";
			String backGroundImage = new File(System.getProperty("user.dir") + File.separator + "DataSheet"
					+ File.separator + "BackGroundImage" + File.separator + "Automation1.png").toURI().toString();

			try (FileWriter writer = new FileWriter(filename)) {
				writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
				writer.write("<style>");
				writer.write("body { background-image: url('" + backGroundImage
						+ "'); background-size: cover; background-repeat: no-repeat; }");
				writer.write("table { border-collapse: collapse; width: 50%; margin: auto; margin-top: 20px; }");
				writer.write(
						"th, td { border: 1px solid black; padding: 8px; text-align: center; background-color: #E4E5E5; }");
				writer.write("th { background-color: #E4E5E5; }");
				writer.write("h1 { text-align: center; color: white; margin-top: 20px; font-weight: bold; }");
				writer.write("h2 { text-align: center; color: white; margin-top: 20px; font-weight: bold; }");
				writer.write("</style>");
				writer.write("</head>\n<body>\n");

				writer.write("<h1><b>Mahindra & Mahindra Financial Services Limited - "
						+ ConnectToMainController.ApplicationName + " Automation Test Report</b></h1>\n");
				writer.write("<h2>Welcome, Biswajit AI-POWERED Self-Healing Automation Testing Report</h2>\n");

				writer.write("<table border=\"1\">\n");
				writer.write("<tr>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">Project</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#F39C12; color: white;\">Total TCs</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#FF6347; color: white;\">Passed TCs</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#4682B4; color: white;\">Failed TCs</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#32CD32; color: white;\">Total Validations in all the TCs</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#1E90FF; color: white;\">Passed Validations</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#C0392B; color: white;\">Failed Validations</th>");

				if (ConnectToMainController.StepsScreenshot != null
						&& ConnectToMainController.StepsScreenshot.equalsIgnoreCase("Y")) {
					writer.write(
							"<th style=\"text-align:center; border: 1px solid black; background-color:#8E44AD; color: white;\">STEPs Report</th>");
				}

				writer.write("<th>Report</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">ExecutionTime</th>"
						+ "</tr>");

				writer.write("<tr>" + "<td style=\"text-align:center; border: 1px solid black;\">"
						+ ConnectToMainController.ApplicationName + "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">"
						+ ConnectToDataSheet.totalTestStep + "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.pass
						+ "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.fail
						+ "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">"
						+ ConnectToDataSheet.totalValidations + "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">"
						+ ConnectToDataSheet.passValidations + "</td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">"
						+ ConnectToDataSheet.failedValidations + "</td>");

				if (ConnectToMainController.StepsScreenshot != null
						&& ConnectToMainController.StepsScreenshot.equalsIgnoreCase("Y")) {
					writer.write("<td><a href=" + eachStepExecution_ReportFile
							+ " target=_blank>View STEPs Report</a></td>");
				}

				writer.write("<td><a href=" + Extent_ReportFile + " target=_blank>View Report</a></td>"
						+ "<td style=\"text-align:center; border: 1px solid black;\">" + TotalExecutionTime
						+ "</td></tr>");

				writer.write("</table>\n");
				writer.write("</body>\n</html>");
			}

		} catch (Exception e) {
			logger.error("Error executing CreateHtmlTable: {}", e.getMessage(), e);
		}
	}

	public static void createEveryStepHtmlReport() throws IOException {
		try {
			eachStepExecution_ReportFile = null;

			String htmlTable = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "STEPs_REPORTs",
					"everyStepScreenShotHtmlTable");
			String scenarioTag = buildScenarioTag();

			String filename = htmlTable + File.separator + scenarioTag + "_"
					+ ConnectToMainController.Process + "_"
					+ "StepHtmlTable_Report_" + time + ".html";

			eachStepExecution_ReportFile = filename;

			String backGroundImage = new File(System.getProperty("user.dir") + File.separator + "DataSheet"
					+ File.separator + "BackGroundImage" + File.separator + "Automation1.png").toURI().toString();

			try (FileWriter writer = new FileWriter(filename)) {
				writer.write("<!DOCTYPE html>\n<html>\n<head>\n");

				writer.write("<style>");
				writer.write("body { background-image: url('" + backGroundImage
						+ "'); background-size: cover; background-repeat: no-repeat; }");
				writer.write(
						"table { border-collapse: collapse; width: 100%; margin: auto; margin-top: 20px; table-layout: fixed; }");
				writer.write(
						"th, td { border: 1px solid black; padding: 8px; text-align: center; background-color: rgba(228, 229, 229, 0.7); white-space:nowrap; max-width: 300px; overflow-x: hidden; }"); // nowrap,
				// scroll,
				// max-width
				writer.write("th:hover, td:hover { overflow-x: auto; }");// Show on hover
				writer.write("th { background-color: rgba(228, 229, 229, 0.7); }");
				writer.write("h1 { text-align: center; color: white; margin-top: 20px; font-weight: bold; }");
				writer.write("h2 { text-align: center; color: white; margin-top: 20px; font-weight: bold; }");
				writer.write(".expanded {");
				writer.write("  max-width: 80% !important;");
				writer.write("}");
				writer.write("</style>");

				// JavaScript and CSS for image toggle
				writer.write("<script>");
				writer.write("function toggleImageSize(imgId) {");
				writer.write("  var img = document.getElementById(imgId);");
				writer.write("  if (img.classList.contains('expanded')) {");
				writer.write("    img.classList.remove('expanded');");
				writer.write("    img.style.maxWidth = '200px';");
				writer.write("  } else {");
				writer.write("    img.classList.add('expanded');");
				writer.write("    img.style.maxWidth = '80%';");
				writer.write("  }");
				writer.write("}");
				writer.write("</script>");

				writer.write("</head>\n<body>\n");

				writer.write("<h1><b>Mahindra & Mahindra Financial Services Limited - "
						+ ConnectToMainController.ApplicationName + " Automation Test Report</b></h1>\n");
				writer.write("<h2>Welcome, Biswajit AI-POWERED Self-Healing Automation Testing Report</h2>\n");

				writer.write("<table border=\"1\">\n");
				writer.write("<tr>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">SI_No</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#F39C12; color: white;\">TestCaseStep</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#FF6347; color: white;\">LocatorName</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#4682B4; color: white;\">LocatorValue</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#32CD32; color: white;\">DataField</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#FF6347; color: white;\">Action</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#1E90FF; color: white;\">TestData</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#C0392B; color: white;\">ExecutionTime</th>"
						+ "<th style=\"text-align:center; border: 1px solid black; background-color:#8E44AD; color: white;\">ScreenShot</th>"
						+ "</tr>");

				// Generate rows from the reportData list
				for (int rowNumber = 0; rowNumber < everyStepScreenShot.size(); rowNumber++) {
					List<String> row = everyStepScreenShot.get(rowNumber);
					writer.write("<tr>");
					for (int i = 0; i < row.size(); i++) {
						String cell = row.get(i);
						if (i == row.size() - 1) { // Last column is the screenshot
							writer.write("<td style=\"text-align:center; border: 1px solid black;\">");
							if (cell != null && !cell.trim().isEmpty()) {
								writer.write("<img id=\"img" + rowNumber + "\" src=\"file:///" + cell
										+ "\" alt=\"Screenshot\" style=\"max-width: 100px; cursor: pointer;\" onclick=\"toggleImageSize('img"
										+ rowNumber + "')\">");
							} else {
								writer.write("No Screenshot");
							}
							writer.write("</td>");
						} else {
							writer.write("<td style=\"text-align:center; border: 1px solid black;\">" + cell + "</td>");
						}
					}
					writer.write("</tr>");
				}
				writer.write("</table>\n");
				writer.write("</body>\n</html>");
			}

		} catch (Exception e) {
			logger.error("Error creating Every Step HTML Report: {}", e.getMessage(), e);
		}
	}

	public static void webUIReport() {
		try {
			String htmlTable = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "MASTERREPORTs",
					"MASTERREPORTs");
			String scenarioTag = buildScenarioTag();
			String filename = htmlTable + File.separator + scenarioTag + "_"
					+ ConnectToMainController.Process + "_"
					+ "MasterUIReport_" + time + ".html";

			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), StandardCharsets.UTF_8))) {

				writer.write("<!DOCTYPE html>\n");
				writer.write("<html lang=\"en\">\n");
				writer.write("<head>\n");
				writer.write("  <meta charset=\"UTF-8\" />\n");
				writer.write("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n");
				writer.write("  <title>Biswajit Automation Execution Summary Dashboard</title>\n");
				writer.write(
						"  <link href=\"https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap\" rel=\"stylesheet\" />\n");
				writer.write("  <style>\n");
				writer.write("    * { margin: 0; padding: 0; box-sizing: border-box; }\n");
				writer.write(
						"    body { font-family: 'Poppins', sans-serif;  background: linear-gradient(to bottom right, #f5f7fa, #c3cfe2); padding: 30px; color: #333; }\n");
				writer.write("    .dashboard { max-width: 1200px; margin: auto; }\n");
				writer.write("    h1 { text-align: center; margin-bottom: 40px; font-size: 32px; color: #2c3e50; }\n");
				writer.write(
						"    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; }\n");
				writer.write(
						"    .card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 6px 15px rgba(0,0,0,0.1); transition: transform 0.3s ease; }\n");
				writer.write("    .card:hover { transform: translateY(-5px); }\n");
				writer.write("    .card h2 { font-size: 18px; color: #2980b9; margin-bottom: 10px; }\n");
				writer.write("    .card p { font-size: 15px; margin: 5px 0; }\n");
				writer.write("    .summary { margin-top: 30px; }\n");
				writer.write(
						"    .summary .card { background: linear-gradient(135deg, #a01669, #1abc9c); color: white; cursor: pointer; }\n");
				writer.write("    .summary .card h2, .summary .card p { color: white; }\n");
				writer.write("    .status-box { display: flex; gap: 20px; margin-top: 20px; flex-wrap: wrap; }\n");
				writer.write(
						"    .status { flex: 1; min-width: 180px; padding: 15px; background: #fff; border-radius: 10px; text-align: center; box-shadow: 0 4px 10px rgba(0,0,0,0.08); }\n");
				writer.write("    .status.pass { border-left: 6px solid #2ecc71; }\n");
				writer.write("    .status.fail { border-left: 6px solid #e74c3c; }\n");
				writer.write("    .status.total { border-left: 6px solid #3498db; }\n");
				writer.write("    .status h3 { margin-bottom: 10px; font-size: 16px; }\n");
				writer.write("    .status span { font-size: 24px; font-weight: bold; }\n");
				writer.write("    footer { text-align: center; margin-top: 40px; font-size: 13px; color: #aaa; }\n");
				// Premium Glow Card for Total Test Cases
				writer.write(
						"    .glow-card { background: linear-gradient(135deg, #0f0c29, #302b63, #24243e); border-radius: 16px; padding: 28px 20px; text-align: center; color: #fff; box-shadow: 0 0 20px rgba(108, 92, 231, 0.5), 0 0 40px rgba(108, 92, 231, 0.2); animation: pulseGlow 2.5s ease-in-out infinite alternate; margin-bottom: 20px; }\n");
				writer.write(
						"    .glow-card h3 { font-size: 16px; color: #a29bfe; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 2px; }\n");
				writer.write(
						"    .glow-card .glow-number { font-size: 52px; font-weight: 700; background: linear-gradient(90deg, #6c5ce7, #a29bfe, #fd79a8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }\n");
				writer.write("    .glow-card .glow-label { font-size: 13px; color: #b2bec3; margin-top: 8px; }\n");
				writer.write(
						"    @keyframes pulseGlow { 0% { box-shadow: 0 0 15px rgba(108, 92, 231, 0.4), 0 0 30px rgba(108, 92, 231, 0.15); } 100% { box-shadow: 0 0 25px rgba(108, 92, 231, 0.7), 0 0 50px rgba(108, 92, 231, 0.3); } }\n");
				writer.write("    .link-card a {\n");
				writer.write("      display: block;\n");
				writer.write("      color: #2980b9;\n");
				writer.write("      text-decoration: none;\n");
				writer.write("      transition: color 0.3s ease;\n");
				writer.write("    }\n");
				writer.write("    .link-card a:hover {\n");
				writer.write("      color: #1abc9c;\n");
				writer.write("      text-decoration: underline;\n");
				writer.write("    }\n");
				writer.write("		footer p {\n");
				writer.write(" 		 color: black;\n");
				writer.write("  	 font-size: 14px;\n");
				writer.write("	  }\n");

				writer.write("  </style>\n");
				writer.write("</head>\n");

				writer.write("<body>\n");
				writer.write("  <div class=\"dashboard\">\n");
				writer.write("    <h1>Mahindra & Mahindra Financial Services Limited - "
						+ ConnectToMainController.ApplicationName + " Automation Test Report</h1>\n");
				writer.write(
						"    <h2 style=\"text-align:center; margin-bottom: 30px; color:#2980b9;\">Welcome, Biswajit AI-POWERED Self-Healing Automation Testing Report</h2>\n");

				writer.write("    <div class=\"grid\">\n");
				writer.write("      <div class=\"card\">\n");
				writer.write("        <h2>\uD83D\uDCC1 Project Details</h2>\n");
				writer.write("        <p><strong>Company:</strong> MAHINDRA FINANCE</p>\n");
				writer.write("        <p><strong>Project:</strong> SuperApp_" + ConnectToMainController.ApplicationName
						+ " </p>\n");
				writer.write("        <p><strong>Module:</strong> " + ConnectToMainController.Process + "</p>\n");
				writer.write("        <p><strong>Framework:</strong> Biswajit AI Self-Healing</p>\n");
				writer.write("      </div>\n");

				writer.write("      <div class=\"card\">\n");
				writer.write("        <h2>\uD83D\uDC64 Team Info</h2>\n");
				writer.write("        <p><strong>SVP:</strong> NARESH YADAV</p>\n");
				writer.write("        <p><strong>Test Leads:</strong> Vikrant & Shankar & Shruti</p>\n");
				writer.write(
						"        <p><strong>TeamMates:</strong> Shantesh, Namrata, Shubham, Dinesh, Durvesh</p>\n");
				writer.write("        <p><strong>Tester:</strong> " + System.getProperty("user.name") + " </p>\n");
				writer.write("      </div>\n");

				writer.write("      <div class=\"card\">\n");
				writer.write("        <h2>\uD83D\uDDA5 System Info</h2>\n");
				writer.write("        <p><strong>OS:</strong> " + System.getProperty("os.name") + "</p>\n");
				writer.write(
						"        <p><strong>User:</strong> " + GetUserName(System.getProperty("user.name")) + "</p>\n");
				writer.write(
						"        <p><strong>Java Version:</strong> " + System.getProperty("java.version") + "</p>\n");
				writer.write("        <p><strong>Execution Type:</strong> Sanity</p>\n");
				writer.write("      </div>\n");

				writer.write("      <div class=\"card\">\n");
				if (ConnectToMainController.PlatForm.equalsIgnoreCase("Mobile")) {
					writer.write("        <h2>\uD83D\uDCF1 Device Info</h2>\n");
					writer.write("        <p><strong>Platform:</strong> Android</p>\n");
					writer.write("        <p><strong>Device:</strong> " + MobileConfiguration.DeviceName + "</p>\n");
					writer.write("        <p><strong>Version:</strong> " + MobileConfiguration.DevicePlatformVersion
							+ "</p>\n");
					writer.write("        <p><strong>App Version:</strong> " + Function.appVersion + "</p>\n");
				} else {
					writer.write("        <h2>\uD83C\uDF10 Browser Info</h2>\n");
					writer.write("        <p><strong>Browser:</strong> Chrome </p>\n");
					writer.write("        <p><strong>Version:</strong> 136 </p>\n");
					writer.write("        <p><strong>Platform:</strong> " + System.getProperty("os.name") + "</p>\n");
				}
				writer.write("      </div>\n");
				writer.write("    </div>\n");

				writer.write("    <div class=\"summary\">\n");
				writer.write(
						"      <h2 style=\"margin-top: 40px; font-size: 22px; color: #2c3e50;\">\uD83D\uDCCA Test Summary</h2>\n");

				writer.write("      <div class=\"grid\">\n");

				writer.write("        <div class=\"card link-card\" style=\"cursor:pointer;\">\n");

				String projectName = new File(System.getProperty("user.dir")).getName();
				String relativePath = Extent_ReportFile.replace(System.getProperty("user.dir"), "/" + projectName);

				writer.write("  <a href=\"" + relativePath + "\" target=\"_blank\">");
				writer.write("          <a href=" + relativePath + " target=_blank>");
				writer.write("            <h2>\uD83D\uDCC4 Report</h2>\n");
				writer.write(
						"            <p>Access the full Extent Report with comprehensive test execution details and validation summaries.</p>\n");
				writer.write("          </a>\n");
				writer.write("        </div>\n");

				writer.write("        <div class=\"card\">\n");
				writer.write("          <h2>⏱ Execution Time</h2>\n");
				writer.write(
						"          <p><strong>Start:</strong> " + Function.executionStartTimeProperFormat + "</p>\n");
				writer.write("          <p><strong>End:</strong> " + executionEndTimeProperFormat + "</p>\n");
				writer.write("          <p><strong>Duration:</strong> " + TotalExecutionTime + "</p>\n");
				writer.write("        </div>\n");

				if (ConnectToMainController.StepsScreenshot != null
						&& ConnectToMainController.StepsScreenshot.equalsIgnoreCase("Y")) {
					writer.write("        <div class=\"card link-card\" style=\"cursor:pointer;\">\n");
					writer.write("			<a href=" + eachStepExecution_ReportFile + " target=_blank>");
					writer.write("            <h2>\uD83D\uDCC4 StepsExecution</h2>\n");
					writer.write(
							"            <p>Access the full Extent Report with comprehensive test execution details and validation summaries.</p>\n");
					writer.write("          </a>\n");
					writer.write("        </div>\n");
				}

				writer.write("      </div>\n");

				// Test Case results summary box
				writer.write(
						"      <h2 style=\"margin-top: 40px; font-size: 22px; color: #2c3e50;\">\uD83C\uDFC6 TestCase Summary</h2>\n");
				writer.write("      <div class=\"status-box\">\n");
				writer.write("        <div class=\"status total\">\n");
				writer.write("          <h3>Total Scenarios</h3>\n");
				writer.write("          <span>" + ConnectToMainController.totalScenarios + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status pass\">\n");
				writer.write("          <h3>Passed Scenarios</h3>\n");
				writer.write("          <span>" + ConnectToMainController.passScenarios + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status fail\">\n");
				writer.write("          <h3>Failed Scenarios</h3>\n");
				writer.write("          <span>" + ConnectToMainController.failScenarios + "</span>\n");
				writer.write("        </div>\n");
				writer.write("      </div>\n");

				// TestCase Step results summary box
				writer.write(
						"      <h2 style=\"margin-top: 40px; font-size: 22px; color: #2c3e50;\">✅ TestCaseStep Summary</h2>\n");
				writer.write("      <div class=\"status-box\">\n");
				writer.write("        <div class=\"status total\">\n");
				writer.write("          <h3>Total Test Steps</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.totalTestStep + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status pass\">\n");
				writer.write("          <h3>Passed Steps</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.pass + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status fail\">\n");
				writer.write("          <h3>Failed Steps</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.fail + "</span>\n");
				writer.write("        </div>\n");
				writer.write("      </div>\n");

				// Validation results summary box
				writer.write(
						"      <h2 style=\"margin-top: 40px; font-size: 22px; color: #2c3e50;\">✅ Validation Summary</h2>\n");
				writer.write("      <div class=\"status-box\">\n");
				writer.write("        <div class=\"status total\">\n");
				writer.write("          <h3>Total Validations</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.totalValidations + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status pass\">\n");
				writer.write("          <h3>Passed Validations</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.passValidations + "</span>\n");
				writer.write("        </div>\n");
				writer.write("        <div class=\"status fail\">\n");
				writer.write("          <h3>Failed Validations</h3>\n");
				writer.write("          <span>" + ConnectToDataSheet.failedValidations + "</span>\n");
				writer.write("        </div>\n");
				writer.write("      </div>\n");

				writer.write("    </div>\n");

				writer.write("    <footer>\n");
				writer.write("      <p>© 2026 Biswajit Mahindra Finance QA Team</p>\n");
				writer.write("    </footer>\n");

				writer.write("  </div>\n");
				writer.write("</body>\n");
				writer.write("</html>\n");
			}
		} catch (Exception e) {
			logger.error("Error creating Web UI Report: {}", e.getMessage(), e);
		}
	}

	public void ExecutionTime() {
		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		long remainingMilliseconds = 0;
		long milliseconds = 0;

		executionEndTime = System.nanoTime();
		long executionTimeInMilliseconds = (executionEndTime - Function.executionStartTime) / 1_000_000;

		hours = executionTimeInMilliseconds / (60 * 60 * 1000);
		remainingMilliseconds = executionTimeInMilliseconds % (60 * 60 * 1000);
		minutes = remainingMilliseconds / (60 * 1000);
		remainingMilliseconds %= (60 * 1000);
		seconds = remainingMilliseconds / 1000;
		milliseconds = remainingMilliseconds % 1000;

		if (hours != 0) {
			TotalExecutionTime = hours + " hour " + minutes + " min " + seconds + " seconds " + milliseconds + " ms";
		} else if (minutes != 0) {
			TotalExecutionTime = minutes + " min " + seconds + " seconds " + milliseconds + " ms";
		} else if (seconds != 0) {
			TotalExecutionTime = seconds + " seconds " + milliseconds + " ms";
		} else
			TotalExecutionTime = milliseconds + " ms";

		executionEndTimeProperFormat = fetchSystemCurrentTime();

	}

	public static String fetchSystemCurrentTime() {
		LocalDateTime startTime = LocalDateTime.now();
		String formattedTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
		return formattedTime;
	}

	public static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "DataSheet"
			+ File.separator + "ApplicationID.xlsx";

	public static void CreateExcelSheetToStoreApplicationID() throws Exception {

		File file = new File(FILE_PATH);
		if (!file.exists()) {
			try (XSSFWorkbook workbook = new XSSFWorkbook()) {
				XSSFSheet sheet = workbook.createSheet("ApplicationID");
				XSSFRow headerRow = sheet.createRow(0);
				headerRow.createCell(0).setCellValue("Si_No");
				headerRow.createCell(1).setCellValue("RunStatus");
				headerRow.createCell(2).setCellValue("ApplicationID");
				// ✅ Two extra columns: ScenarioNo (auto-increment) + VerticalName
				headerRow.createCell(3).setCellValue("ScenarioNo");
				headerRow.createCell(4).setCellValue("VerticalName");

				try (FileOutputStream fos = new FileOutputStream(file)) {
					workbook.write(fos);
				}
			}
			System.out.println("Excel sheet created at: " + FILE_PATH);
		} else {
			System.out.println("Excel sheet already exists at: " + FILE_PATH);
		}
	}

	/**
	 * Writes one Application ID row to ApplicationID.xlsx.
	 *
	 * @param siNo          Auto-increment serial number (row count)
	 * @param applicationId The generated application ID
	 * @param scenarioNo    ScenarioNo from MAIN_CONTROLLER (e.g. SC_01)
	 * @param verticalName  VerticalName from MAIN_CONTROLLER (e.g. XPL / SALPL)
	 */
	public static void writeApplicationIDToExcel(String siNo, String applicationId,
	                                             String scenarioNo, String verticalName) {
		Fillo fillo = new Fillo();
		Connection connection = null;
		try {
			connection = fillo.getConnection(FILE_PATH);
			// ✅ INSERT includes ScenarioNo + VerticalName so each row is fully traceable
			String query = String.format(
					"INSERT INTO ApplicationID(Si_No, RunStatus, ApplicationID, ScenarioNo, VerticalName)"
							+ " VALUES('%s', 'Y', '%s', '%s', '%s')",
					siNo, applicationId,
					(scenarioNo != null ? scenarioNo : ""),
					(verticalName != null ? verticalName : ""));
			connection.executeUpdate(query);
			logger.info("Data written successfully: Si_No={}, ApplicationID={}, ScenarioNo={}, VerticalName={}",
					siNo, applicationId, scenarioNo, verticalName);
		} catch (Exception e) {
			logger.error("Error while writing to Excel: {}", e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	static ArrayList<String> rowValues;

	public static void readApplicationIDToExcel() {
		Fillo fillo = new Fillo();
		Connection connection = null;
		Recordset recordset = null;
		rowValues = new ArrayList<>();
		try {
			connection = fillo.getConnection(FILE_PATH);
			String query = "SELECT * FROM ApplicationID Where RunStatus='Y'";
			recordset = connection.executeQuery(query);

			while (recordset.next()) {
				rowValues.add(recordset.getField("ApplicationID"));
			}
			logger.info("Data Read successfully: {}", rowValues);
		} catch (Exception e) {
			logger.error("Error while reading ApplicationID from Excel: {}", e.getMessage(), e);
		} finally {
			if (connection != null) {
				connection.close();
			}
			if (recordset != null) {
				recordset.close();
			}
		}
	}

	public static String assignApplicationId(int rowNumber) {
		System.out.println(rowValues.get(rowNumber));
		return rowValues.get(rowNumber);
	}

	// handel every step execution time
	public static String calculateEveryStepExecutionTime(long stepStartTime) {

		long hours = 0;
		long minutes = 0;
		long seconds = 0;
		long milliseconds = 0;
		long remainingMilliseconds = 0;
		String TotalTime = null;

		long executionStepStartTime = stepStartTime;

		long executionEndTime = System.nanoTime();

		long executionTimeInMilliseconds = (executionEndTime - executionStepStartTime) / 1_000_000;

		hours = executionTimeInMilliseconds / (60 * 60 * 1000);
		remainingMilliseconds = executionTimeInMilliseconds % (60 * 60 * 1000);
		minutes = remainingMilliseconds / (60 * 1000);
		remainingMilliseconds %= (60 * 1000);
		seconds = remainingMilliseconds / 1000;
		milliseconds = remainingMilliseconds % 1000;

		if (hours != 0) {
			// System.out.println(hours + ":" + minutes + ":" + seconds );
			TotalTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else if (minutes != 0) {
			// System.out.println(minutes + ":" + seconds );
			// TotalTime = String.format("%02d:%02d", minutes, seconds);
			TotalTime = String.format("%02d:%02d.%03d", minutes, seconds, milliseconds) + " ms";
		} else {
			TotalTime = String.format("%02d.%03d", seconds, milliseconds) + " ms";
		}
		return TotalTime;
	}

	// ============================logger functionality
	// ======================================

	public static void configureLog4j() {
		// Get the current LoggerContext
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);

		// Get the current Log4j configuration
		Configuration config = loggerContext.getConfiguration();

		// Set the root logger level to DEBUG
		LoggerConfig rootLoggerConfig = config.getRootLogger();
		rootLoggerConfig.setLevel(org.apache.logging.log4j.Level.INFO); // HERE ALSO TO CHANGE THE DEBUG OR INFO OR ALL
		// OR ERROR OR WARN ETC MODE,

		// Remove any existing appenders (including console appender)
		rootLoggerConfig.getAppenders().forEach((name, appender) -> {
			rootLoggerConfig.removeAppender(name);
			appender.stop();
		});

		// Create a FileAppender with a dynamically generated file name
		String logFileName = generateLogFileName();

		FileAppender appender = FileAppender.newBuilder()
				.setName("File")
				.withFileName(logFileName)
				// .withAppend(false)//here write the append false me u can not append the new
				// run logs only overwrite if u want then make false to true
				.withAppend(true)
				.setLayout(PatternLayout.newBuilder()
						.withPattern("%d{yyyy-MM-dd HH:mm:ss} [%p] %C{1} - %msg%n")
						.build())
				.build();

		// Add the appender to the configuration
		appender.start();
		config.addAppender(appender);

		// Update the LoggerConfig to use the new appender
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.addAppender(appender, null, null);

		// Update the configuration
		config.getRootLogger().addAppender(appender, null, null);

		// Update the Log4j context
		loggerContext.updateLoggers();

		// Log a message indicating the new log file
		// logger.info("Logging to dynamically FILE created");
	}

	// generate logs file dynamic name
	private static String generateLogFileName() {
		String LogsFilePath = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "LOGs",
				"logsFileMethod");
		return LogsFilePath + File.separator + ConnectToMainController.Process + "_" + "Logs_" + time + ".log";
	}

	/**
	 * Creates ONE log file for the entire scenario lifetime.
	 * Named as: <ScenarioNo>_Logs_<date>.log
	 * Call this ONCE per scenario start (before the process loop), not per process.
	 *
	 * @param scenarioNo e.g. "SC_01"
	 */
	public static void configureLog4jForScenario(String scenarioNo) {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		Configuration config = loggerContext.getConfiguration();

		// Set log level
		LoggerConfig rootLoggerConfig = config.getRootLogger();
		rootLoggerConfig.setLevel(org.apache.logging.log4j.Level.INFO);

		// Stop and remove ALL existing appenders so the old file is closed properly
		rootLoggerConfig.getAppenders().forEach((name, appender) -> {
			rootLoggerConfig.removeAppender(name);
			appender.stop();
		});

		// Build file path: LOGs/<date>/<ScenarioNo>_Logs_<date>.log
		String logsDir = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName, "LOGs",
				"logsFileMethod");
		String safeScenarioNo = (scenarioNo != null && !scenarioNo.trim().isEmpty()) ? scenarioNo.trim() : "SCENARIO";
		String logFileName = logsDir + File.separator + safeScenarioNo + "_Logs_" + time + ".log";

		FileAppender appender = FileAppender.newBuilder()
				.setName("ScenarioFile_" + safeScenarioNo)
				.withFileName(logFileName)
				.withAppend(true) // append=true so multiple processes within the same scenario add to same file
				.setLayout(PatternLayout.newBuilder()
						.withPattern("%d{yyyy-MM-dd HH:mm:ss} [%p] %C{1} - %msg%n")
						.build())
				.build();

		appender.start();
		config.addAppender(appender);

		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		loggerConfig.addAppender(appender, null, null);
		config.getRootLogger().addAppender(appender, null, null);

		loggerContext.updateLoggers();

		logger.info("=== SCENARIO LOG STARTED: {} | File: {} ===", safeScenarioNo, logFileName);
	}

	// Take ScreenShot EveryStep
	public String takeScreenShotEveryStep(WebElement element) throws IOException {

		try {
			String screenShotFileName = ScenarioID + "_" + Module + "_" + TestCaseStepID + "_" + PageName + "_"
					+ ConnectToDataSheet.everyStepExecutionTime.replaceAll(":", "") + "_";
			File srcFile;
			try {
				srcFile = element.getScreenshotAs(OutputType.FILE);
			} catch (Exception e) {
				srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			}
			String destPath = getFormat("YYYY", "MMMM", "dd", ConnectToMainController.ApplicationName,
					"STEPs_SCREENSHOTs",
					"everyStepScreenShotMethod");
			String destFileEveryStepScreenShot = destPath + File.separator + screenShotFileName + ".png";
			FileUtils.copyFile(srcFile, new File(destFileEveryStepScreenShot));
			return destFileEveryStepScreenShot;
		} catch (Exception e) {
			logger.error(e + "\n");
			return null;
		}

	}

	// ════════════════════════════════════════════════════════════════════════════
	// AUDIT EXECUTION REPORT
	// Appends all scenario execution results from the current run to
	// ScenarioExecutionReport.xlsx in the project root directory.
	//
	// Columns: Si No | Date | ScenarioID | Application | Vertical | Scenario
	// | Status | Failed At | Time
	//
	// Rules:
	// - If the file does NOT exist → create it with a header row then add data.
	// - If the file DOES exist → open it, find the next empty row, append data.
	// - Multiple runs on the same day are appended as new rows (no overlap).
	// - "Failed At" is blank when status is PASS; populated when FAIL.
	// - Si No is a global auto-increment across ALL runs (row index in sheet).
	// ════════════════════════════════════════════════════════════════════════════
	public static void saveScenarioAuditToReport() {

		// Path: <project-root>/ScenarioExecutionReport.xlsx
		String reportFilePath = System.getProperty("user.dir") + File.separator + "ScenarioExecutionReport.xlsx";
		File reportFile = new File(reportFilePath);

		List<ConnectToMainController.ScenarioResult> results = ConnectToMainController.scenarioResults;

		if (results == null || results.isEmpty()) {
			logger.warn("saveScenarioAuditToReport: No scenario results to persist. Skipping.");
			System.out.println("ℹ️  No scenario results to write to ScenarioExecutionReport.xlsx.");
			return;
		}

		// Today's date string — e.g. "09/06/2026"
		String todayDate = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

		// Application name (safe default)
		String appName = (ConnectToMainController.ApplicationName != null
				&& !ConnectToMainController.ApplicationName.trim().isEmpty())
				? ConnectToMainController.ApplicationName.trim()
				: "AUTOMATION";

		org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = null;
		org.apache.poi.xssf.usermodel.XSSFSheet sheet = null;

		try {
			if (reportFile.exists()) {
				// ── File exists: load it and find next row ──
				try (java.io.FileInputStream fis = new java.io.FileInputStream(reportFile)) {
					workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis);
				}
				sheet = workbook.getSheet("ExecutionReport");
				if (sheet == null) {
					// Sheet not found in existing file — create it with header
					sheet = workbook.createSheet("ExecutionReport");
					createReportHeaderRow(sheet, workbook);
					logger.info("'ExecutionReport' sheet not found in existing file; created it with header.");
				}
				// If file exists and sheet exists, header is already present — just append
				// rows.
			} else {
				// ── File does NOT exist: create workbook + sheet + header ──
				workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
				sheet = workbook.createSheet("ExecutionReport");
				createReportHeaderRow(sheet, workbook);
				logger.info("ScenarioExecutionReport.xlsx does not exist; creating new file with header.");
			}

			// Determine the next row index (last used row + 1).
			// getLastRowNum() returns 0 if only header exists (row 0) or -1 if sheet empty.
			int nextRowIndex = sheet.getLastRowNum() + 1;
			// If sheet is completely empty (getLastRowNum() == -1 means 0 rows):
			if (nextRowIndex < 1) {
				nextRowIndex = 1; // Row 0 = header; data starts at row 1
			}

			// ── BUG FIX: Append ONLY the last (just-completed) scenario result ──
			// Previously the for-loop iterated ALL scenarioResults on every call, causing
			// Scenario 1 to be re-written after Scenario 2 completes, and so on.
			// Fixing this: write only results.get(results.size()-1) — the single entry
			// that was just added — so each scenario is recorded exactly once.
			ConnectToMainController.ScenarioResult r = results.get(results.size() - 1);

			// Si No = absolute row number in the sheet (1-based, auto-increments across
			// runs)
			int siNo = nextRowIndex;

			// Status display
			boolean isPass = "PASS".equalsIgnoreCase(r.verdict);
			String statusDisplay = isPass ? "PASS" : "FAIL";

			// Failed At: empty if PASS, process name if FAIL
			String failedAt = "";
			if (!isPass) {
				if (r.failedProcess != null && !r.failedProcess.trim().isEmpty()
						&& !"-".equals(r.failedProcess.trim())) {
					failedAt = r.failedProcess.trim();
				}
			}

			// Vertical name (safe)
			String vertical = (r.verticalName != null) ? r.verticalName.trim() : "";

			// Scenario description (safe)
			String scenarioDesc = (r.verticalScenario != null) ? r.verticalScenario.trim() : "";

			// Execution time
			String execTime = (r.executionTime != null) ? r.executionTime.trim() : "";

			// Create the data row
			org.apache.poi.xssf.usermodel.XSSFRow dataRow = sheet.createRow(nextRowIndex);
			dataRow.createCell(0).setCellValue(siNo); // Si No
			dataRow.createCell(1).setCellValue(todayDate); // Date
			dataRow.createCell(2).setCellValue(r.scenarioNo != null ? r.scenarioNo.trim() : ""); // ScenarioID
			dataRow.createCell(3).setCellValue(appName); // Application
			dataRow.createCell(4).setCellValue(vertical); // Vertical
			dataRow.createCell(5).setCellValue(scenarioDesc); // Scenario
			dataRow.createCell(6).setCellValue(statusDisplay); // Status
			dataRow.createCell(7).setCellValue(failedAt); // Failed At (empty if PASS)
			dataRow.createCell(8).setCellValue(execTime); // Time

			System.out.println("📝 Audit row " + siNo + " written: [" + r.scenarioNo + "] " + statusDisplay);
			logger.info("Audit row {} written: ScenarioID={}, Status={}, FailedAt={}", siNo, r.scenarioNo,
					statusDisplay, failedAt);

			// ── Write (save) the workbook back to disk ──
			try (java.io.FileOutputStream fos = new java.io.FileOutputStream(reportFile)) {
				workbook.write(fos);
			}

			System.out.println("✅ ScenarioExecutionReport.xlsx updated at: " + reportFilePath);
			logger.info("ScenarioExecutionReport.xlsx updated successfully at: {}", reportFilePath);

		} catch (Exception e) {
			logger.error("❌ Failed to write ScenarioExecutionReport.xlsx: {}", e.getMessage(), e);
			System.out.println("❌ Error writing ScenarioExecutionReport.xlsx: " + e.getMessage());
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	// ── Private helper: creates the header row with styled column names ──
	private static void createReportHeaderRow(org.apache.poi.xssf.usermodel.XSSFSheet sheet,
	                                          org.apache.poi.xssf.usermodel.XSSFWorkbook workbook) {

		org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);

		// Bold font for header
		org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
		org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		headerStyle.setFont(boldFont);
		headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

		String[] headers = { "Si No", "Date", "ScenarioID", "Application", "Vertical", "Scenario", "Status",
				"Failed At", "Time" };

		for (int i = 0; i < headers.length; i++) {
			org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle);
			sheet.autoSizeColumn(i);
		}
	}

}