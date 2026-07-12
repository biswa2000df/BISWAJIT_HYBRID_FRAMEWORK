package com.mahindra.utils;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.mahindra.config.*;
import com.mahindra.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MailSend {

    public static String Sr_No;
    public static String Process;
    public static String HOST;
    public static String Password;
    public static String MAIL_TO;
    public static String MAIL_CC;
    public static String SUBJECT;
    public static String BODY_MESSAGE;

    public static final Logger logger = LogManager.getLogger(MailSend.class.getName());

    // ══════════════════════════════════════════════════════════════
    // Per-process mail: called after every process completes.
    // Reads MAIL_SEND where RunStatus='Y' and Process=<current>.
    // ══════════════════════════════════════════════════════════════
    public static void mailSend() throws Exception {

        String mainControllerPath = ConnectToMainController.mainControllerFilePath;
        Fillo fillo = new Fillo();
        Connection conn = null;
        Recordset recordset = null;

        try {
            conn = fillo.getConnection(mainControllerPath);

            // ── 1. Check required columns exist in MAIL_SEND ──
            try {
                recordset = conn.executeQuery("SELECT * FROM MAIL_SEND");
                if (recordset == null) {
                    System.out.println("SORRY!!! MAIL_SEND sheet returned null recordset.");
                    return;
                }

                List<String> actualCols = recordset.getFieldNames();
                List<String> expectedCols = Arrays.asList(
                        "SR_NO", "RunStatus", "Process", "HOST", "Password",
                        "MAIL_TO", "MAIL_CC", "SUBJECT", "BODY_MESSAGE");

                List<String> missing = new ArrayList<>();
                for (String col : expectedCols) {
                    if (!actualCols.contains(col)) {
                        missing.add(col);
                    }
                }
                recordset.close();
                recordset = null;

                if (!missing.isEmpty()) {
                    System.out.println("SORRY!!! " + missing
                            + " columns are not present inside MainController file MailSend sheet");
                    logger.error("SORRY!!! {} columns are not present inside MailSend sheet", missing);
                    return; // Don't System.exit — let execution continue; just skip email
                }

            } catch (Exception e) {
                System.out.println("SORRY!!! MAIL_SEND sheet are not present...");
                logger.error("SORRY!!! MAIL_SEND sheet are not present: {}", e.getMessage());
                return;
            }

            // ── 2. Query rows matching current Process + RunStatus=Y ──
            String processQuery = "SELECT * FROM MAIL_SEND Where RunStatus='Y' and Process='"
                    + ConnectToMainController.Process + "'";
            try {
                recordset = conn.executeQuery(processQuery);
                if (recordset == null) {
                    System.out.println("SORRY!!! MAIL_SEND sheet are present BUT problem may be "
                            + "RunStatus not set 'Y' or ProcessName not set same as 'Project' name");
                    return;
                }

                // Collect all matching rows first, then close recordset
                List<String[]> rows = new ArrayList<>();
                while (recordset.next()) {
                    String[] r = new String[] {
                            safeField(recordset, "SR_NO"),
                            safeField(recordset, "RunStatus"),
                            safeField(recordset, "Process"),
                            safeField(recordset, "HOST"),
                            safeField(recordset, "Password"),
                            safeField(recordset, "MAIL_TO"),
                            safeField(recordset, "MAIL_CC"),
                            safeField(recordset, "SUBJECT"),
                            safeField(recordset, "BODY_MESSAGE")
                    };
                    rows.add(r);
                }
                recordset.close();
                recordset = null;

                for (String[] r : rows) {
                    Sr_No = r[0];
                    // r[1] = RunStatus (already filtered)
                    Process = r[2];
                    HOST = r[3];
                    Password = r[4];
                    MAIL_TO = r[5];
                    MAIL_CC = r[6];
                    SUBJECT = r[7];
                    BODY_MESSAGE = r[8];

                    boolean allFilled = notEmpty(Sr_No) && notEmpty(Process) && notEmpty(HOST)
                            && notEmpty(Password) && notEmpty(MAIL_TO) && notEmpty(MAIL_CC)
                            && notEmpty(SUBJECT) && notEmpty(BODY_MESSAGE);

                    if (allFilled) {
                        sendProcessEmail();
                    } else {
                        System.out.println("⚠️  Please fill all required columns in the MailSend Sheet for Process="
                                + Process);
                    }
                }

            } catch (Exception e) {
                System.out.println("SORRY!!! MAIL_SEND sheet are present BUT problem may be "
                        + "RunStatus not set 'Y' or ProcessName not set same as 'Project' name");
                logger.error("Error querying MAIL_SEND for process='{}': {}", ConnectToMainController.Process,
                        e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("SORRY!!! MAIL_SEND sheet are not present...");
            logger.error("SORRY!!! MAIL_SEND sheet are not present: {}", e.getMessage());
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

    // ══════════════════════════════════════════════════════════════
    // Sends the per-process email with step/validation summary table.
    // Static fields HOST, Password, MAIL_TO etc. must be set before call.
    // ══════════════════════════════════════════════════════════════
    public static void sendProcessEmail() {

        final String username = HOST;
        final String password = Password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Null-safe ApplicationName
        String appName = (ConnectToMainController.ApplicationName != null
                && !ConnectToMainController.ApplicationName.isEmpty())
                        ? ConnectToMainController.ApplicationName.toUpperCase()
                        : "AUTOMATION";

        String execType = (ConnectToMainController.ExecutionType != null
                && !ConnectToMainController.ExecutionType.isEmpty())
                        ? ConnectToMainController.ExecutionType.toUpperCase()
                        : "-";

        String devicePlatform = (MobileConfiguration.DevicePlatform != null
                && !MobileConfiguration.DevicePlatform.isEmpty())
                        ? MobileConfiguration.DevicePlatform.toUpperCase()
                        : "-";

        String htmlContent = "<html><body>"
                + "<h2>" + appName + " - Mobile Automation Test Report</h2>"
                + "<p>Hi Team, </p><p> " + BODY_MESSAGE + " </p>"
                + "<TABLE style=\"border-collapse: collapse; border: 1px solid black;"
                + " background-color:#E4E5E5;\" width=\"100%\">"
                + "<tr>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">Project</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#FFD700; color: black;\">Execution Type</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#00BFFF; color: white;\">Device Platform</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#8E44AD; color: white;\">Total Test Scenarios</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#1E90FF; color: white;\">Total Test Steps</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">Passed Steps</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#FF6347; color: white;\">Failed Steps</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#4682B4; color: white;\">Total Validations</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#32CD32; color: white;\">Passed Validations</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#FF6347; color: white;\">Failed Validations</th>"
                + "<th style=\"text-align:center; border: 1px solid black; background-color:#4CAF50; color: white;\">Execution Time</th>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + appName + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + execType + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + devicePlatform + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black; font-weight:bold; color:#8E44AD;\">"
                + ConnectToMainController.totalScenarios + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.totalTestStep
                + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.pass + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.fail + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.totalValidations
                + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.passValidations
                + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + ConnectToDataSheet.failedValidations
                + "</td>"
                + "<td style=\"text-align:center; border: 1px solid black;\">" + UtilsActivity.TotalExecutionTime
                + "</td>"
                + "</tr>"
                + "</TABLE><br><br></body></html>";

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(HOST));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(MAIL_TO));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(MAIL_CC));
            message.setSubject(SUBJECT);

            Multipart multipart = new MimeMultipart("mixed");
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);
            Transport.send(message);

            System.out.println("📧 Process email sent successfully to: " + MAIL_TO);
            logger.info("Process email sent to: {}", MAIL_TO);

        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("Failed to send process email: {}", e.getMessage());
        }
    }

    // Keep old method name as a delegate so existing call-sites don't break
    public static void MailSend_WithoutAnd_WithAttachment() {
        sendProcessEmail();
    }

    // ══════════════════════════════════════════════════════════════
    // Consolidated scenario-level summary email.
    // Called once after ALL scenarios complete.
    // Reads MAIL_SEND where SendScenarioSummary='Y'.
    // Column to add to MAIL_SEND sheet: SendScenarioSummary
    // ══════════════════════════════════════════════════════════════

    public static void sendScenarioSummaryEmail() {

        String mainControllerPath = ConnectToMainController.mainControllerFilePath;
        Fillo fillo = new Fillo();
        Connection conn = null;
        Recordset recordset = null;

        try {
            conn = fillo.getConnection(mainControllerPath);

            // 1. Verify SendScenarioSummary column exists
            try {
                recordset = conn.executeQuery("SELECT * FROM MAIL_SEND");
                if (recordset == null) {
                    System.out.println("ℹ️ MAIL_SEND sheet is empty. Skipping summary email.");
                    return;
                }

                List<String> cols = recordset.getFieldNames();
                recordset.close();
                recordset = null;

                if (!cols.contains("SendScenarioSummary")) {
                    System.out
                            .println("ℹ️ 'SendScenarioSummary' column not found in MAIL_SEND. Skipping summary email.");
                    return;
                }

            } catch (Exception e) {
                logger.warn("Could not check MAIL_SEND for SendScenarioSummary column: {}", e.getMessage());
                return;
            }

            // 2. Find SendScenarioSummary=Y row
            try {
                recordset = conn.executeQuery("SELECT * FROM MAIL_SEND Where SendScenarioSummary='Y'");

                if (recordset == null || !recordset.next()) {
                    System.out.println("ℹ️ No row with SendScenarioSummary='Y' in MAIL_SEND. Skipping summary email.");
                    if (recordset != null) {
                        recordset.close();
                        recordset = null;
                    }
                    return;
                }

                HOST = safeField(recordset, "HOST");
                Password = safeField(recordset, "Password");
                MAIL_TO = safeField(recordset, "MAIL_TO");
                MAIL_CC = safeField(recordset, "MAIL_CC");
                SUBJECT = safeField(recordset, "SUBJECT");
                BODY_MESSAGE = safeField(recordset, "BODY_MESSAGE");

                recordset.close();
                recordset = null;

            } catch (Exception e) {
                logger.warn("Error reading SendScenarioSummary row from MAIL_SEND: {}", e.getMessage());
                return;
            }

        } catch (Exception e) {
            logger.warn("Cannot open MainController for sendScenarioSummaryEmail: {}", e.getMessage());
            return;

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

        if (!notEmpty(HOST) || !notEmpty(MAIL_TO)) {
            System.out.println("⚠️ HOST or MAIL_TO empty in SendScenarioSummary row. Cannot send summary email.");
            return;
        }

        // 3. Prepare scenario data
        List<ConnectToMainController.ScenarioResult> results = ConnectToMainController.scenarioResults;

        long totalDurationMs = 0;
        StringBuilder tableRows = new StringBuilder();
        int rowIndex = 0;

        for (ConnectToMainController.ScenarioResult r : results) {

            totalDurationMs += r.durationMs;
            rowIndex++;

            boolean isPass = "PASS".equalsIgnoreCase(r.verdict);

            String rowBg = isPass ? "#f6fff8" : "#fff7f7";
            String leftBorder = isPass ? "#22c55e" : "#ef4444";
            String badgeBg = isPass ? "#16a34a" : "#dc2626";
            String badgeText = isPass ? "✓ PASS" : "✕ FAIL";

            String failedAt = isPass
                    ? ""
                    : (r.failedProcess != null && !r.failedProcess.trim().isEmpty()
                            && !"-".equals(r.failedProcess.trim())
                                    ? htmlEscape(r.failedProcess)
                                    : "");

            String failedColor = isPass ? "#64748b" : "#b91c1c";

            tableRows
                    .append("<tr bgcolor=\"").append(rowBg).append("\" style=\"background-color:")
                    .append(rowBg).append("; border-bottom:1px solid #e5e7eb;\">")

                    .append("<td style=\"padding:14px 12px; text-align:center; border-left:5px solid ").append(leftBorder)
                    .append("; font-size:13px; color:#475569; font-weight:bold; background-color:")
                    .append(rowBg).append(";\">")
                    .append(String.format("%02d", rowIndex)).append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; font-size:13px; color:#111827; font-weight:700; background-color:")
                    .append(rowBg).append(";\">")
                    .append(htmlEscape(r.scenarioNo)).append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; font-size:13px; color:#334155; background-color:")
                    .append(rowBg).append(";\">")
                    .append(htmlEscape(r.verticalName)).append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; font-size:13px; color:#334155; background-color:")
                    .append(rowBg).append(";\">")
                    .append(htmlEscape(r.verticalScenario)).append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; background-color:").append(rowBg).append(";\">")
                    .append("<span style=\"background-color:").append(badgeBg)
                    .append("; color:#ffffff; padding:7px 16px; border-radius:999px; font-size:12px; font-weight:800; display:inline-block; min-width:72px; text-align:center;\">")
                    .append(badgeText).append("</span>")
                    .append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; font-size:13px; color:").append(failedColor)
                    .append("; font-weight:700; background-color:").append(rowBg).append(";\">")
                    .append(failedAt).append("</td>")

                    .append("<td style=\"padding:14px 12px; text-align:center; font-size:13px; color:#111827; font-weight:600; background-color:")
                    .append(rowBg).append(";\">")
                    .append(htmlEscape(r.executionTime)).append("</td>")

                    .append("</tr>");
        }

        // 4. Format total duration
        long hours = totalDurationMs / (60 * 60 * 1000);
        long remainingMs = totalDurationMs % (60 * 60 * 1000);
        long minutes = remainingMs / (60 * 1000);
        remainingMs %= (60 * 1000);
        long seconds = remainingMs / 1000;

        String totalExecutionTimeString;
        if (hours > 0) {
            totalExecutionTimeString = hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            totalExecutionTimeString = minutes + "m " + seconds + "s";
        } else {
            totalExecutionTimeString = seconds + "s";
        }

        String appName = (ConnectToMainController.ApplicationName != null
                && !ConnectToMainController.ApplicationName.trim().isEmpty())
                        ? ConnectToMainController.ApplicationName.toUpperCase()
                        : "AUTOMATION";

        int total = ConnectToMainController.totalScenarios;
        int pass = ConnectToMainController.passScenarios;
        int fail = ConnectToMainController.failScenarios;

        int passRate = total > 0 ? (int) Math.round((pass * 100.0) / total) : 0;
        int failRate = total > 0 ? 100 - passRate : 0;

        String currentDate = new java.text.SimpleDateFormat("dd MMM yyyy").format(new java.util.Date());

        String safeBody = (BODY_MESSAGE != null && !BODY_MESSAGE.trim().isEmpty())
                ? htmlEscape(BODY_MESSAGE)
                : "Please find below the consolidated summary of today's automation execution run.";

        // Premium professional colors
        String primary = "#4f46e5"; // premium indigo
        String primaryDark = "#312e81";
        String bg = "#f3f4f6";
        String cardBg = "#ffffff";
        String passColor = "#16a34a";
        String failColor = "#dc2626";
        String warningColor = "#f59e0b";

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang=\"en\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<meta name=\"color-scheme\" content=\"light only\">"
                + "<meta name=\"supported-color-schemes\" content=\"light\">"
                + "<title>Automation Execution Report</title>"
                + "<style>"
                + "body, table, td, p, h1, h2, h3 { font-family: Segoe UI, Arial, sans-serif; }"
                + ":root { color-scheme: light only; supported-color-schemes: light; }"
                + "[data-ogsc] .force-light { background-color:#ffffff !important; color:#111827 !important; }"
                + "[data-ogsc] .kpi-total { background-color:#4f46e5 !important; color:#ffffff !important; }"
                + "[data-ogsc] .kpi-pass { background-color:#16a34a !important; color:#ffffff !important; }"
                + "[data-ogsc] .kpi-fail { background-color:#dc2626 !important; color:#ffffff !important; }"
                + "[data-ogsc] .kpi-time { background-color:#f59e0b !important; color:#ffffff !important; }"
                + "</style>"
                + "</head>"

                + "<body bgcolor=\"" + bg + "\" style=\"margin:0; padding:0; background-color:" + bg + ";\">"

                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"" + bg
                + "\" style=\"background-color:" + bg + "; padding:28px 0;\">"
                + "<tr><td align=\"center\">"

                + "<table width=\"900\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"" + cardBg + "\" "
                + "style=\"width:900px; max-width:900px; background-color:" + cardBg
                + "; border-radius:18px; overflow:hidden; "
                + "box-shadow:0 14px 35px rgba(17,24,39,0.12); border:1px solid #e5e7eb;\">"

                // HEADER
                + "<tr>"
                + "<td bgcolor=\"" + primaryDark + "\" style=\"background-color:" + primaryDark
                + "; padding:32px 42px; color:#ffffff;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                + "<tr>"
                + "<td>"
                + "<p style=\"margin:0 0 8px; font-size:12px; letter-spacing:2px; color:#c7d2fe; font-weight:800;\">QA AUTOMATION REPORT</p>"
                + "<h1 style=\"margin:0; font-size:27px; line-height:1.25; color:#ffffff; font-weight:800;\">🚀 Automation Execution Summary</h1>"
                + "<p style=\"margin:8px 0 0; font-size:14px; color:#ddd6fe;\">" + htmlEscape(appName)
                + " &nbsp;•&nbsp; Daily Regression Run</p>"
                + "</td>"
                + "<td align=\"right\" valign=\"top\">"
                + "<span style=\"background-color:" + primary
                + "; color:#ffffff; padding:9px 18px; border-radius:999px; display:inline-block; font-size:13px; font-weight:700;\">"
                + "📅 " + currentDate + "</span>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"

                // GREETING
                + "<tr>"
                + "<td bgcolor=\"#ffffff\" class=\"force-light\" style=\"padding:30px 42px 12px; background-color:#ffffff;\">"
                + "<p style=\"margin:0; color:#111827; font-size:15px; font-weight:700;\">Hi Everyone,</p>"
                + "<p style=\"margin:10px 0 0; color:#475569; font-size:14px; line-height:1.7;\">"
                + safeBody
                + "</p>"
                + "</td>"
                + "</tr>"

                // KPI CARDS
                + "<tr>"
                + "<td bgcolor=\"#ffffff\" style=\"padding:22px 34px 10px; background-color:#ffffff;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"8\" border=\"0\">"
                + "<tr>"

                + "<td width=\"25%\" bgcolor=\"" + primary + "\" class=\"kpi-total\" style=\"background-color:"
                + primary + "; padding:20px 14px; border-radius:14px; text-align:center;\">"
                + "<p style=\"margin:0; font-size:12px; color:#e0e7ff; font-weight:800; letter-spacing:1px;\">TOTAL</p>"
                + "<p style=\"margin:8px 0 0; color:#ffffff; font-size:30px; font-weight:900;\">" + total + "</p>"
                + "</td>"

                + "<td width=\"25%\" bgcolor=\"" + passColor + "\" class=\"kpi-pass\" style=\"background-color:"
                + passColor + "; padding:20px 14px; border-radius:14px; text-align:center;\">"
                + "<p style=\"margin:0; font-size:12px; color:#dcfce7; font-weight:800; letter-spacing:1px;\">PASSED</p>"
                + "<p style=\"margin:8px 0 0; color:#ffffff; font-size:30px; font-weight:900;\">" + pass + "</p>"
                + "</td>"

                + "<td width=\"25%\" bgcolor=\"" + failColor + "\" class=\"kpi-fail\" style=\"background-color:"
                + failColor + "; padding:20px 14px; border-radius:14px; text-align:center;\">"
                + "<p style=\"margin:0; font-size:12px; color:#fee2e2; font-weight:800; letter-spacing:1px;\">FAILED</p>"
                + "<p style=\"margin:8px 0 0; color:#ffffff; font-size:30px; font-weight:900;\">" + fail + "</p>"
                + "</td>"

                + "<td width=\"25%\" bgcolor=\"" + warningColor + "\" class=\"kpi-time\" style=\"background-color:"
                + warningColor + "; padding:20px 14px; border-radius:14px; text-align:center;\">"
                + "<p style=\"margin:0; font-size:12px; color:#fff7ed; font-weight:800; letter-spacing:1px;\">DURATION</p>"
                + "<p style=\"margin:8px 0 0; color:#ffffff; font-size:24px; font-weight:900;\">"
                + totalExecutionTimeString + "</p>"
                + "</td>"

                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"

                // PROGRESS BAR - FIXED GREEN + RED
                + "<tr>"
                + "<td bgcolor=\"#ffffff\" style=\"padding:18px 42px 28px; background-color:#ffffff;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                + "<tr>"
                + "<td align=\"left\" style=\"font-size:14px; color:#111827; font-weight:800;\">Overall Execution Health</td>"
                + "<td align=\"right\" style=\"font-size:13px; color:#475569; font-weight:700;\">"
                + passRate + "% Pass &nbsp;•&nbsp; " + failRate + "% Fail"
                + "</td>"
                + "</tr>"
                + "</table>"

                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"#e5e7eb\" "
                + "style=\"margin-top:10px; background-color:#e5e7eb; border-radius:999px; overflow:hidden; height:18px;\">"
                + "<tr>"

                + "<td bgcolor=\"" + passColor + "\" width=\"" + passRate + "%\" "
                + "style=\"background-color:" + passColor
                + "; height:18px; font-size:1px; line-height:1px;\">&nbsp;</td>"

                + "<td bgcolor=\"" + failColor + "\" width=\"" + failRate + "%\" "
                + "style=\"background-color:" + failColor
                + "; height:18px; font-size:1px; line-height:1px;\">&nbsp;</td>"

                + "</tr>"
                + "</table>"

                + "</td>"
                + "</tr>"

                // SCENARIO TABLE
                + "<tr>"
                + "<td bgcolor=\"#ffffff\" style=\"padding:0 42px 34px; background-color:#ffffff;\">"
                + "<h3 style=\"margin:0 0 15px; color:#111827; font-size:17px; font-weight:900;\">📋 Scenario Details</h3>"

                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"#ffffff\" "
                + "style=\"border-collapse:separate; border-spacing:0; background-color:#ffffff; border:1px solid #e5e7eb; border-radius:14px; overflow:hidden;\">"

                + "<thead>"
                + "<tr bgcolor=\"" + primaryDark + "\" style=\"background-color:" + primaryDark + ";\">"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">Sr No.</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">SCENARIO ID</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">VERTICAL</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">SCENARIO</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">STATUS</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">FAILED AT</th>"
                + "<th align=\"center\" style=\"padding:15px 12px; font-size:12px; color:#ffffff; background-color:"
                + primaryDark + "; letter-spacing:.5px;\">TIME</th>"
                + "</tr>"
                + "</thead>"

                + "<tbody>"
                + tableRows
                + "</tbody>"

                + "</table>"
                + "</td>"
                + "</tr>"

                // FOOTER
                + "<tr>"
                + "<td bgcolor=\"#f8fafc\" style=\"background-color:#f8fafc; padding:24px 42px; text-align:center; border-top:1px solid #e5e7eb;\">"
                + "<p style=\"margin:0; font-size:12px; color:#64748b; line-height:1.6;\">"
                + "⚙️ This is an auto-generated email from <b style=\"color:" + primary + ";\">" + htmlEscape(appName)
                + " QA Automation Framework Team</b>.<br>"
                + "For any queries related to this report, please contact Biswajit Sahoo."
                + "</p>"
                + "<p style=\"margin:10px 0 0; font-size:11px; color:#94a3b8;\">"
                + "© " + new java.text.SimpleDateFormat("yyyy").format(new java.util.Date())
                + " Mahindra Finance "
                + "</p>"
                + "</td>"
                + "</tr>"

                + "</table>"
                + "</td></tr>"
                + "</table>"

                + "</body>"
                + "</html>";

        // 5. Send via SMTP
        final String finalHost = HOST;
        final String finalPass = Password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalHost, finalPass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(HOST));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(MAIL_TO));

            if (notEmpty(MAIL_CC)) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(MAIL_CC));
            }

            message.setSubject(SUBJECT);

            Multipart multipart = new MimeMultipart("mixed");

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("📧 Scenario Summary Email sent successfully to: " + MAIL_TO);
            logger.info("Scenario Summary Email sent to: {}", MAIL_TO);

        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("Failed to send Scenario Summary Email: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Safely read a field from a Recordset.
     * Returns empty string if column doesn't exist or value is null.
     */
    private static String safeField(Recordset rs, String fieldName) {
        try {
            String value = rs.getField(fieldName);
            return (value != null) ? value.trim() : "";
        } catch (FilloException e) {
            logger.warn("Field '{}' not found in recordset: {}", fieldName, e.getMessage());
            return "";
        }
    }

    private static String htmlEscape(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Check if a string is not null and not empty.
     */
    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
