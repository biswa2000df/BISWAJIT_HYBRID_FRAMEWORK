package com.mahindra.utils;

import com.mahindra.core.ConnectToMainController;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LicenceClass {


	public static void Run() throws Exception {
		ConnectToMainController.mainControllerSheet();
	}


	private static final String LICENSE_EXPIRY = "10/09/2037";
	private static final String ALLOWED_MACHINE_ID = "BISWA-00155D";
	private static final String SUPPORT_EMAIL = "support@biswajitautomation.com";

	public static void validateLicense() {
		try {
			// 1. Validate license expiry
			if (isLicenseExpired()) {
				showLicenseExpired();
				System.exit(1);
			}

			// 2. Validate machine authorization
//			if (!isAuthorizedMachine()) {
//				showInvalidMachine();
//				System.exit(1);
//			}

			// 3. Check for expiry warning (non-blocking)
			checkLicenseExpiryWarning();

			// 4. Show welcome message
			showWelcomeMessage();

		} catch (Exception e) {
			showInvalidLicense();
			System.exit(1);
		}
	}

	private static boolean isLicenseExpired() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date expiryDate = dateFormat.parse(LICENSE_EXPIRY);
		return new Date().after(expiryDate);
	}

	private static boolean isAuthorizedMachine() throws Exception {
		return getSimpleMachineId().equals(ALLOWED_MACHINE_ID);
	}

	private static String getSimpleMachineId() throws Exception {
		String userName = System.getProperty("user.name");
		String macPrefix = getMacAddress().substring(0, 6);
		return userName.toUpperCase() + "-" + macPrefix;
	}

	private static String getMacAddress() throws Exception {
		java.net.NetworkInterface network = java.net.NetworkInterface.getByInetAddress(
				java.net.InetAddress.getLocalHost());
		byte[] mac = network.getHardwareAddress();
		StringBuilder sb = new StringBuilder();
		for (byte b : mac) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	private static void checkLicenseExpiryWarning() {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date expiryDate = dateFormat.parse(LICENSE_EXPIRY);
			Date currentDate = new Date();

			long diffInMillies = expiryDate.getTime() - currentDate.getTime();
			long daysRemaining = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			if (daysRemaining <= 30 && daysRemaining >= 0) {
				SwingUtilities.invokeLater(() -> {
					JDialog warningDialog = new JDialog();
					warningDialog.setTitle("⚠️ License Renewal Notice");
					warningDialog.setModal(false);
					warningDialog.setAlwaysOnTop(true);

					String messageText = createWarningMessage(daysRemaining);
					JLabel message = new JLabel(messageText);

					warningDialog.add(message);
					warningDialog.pack();
					warningDialog.setLocationRelativeTo(null);
					warningDialog.setVisible(true);

					new Timer(4000, e -> warningDialog.dispose()).start();
				});
			}
		} catch (Exception e) {
			// Silent handling
		}
	}

	private static String createWarningMessage(long daysRemaining) {
		String color = daysRemaining <= 7 ? "#D32F2F" : "#FFA000";
		String urgency = daysRemaining <= 7 ? "URGENT RENEWAL REQUIRED" : "Renewal Advisory";

		return "<html><div style='text-align: center; width: 300px; padding: 10px;'>" +
				"<h3 style='color: " + color + "; margin-top: 0;'>" + urgency + "</h3>" +
				"<p style='font-size: 14px;'>Your <b>Biswajit AI-POWERED Self-Healing Automation Tool</b> will expire in</p>" +
				"<h1 style='color: " + color + "; margin: 5px 0;'>" + daysRemaining + " DAYS</h1>" +
				"<p style='font-size: 13px;'>Expiry Date: <b>" + LICENSE_EXPIRY + "</b></p>" +
				"<hr style='border-color: #EEEEEE; margin: 10px 0;'>" +
				"<p style='font-size: 13px;'>Renew now to maintain uninterrupted automation workflows</p>" +
				"<p style='font-size: 12px; color: #1976D2; margin-top: 10px;'>" +
				"Contact: " + SUPPORT_EMAIL + "</p></div></html>";
	}

	private static void showWelcomeMessage() throws Exception {
		System.out.println("\n╔══════════════════════════════════════════════════════════╗");
		System.out.println(" ║     BISWAJIT AI-POWERED SELF-HEALING AUTOMATION TOOL   ║");
		System.out.println(" ║          • Scriptless • Adaptive • Intelligent         ║");
		System.out.println("╚══════════════════════════════════════════════════════════╝");
		System.out.println();
		System.out.println("             Welcome, " + System.getProperty("user.name").toUpperCase() + "!");
		System.out.println();
		System.out.println("    Licensed To:        " + System.getProperty("user.name").toUpperCase());
//		System.out.println("    Machine ID:         " + getSimpleMachineId());
		System.out.println("    License Valid Until: " + LICENSE_EXPIRY);
		System.out.println("    System Date:         " + new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss").format(new Date()));
		System.out.println();
		System.out.println("╔═══════════════════════════════════════════════════════════╗");
		System.out.println("║                  LICENSE RESTRICTIONS                     ║");
		System.out.println("╟───────────────────────────────────────────────────────────╢");
		System.out.println("║ • Licensed exclusively to the authorized user and machine ║");
		System.out.println("║ • Unauthorized use, distribution, or modification is      ║");
		System.out.println("║   strictly prohibited                                     ║");
		System.out.println("╟───────────────────────────────────────────────────────────╢");
		System.out.println("║ Need support? Contact: " + SUPPORT_EMAIL + "     ║");
		System.out.println("╚═══════════════════════════════════════════════════════════╝");
	}

	private static void showLicenseExpired() {
		showPopupDialog("License Expired",
				"Your automation license expired on " + LICENSE_EXPIRY + "\n\n" +
						"Please contact " + SUPPORT_EMAIL + " for renewal\n\n" +
						"Application will now exit", true);
	}

	private static void showInvalidMachine() throws Exception {
		showPopupDialog("Invalid License",
				"This machine is not authorized for use\n\n" +
						"Machine ID: " + getSimpleMachineId() + "\n\n" +
						"Contact " + SUPPORT_EMAIL + " for assistance", true);
	}

	private static void showInvalidLicense() {
		showPopupDialog("License Error",
				"Failed to validate license\n\n" +
						"Please contact " + SUPPORT_EMAIL + "\n\n" +
						"Application will now exit", true);
	}

	private static void showPopupDialog(String title, String message, boolean isError) {
		JOptionPane.showMessageDialog(null,
				message,
				title,
				isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
	}

	public static void printExecutionTime(long automationStartTime) {
		long automationExecutionEndTime = System.currentTimeMillis();
		long timeTakenMs = automationExecutionEndTime - automationStartTime;
		long seconds = (timeTakenMs / 1000) % 60;
		long minutes = (timeTakenMs / (1000 * 60)) % 60;
		long hours = (timeTakenMs / (1000 * 60 * 60)) % 24;

		String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		System.out.println();
		System.out.println("=======================================================");
		System.out.println("Execution Completed On :     " + currentDate);
		System.out.println(String.format("Total Execution Time   :     %02d Hours %02d Minutes %02d Seconds", hours, minutes, seconds));
		System.out.println("=======================================================");
	}
}