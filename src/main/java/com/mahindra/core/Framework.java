package com.mahindra.core;

import com.mahindra.utils.*;

public class Framework {

	public static String applicID;
	public static long automationStartTime = System.currentTimeMillis();

	static {
		LicenceClass.validateLicense();
	}

	public static void main(String[] Biswajit) throws Exception {

		applicID = System.getProperty("applicationId", "MF25013100001149");

		LicenceClass.Run();

		LicenceClass.printExecutionTime(automationStartTime);

		// ConnectToMainController.mainControllerSheet();
		// java -DapplicationId=MF25010800008000 -jar Biswajit_Framework.jar
		// java -DuserInput="$USER_INPUT" -jar *.jar //github action file required
		// java -DuserInput=DSR.xlsx -jar *.jar //normal terminal
		// java -DuserInput="DSR2.xlsx" -DemployeeID="A-22169" -jar *.jar

		// java -DuserInputMainController="MainController.xlsx" -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

		// java -DuserInputMainController="CustomController.xlsx" -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

		// java -DuserInputMainController="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/XPL_MainController.xlsx" -jar BISWAJIT_HYBRID_FRAMEWORK-B1.jar


		// java -DuserInputMainController="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/XPL_MainController.xlsx" -DuserInputDataSheetFolderPath="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/DataSheet" -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

//java -DuserInputMainController="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/XPL_MainController.xlsx" \-DuserInputDataSheetFolderPath="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/DataSheet" \-DuserInputConfigFilePath="/Users/mf_pnk-100005482/Documents/Biswajit/BISWAJIT-HYBRID-FRAMEWORK/config.yaml" \-Denv="qa" \-DSAPCODE="your_sap_code" \-DPASSWORD="your_password" \-jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar


//		java -DuserInputDataSheetFolderPath="/Users/mf_pnk-100005482/Documents/Biswajit/BISWAJIT-HYBRID-FRAMEWORK/DataSheet" -DSAPCODE="100005482" -DPASSWORD="Kanha" -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar


		//		java -DrunScenario=SC_03 -DrunVertical=BAU -DuserInputDataSheetFolderPath="/Users/mf_pnk-100005482/Documents/Biswajit/BISWAJIT-HYBRID-FRAMEWORK/DataSheet" -DSAPCODE="100005482" -DPASSWORD="Kanha" -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

		//final version of both main-controller and datasheet folder

		// java -DuserInputMainController="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/XPL_MainController.xlsx" \
		//     -DuserInputDataSheetFolderPath="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/DataSheet" \
		//     -DuserInputConfigFilePath="/Users/mf_pnk-100005482/Documents/SuperApp/SUPERAPP-HYBRID-FRAMEWORK/config.yaml" \
		//     -Denv="qa" \
		//     -DSAPCODE="100005482" -DPASSWORD="Kanha" \
		//     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

	}
}
