# 🤖 BISWAJIT HYBRID FRAMEWORK

### *Biswajit AI-Powered · Self-Healing · Scriptless Automation*

[![License](https://img.shields.io/badge/License-Commercial-purple.svg)](#-license--security)
[![Platform](https://img.shields.io/badge/Platform-Web%20%7C%20Mobile-blue.svg)](#-execution-modes)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](#-prerequisites)
[![BrowserStack](https://img.shields.io/badge/Cloud-BrowserStack-brightgreen.svg)](#-browserstack-remote-execution)
[![No Code](https://img.shields.io/badge/Testing-No--Code%20Excel--Driven-gold.svg)](#-what-is-this-framework)

> **Zero code. Zero panic. Just Excel → Run → Report.**
> Any fresher who can find a locator can automate Web & Mobile apps.

> **📌 Diagram Rendering:** This README uses [Mermaid](https://mermaid.js.org/) diagrams. They render automatically on **GitHub**, **GitLab**, **Azure DevOps**, and in VS Code with the [Mermaid Preview](https://marketplace.visualstudio.com/items?itemName=bierner.markdown-mermaid) extension. If diagrams show as code blocks, install a Mermaid-compatible markdown viewer.

---

## 📑 Table of Contents

| # | Section | Description |
|---|---------|-------------|
| 1 | [What Is This Framework?](#-what-is-this-framework) | High-level overview for everyone |
| 2 | [Why Use It?](#-why-use-it) | Before vs After comparison |
| 3 | [Key Features](#-key-features) | Feature summary table |
| 4 | [Architecture Overview](#-architecture-overview) | Visual package & class diagram |
| 5 | [Complete Execution Flow](#-complete-execution-flow) | 4-phase lifecycle flowcharts |
| 6 | [Class Inheritance Diagram](#-class-level-inheritance-diagram) | Who extends whom and why |
| 7 | [Excel Sheet Guide](#-excel-sheets--your-test-scripts) | How to write tests using only Excel |
| 8 | [Folder Structure](#-folder-structure) | Where every file lives |
| 9 | [How to Set Up](#-how-to-set-up) | Prerequisites and build |
| 10 | [How to Run](#-how-to-run) | All CLI commands with examples |
| 11 | [Configuration (config.yaml)](#-configuration-configyaml) | Environment-specific settings |
| 12 | [Execution Modes](#-execution-modes) | Local vs Remote (BrowserStack) |
| 13 | [Supported Actions](#-supported-actions) | 60+ actions for your Excel |
| 14 | [Error Handling & Recovery](#-error-handling--recovery) | Multi-level failure strategy |
| 15 | [Reporting System](#-reporting-system) | 5 auto-generated report types |
| 16 | [Email Notifications](#-email-notifications) | Automated email after execution |
| 17 | [License & Security](#-license--security) | License validation details |
| 18 | [FAQ for Freshers](#-faq-for-freshers) | Common questions answered |
| 19 | [Team & Credits](#-team--credits) | Who built this |
| 20 | [Technology Stack](#-technology-stack) | Libraries and versions |

---

## 🧠 What Is This Framework?

The **BISWAJIT Hybrid Framework** is a **no-code, Excel-driven** test automation platform that combines two powerful testing strategies:

| Strategy | What It Means |
|---|---|
| **Keyword-Driven** | You write action keywords like `CLICK`, `SENDKEYS` in Excel. The framework executes them. |
| **Data-Driven** | You supply multiple rows of test data. The framework runs the same steps for every data row automatically. |

### The Golden Rule
> 🔑 **You only need to fill Excel sheets. You never touch Java code.**

The entire engine is packaged as a **single JAR file**. Give it:
- `MainController.xlsx` — which app, which platform, which process to run
- `DataSheet/{Vertical}/{Scenario}/YourApp.xlsx` — what steps to execute and with what data

...and it does everything else: launches browser/app, finds elements, clicks/types/validates, generates HTML & Extent reports, and emails results.

---

## ✅ Why Use It?

| Without This Framework | With This Framework |
|---|---|
| Write 500 lines of Java per test | Fill 5 columns in Excel |
| Need senior dev to add a test | Any fresher can add steps |
| Separate web & mobile codebases | One JAR handles both |
| Manual result compilation | Auto HTML + Extent + Email reports |
| Breaks on every UI change | **Self-Healing** locator strategy |
| Run one dataset at a time | Loop over N data rows automatically |

---

## 🌟 Key Features

| Feature | Description |
|---------|-------------|
| 🧾 **100% Excel-Driven** | All test steps, test data, and configuration live in `.xlsx` files |
| 📱 **Mobile Testing** | Android & iOS apps via Appium (Local device or BrowserStack/LambdaTest cloud) |
| 🌐 **Web Testing** | Chrome, Firefox, Edge via Selenium WebDriver |
| 🔄 **Self-Healing Retry** | Configurable retry mechanism — if a button isn't found, it retries automatically |
| ❌ **Abort Condition** | If too many consecutive steps fail, skip that scenario and continue with the next |
| 📊 **Rich Reporting** | Extent HTML reports + Screenshot reports + Master UI dashboard |
| 📧 **Auto Email** | Sends pass/fail summary email with beautiful HTML tables after execution |
| 📝 **Audit Trail** | Every scenario execution logged to `ScenarioExecutionReport.xlsx` with date+time |
| 🔐 **License Protection** | Built-in license validation and machine-bound authorization |
| ⚙️ **Multi-Environment** | `config.yaml` supports `qa`, `dev`, `staging` environments with fallback |

---

## 🏗 Architecture Overview

```mermaid
graph TB
    subgraph "📦 com.mahindra.superapp"
        direction TB
        
        subgraph CORE["🔵 core — Orchestration Engine"]
            FW["Framework.java<br/><i>main() entry point</i>"]
            CMC["ConnectToMainController.java<br/><i>Scenario loop + grouping</i>"]
            CDS["ConnectToDataSheet.java<br/><i>Step-level execution</i>"]
            AID["Android_IOS_Driver.java<br/><i>Driver initialization</i>"]
        end

        subgraph CONFIG["🟡 config — Configuration"]
            CFG["ConfigManager.java<br/><i>config.yaml reader</i>"]
            MOB["MobileConfiguration.java<br/><i>MOBILE_CONFIGURATION sheet</i>"]
            TST["Testing.java<br/><i>Appium server control</i>"]
        end

        subgraph ACTIONS["🟢 actions — Test Execution"]
            LM["LocatorManager.java<br/><i>Element finding + retry</i>"]
            FN["Function.java<br/><i>60+ actions: CLICK, SENDKEYS...</i>"]
        end

        subgraph UTILS["🟠 utils — Reporting & Utilities"]
            UA["UtilsActivity.java<br/><i>Extent reports + screenshots</i>"]
            MS["MailSend.java<br/><i>Process + Scenario emails</i>"]
            LC["LicenceClass.java<br/><i>License validation</i>"]
            WS["WebScrolling.java<br/><i>Scroll utilities</i>"]
        end
    end

    subgraph EXCEL["📗 Excel Files (Your Test Scripts)"]
        MC["MainController.xlsx"]
        DS["DataSheet/*.xlsx"]
        CY["config.yaml"]
    end

    FW -->|"1. LicenceClass.Run()"| LC
    LC -->|"2. mainControllerSheet()"| CMC
    CMC -->|"3. Read scenarios"| MC
    CMC -->|"4. Read mobile config"| MOB
    CMC -->|"5. Init driver"| AID
    CMC -->|"6. For each process"| CDS
    CDS -->|"7. Read step rows"| DS
    CDS -->|"8. Find elements"| LM
    LM -->|"9. Execute actions"| FN
    FN -->|"10. Report results"| UA
    CMC -->|"11. Send email"| MS
    AID -->|"reads"| MOB
    MOB -->|"reads"| MC
    CFG -->|"reads"| CY

    style CORE fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    style CONFIG fill:#fff9c4,stroke:#f9a825,stroke-width:2px
    style ACTIONS fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    style UTILS fill:#fff3e0,stroke:#e65100,stroke-width:2px
    style EXCEL fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
```

---

## 🔄 Complete Execution Flow

This is the **full lifecycle** — from the moment you run the JAR file to the final email being sent.

### Phase 1: Startup & Validation

```mermaid
flowchart TD
    START(["▶ java -jar Framework.jar"]) --> A["Framework.main()"]
    A --> B["LicenceClass.validateLicense()"]
    B --> B1{"License<br/>expired?"}
    B1 -->|"YES"| B2["❌ Show popup + System.exit(1)<br/>APPLICATION STOPS"]
    B1 -->|"NO"| B3["✅ Show welcome banner"]
    B3 --> C["ConfigManager.loadConfig()"]
    C --> C1{"config.yaml<br/>found?"}
    C1 -->|"NO"| C2["❌ System.exit(0)<br/>APPLICATION STOPS"]
    C1 -->|"YES"| C3["Parse YAML + resolve environment"]
    C3 --> C4{"env section<br/>valid?"}
    C4 -->|"NO"| C5["❌ System.exit(0)<br/>APPLICATION STOPS"]
    C4 -->|"YES"| D["LicenceClass.Run()"]
    D --> E["ConnectToMainController.mainControllerSheet()"]
    
    style START fill:#4CAF50,color:#fff,stroke-width:0
    style B2 fill:#f44336,color:#fff,stroke-width:0
    style C2 fill:#f44336,color:#fff,stroke-width:0
    style C5 fill:#f44336,color:#fff,stroke-width:0
    style E fill:#2196F3,color:#fff,stroke-width:0
```

### Phase 2: MainController — Load & Group Scenarios

```mermaid
flowchart TD
    E["mainControllerSheet()"] --> F["Resolve MainController.xlsx path"]
    F --> F1{"File<br/>exists?"}
    F1 -->|"NO"| F2["❌ System.exit(0)<br/>CANNOT RUN WITHOUT IT"]
    F1 -->|"YES"| G["loadAndGroupMainControllerRows()"]
    G --> G1["Read MAIN_CONTROLLER sheet<br/>WHERE RunStatus='Y'"]
    G1 --> G2["Validate required columns exist"]
    G2 --> G3["Group rows by<br/>ScenarioNo|VerticalName"]
    G3 --> H["🔁 FOR EACH SCENARIO GROUP"]

    style F2 fill:#f44336,color:#fff
    style H fill:#FF9800,color:#fff,stroke-width:2px
```

### Phase 3: Scenario Loop — The Heart of the Framework

```mermaid
flowchart TD
    H["🔁 FOR EACH SCENARIO<br/>(e.g., SC_01|XPL)"] --> I["Reset counters:<br/>pass=0, fail=0<br/>scenarioHasFailed=false<br/>currentScenarioAborted=false"]
    I --> J["Extract Process rows<br/>for this scenario"]
    J --> K["🔁 FOR EACH PROCESS<br/>(e.g., START_APPLICATION,<br/>LOGIN, APPLY_LOAN)"]
    
    K --> K1{"Scenario<br/>already<br/>aborted?"}
    K1 -->|"YES ⚠️"| K2["⏭ SKIP remaining<br/>processes in this scenario"]
    K1 -->|"NO"| L["MainControlerDataSheet()"]
    
    L --> L1["Read DATASHEET sheet"]
    L1 --> L2["Match Process + ScenarioNo"]
    L2 --> L3["testDataSheetCheck()"]
    L3 --> L4{"DataSheet<br/>file exists?"}
    L4 -->|"NO"| L5["⚠️ Set scenarioAborted=true<br/>Skip remaining processes"]
    L4 -->|"YES"| M["Read MOBILE_CONFIGURATION"]
    M --> N["Android_IOS_Driver.Init()"]
    N --> N1{"Driver<br/>started<br/>OK?"}
    N1 -->|"NO 💥"| N2["throw RuntimeException<br/>→ Caught by process catch block<br/>→ scenarioAborted=true<br/>→ SKIP to next scenario"]
    N1 -->|"YES ✅"| O["ConnectToDataSheet.extractAllData()"]
    
    O --> P["Execute step rows<br/>(See Phase 4)"]
    
    K2 --> Q["SCENARIO VERDICT"]
    L5 --> Q
    N2 --> Q
    P --> Q
    
    Q --> Q1{"scenarioHasFailed<br/>OR<br/>scenarioAborted?"}
    Q1 -->|"YES"| Q2["🔴 FAIL<br/>failScenarios++"]
    Q1 -->|"NO"| Q3["🟢 PASS<br/>passScenarios++"]
    Q2 --> R["Store ScenarioResult"]
    Q3 --> R
    R --> S{"More<br/>scenarios?"}
    S -->|"YES"| H
    S -->|"NO"| T["📊 Save audit to ScenarioExecutionReport.xlsx"]
    T --> U["📧 Send scenario summary email"]
    U --> V["Print total execution time"]
    V --> DONE(["✅ EXECUTION COMPLETE"])

    style H fill:#FF9800,color:#fff,stroke-width:2px
    style K fill:#42A5F5,color:#fff
    style Q2 fill:#f44336,color:#fff
    style Q3 fill:#4CAF50,color:#fff
    style N2 fill:#FF5722,color:#fff
    style DONE fill:#4CAF50,color:#fff,stroke-width:0
```

### Phase 4: Step-Level Execution (Inside Each Process)

```mermaid
flowchart TD
    O["extractAllData(sheet2rowCount)"] --> P1["Create LocatorManager + Function + UtilsActivity<br/>(once per process)"]
    P1 --> P2["readAndValidateStepRows()<br/>Cache all Sheet1 rows"]
    P2 --> P3["🔁 FOR EACH Sheet2 DATA ROW<br/>(iteration = test data variation)"]
    P3 --> P4["Init Extent Report for this row"]
    P4 --> P5["🔁 FOR EACH STEP ROW<br/>(Sheet1: SI_No, Action, PropertyName...)"]
    
    P5 --> P6{"AbortCondition<br/>triggered?"}
    P6 -->|"YES ⚠️"| P7["⏭ SKIP remaining steps<br/>scenarioHasFailed=true"]
    P6 -->|"NO"| P8["LocatorManager.mapToLocator()"]
    
    P8 --> P9{"Locator<br/>specified?"}
    P9 -->|"YES"| P10["Build By locator<br/>(xpath/id/name/css...)"]
    P9 -->|"NO"| P11["Direct action<br/>(WAIT, QUIT, BROWSERURL)"]
    
    P10 --> P12["Wait for element<br/>(WebDriverWait)"]
    P12 --> P13{"Element<br/>found?"}
    P13 -->|"YES ✅"| P14["Function.ActionRDS()<br/>Execute the action"]
    P13 -->|"NO ❌"| P15{"Retry<br/>enabled?"}
    P15 -->|"YES"| P16["🔄 Retry up to RetryCount times<br/>Wait 2s between retries"]
    P15 -->|"NO"| P17["❌ FAIL step<br/>Take screenshot<br/>AbortCondition()"]
    P16 --> P18{"Retry<br/>succeeded?"}
    P18 -->|"YES"| P14
    P18 -->|"NO"| P17
    
    P14 --> P19{"Action has<br/>validation?"}
    P19 -->|"YES"| P20["CheckVisibility / IsEnable<br/>Compare expected vs actual"]
    P19 -->|"NO"| P21["✅ Step PASS<br/>pass++"]
    P20 --> P22{"Match?"}
    P22 -->|"YES"| P23["✅ Validation PASS<br/>passValidations++"]
    P22 -->|"NO"| P24["❌ Validation FAIL<br/>failedValidations++"]

    P11 --> P14
    P7 --> P25["Flush Extent Report"]
    P17 --> P25
    P21 --> P25
    P23 --> P25
    P24 --> P25
    P25 --> P26["Generate HTML Table + Master UI Report"]
    P26 --> P27["MailSend.mailSend()<br/>(per-process email)"]
    P27 --> P28["Calculate execution time"]

    style P5 fill:#42A5F5,color:#fff
    style P14 fill:#66BB6A,color:#fff
    style P17 fill:#ef5350,color:#fff
    style P16 fill:#FFA726,color:#fff
```

---

## 🧬 Class-Level Inheritance Diagram

The framework uses class inheritance to share state (driver, locators, counters) across all layers:

```mermaid
classDiagram
    class Android_IOS_Driver {
        +WebDriver driver
        +AppiumDriverLocalService service
        +InitialisationDriverRemote()
        +InitialisationDriverLocal()
        +Initialisation()
        +fnStopAppiumServer()
        +isDriverAlive()
    }

    class ConnectToDataSheet {
        +String Si_No, Module, Action...
        +int totalTestStep, pass, fail
        +boolean scenarioHasFailed
        +extractAllData(sheet2rowCount)
        +readAndValidateStepRows()
        +extractTestData()
    }

    class LocatorManager {
        +WebElement webElement
        +mapToLocator()
        +AbortCondition()
        +waitForPageReady()
        -getMaxRetryCount()
    }

    class Function {
        +ActionRDS()
        +60+ action cases
        +CHECKVISIBILITY, ISENABLE
    }

    class UtilsActivity {
        +extentReport()
        +takeScreenShot()
        +CreateHtmlTable()
        +webUIReport()
        +saveScenarioAuditToReport()
    }

    class ConnectToMainController {
        +mainControllerSheet()$
        +loadAndGroupMainControllerRows()$
        +MainControlerDataSheet()$
        +testDataSheetCheck()$
        +ScenarioResult
        +scenarioResults
    }

    Android_IOS_Driver <|-- ConnectToDataSheet : extends
    ConnectToDataSheet <|-- LocatorManager : extends
    LocatorManager <|-- Function : extends
    ConnectToDataSheet <|-- UtilsActivity : extends
    ConnectToMainController ..> ConnectToDataSheet : calls
    ConnectToMainController ..> Android_IOS_Driver : calls
    ConnectToMainController ..> MobileConfiguration : calls
    Function ..> UtilsActivity : uses
    LocatorManager ..> Function : delegates

    class MobileConfiguration {
        +mobileConfigurationSheet()$
        +DeviceName, DevicePlatform...
    }

    class ConfigManager {
        +loadConfig()$
        +resolveEnvironment()$
        +get(key)$
        +getEnv()$
    }

    class MailSend {
        +mailSend()$
        +sendScenarioSummaryEmail()$
        +sendProcessEmail()$
    }

    class LicenceClass {
        +validateLicense()$
        +Run()$
        +printExecutionTime()$
    }
```

**Inheritance chain explained:**
```
Android_IOS_Driver  ← holds the driver object
       ↑
ConnectToDataSheet  ← step-level execution loop
       ↑
  LocatorManager    ← element finding + retry + abort
       ↑
    Function        ← 60+ action implementations
```

Because of this chain, **every class in the framework has access to `driver`** — the core Selenium/Appium driver instance.

---

## 📗 Excel Sheets — Your Test Scripts

> **This is the most important section for testers.** You control everything through Excel. No code changes needed.

### 1. `MainController.xlsx` — The Master Control

This file contains **4 sheets**:

#### Sheet: `MAIN_CONTROLLER` — Define What Scenarios to Run

| Column | Required | Description | Example |
|--------|----------|-------------|---------|
| `Si_No` | ✅ | Serial number | 1, 2, 3 |
| `RunStatus` | ✅ | Y = run, N = skip | Y |
| `ScenarioNo` | ✅ | Scenario group ID | SC_01 |
| `VerticalName` | ✅ | Business vertical | XPL, SALPL, POCL |
| `Scenario` | | Description of what this scenario performs | "Till UW", "Full Journey" |
| `Process` | ✅ | Process/module name | LOGIN, APPLY_LOAN |
| `PlatForm` | ✅ | Mobile or Web | Mobile |
| `ExecutionType` | ✅ | Local or Remote | Local |
| `ApplicationName` | ✅ | App name for reports | SuperApp |
| `Abort` | ✅ | Enable abort condition? | Y |
| `StepsScreenshot` | ✅ | Capture every step screenshot? | Y |
| `SkipLine` | ✅ | BrowserStack multi-data skip | N |
| `Retry` | | Enable retry on element failure? | Y |
| `RetryCount` | | Number of retries | 3 |

#### Sheet: `DATASHEET` — Map Processes to DataSheet Files

| Column | Required | Description | Example |
|--------|----------|-------------|---------|
| `Si_No` | ✅ | Serial number | 1 |
| `RunStatus` | ✅ | Y = active | Y |
| `Process` | ✅ | Must match MAIN_CONTROLLER Process | LOGIN |
| `TestDataSheet` | ✅ | File name inside DataSheet/ folder | DSR.xlsx |
| `ImplicityWait` | ✅ | Implicit wait seconds | 10 |
| `ExplicityWait` | ✅ | Explicit wait seconds | 30 |
| `RepeatedFailed` | ✅ | Abort threshold for consecutive failures | 3 |
| `RetryCount` | | Retry count for element finding | 3 |
| `ScenarioNo` | | *(Optional)* Scenario filter | SC_01 |
| `VerticalName` | | *(Optional)* Vertical filter | XPL |

#### Sheet: `MOBILE_CONFIGURATION` — Device/App Settings

| Column | Description | Example |
|--------|-------------|---------|
| `Process` | Must match MAIN_CONTROLLER Process | LOGIN |
| `RunStatus` | Y to use | Y |
| `App_PackageName` | Android package name | com.mahindra.superapp |
| `App_PackageActivityName` | Launch activity | .ui.SplashActivity |
| `DeviceName` | Device name | Samsung Galaxy S21 |
| `DevicePlatform` | Android or iOS | Android |
| `DevicePlatformVersion` | OS version | 14 |
| `AppiumPort` | Appium server port | 4723 |
| `TestingPlatform` | BrowserStack / LambdaTest / Local | Local |
| `UserName` | Cloud username (if remote) | user@example.com |
| `AccessKey` | Cloud access key (if remote) | abc123key |

#### Sheet: `MAIL_SEND` — Email Configuration

| Column | Description | Example |
|--------|-------------|---------|
| `RunStatus` | Y to send | Y |
| `Process` | Process name | LOGIN |
| `HOST` | Gmail address | qa@mahindra.com |
| `Password` | App password | xxxx xxxx xxxx |
| `MAIL_TO` | Recipients | a@m.com,b@m.com |
| `MAIL_CC` | CC recipients | lead@m.com |
| `SUBJECT` | Email subject | Automation Report |
| `BODY_MESSAGE` | Email body | Test execution completed |
| `SendScenarioSummary` | Y = send scenario summary | Y |

---

### 2. DataSheet Files — Your Test Steps

Located at: `DataSheet/{VerticalName}/{ScenarioNo}/{TestDataSheet}.xlsx`

Example path: `DataSheet/XPL/SC_01/DSR.xlsx`

#### Sheet1 — Test Steps (What to Do)

| Column | Description | Example |
|--------|-------------|---------|
| `Si_No` | Step number | 1 |
| `Module` | Module name (must match Process) | LOGIN |
| `RunStatus` | Y = run | Y |
| `PageName` | Page name | LoginPage |
| `ScenarioID` | Scenario | SC_Login |
| `TestCaseID` | Test case | TC_001 |
| `TestCaseStepID` | Step ID | TS_001 |
| `TestCaseDescription` | Description | Enter SAP Code |
| `TestCaseStepDescription` | Step description | Type SAP code into input |
| `PropertyName` | Locator type | xpath, id, name, css, accessibilityid |
| `PropertyValue` | Locator value | //input[@id='sapcode'] |
| `DataField` | Data column name from Sheet2 | SAPCode |
| `Action` | Action to perform | SENDKEYS, CLICK, CHECKVISIBILITY |
| `ActionType` | Additional action config | *(varies)* |

#### Sheet2 — Test Data (What Data to Use)

| Column | Description | Example |
|--------|-------------|---------|
| `Si_No` | Row number | 1 |
| `RunStatus` | Y = use this data row | Y |
| `ApplicationName` | Must match Module/Process | LOGIN |
| Any custom columns | Your test data | SAPCode=100005482, Password=MyPass |

> 💡 **How It Works:** For each data row in Sheet2 (where RunStatus=Y), ALL steps from Sheet1 are executed. This is how you run the same test with different data sets!

---

## 📁 Folder Structure

```
BISWAJIT-HYBRID-FRAMEWORK/
│
├── 📄 pom.xml                          # Maven build config
├── 📄 config.yaml                      # Environment config (qa/dev/staging)
├── 📄 MainController.xlsx              # 🎯 Master control file
├── 📄 ScenarioExecutionReport.xlsx     # 📊 Auto-generated audit trail
│
├── 📂 src/main/java/com/mahindra/superapp/
│   ├── 📂 core/                        # 🔵 Orchestration engine
│   │   ├── Framework.java              #    Entry point (main method)
│   │   ├── ConnectToMainController.java#    Scenario loop + grouping
│   │   ├── ConnectToDataSheet.java     #    Step-level execution
│   │   └── Android_IOS_Driver.java     #    Driver init (Appium/Selenium)
│   │
│   ├── 📂 config/                      # 🟡 Configuration readers
│   │   ├── ConfigManager.java          #    config.yaml reader
│   │   ├── MobileConfiguration.java    #    MOBILE_CONFIGURATION sheet reader
│   │   └── Testing.java                #    Appium server start/stop
│   │
│   ├── 📂 actions/                     # 🟢 Test action execution
│   │   ├── LocatorManager.java         #    Element finding + retry + abort
│   │   └── Function.java              #    60+ actions (CLICK, SENDKEYS...)
│   │
│   └── 📂 utils/                       # 🟠 Reporting & utilities
│       ├── UtilsActivity.java          #    Extent reports + screenshots + audit
│       ├── MailSend.java               #    Email sending (process + scenario)
│       ├── LicenceClass.java           #    License validation
│       └── WebScrolling.java           #    Web page scrolling utilities
│
├── 📂 DataSheet/                       # 📗 Test data files
│   ├── 📂 XPL/SC_01/DSR.xlsx          #    DataSheet for XPL scenario 1
│   ├── 📂 SALPL/SC_01/DSR.xlsx        #    DataSheet for SALPL scenario 1
│   ├── 📂 POCL/SC_01/DSR.xlsx         #    DataSheet for POCL scenario 1
│   └── 📄 ApplicationID.xlsx          #    Generated application IDs
│
├── 📂 RESULT/                          # 📊 Auto-generated results
│   └── 📂 2026/June/17/SuperApp/
│       ├── 📂 REPORTs/                 #    Extent HTML reports
│       ├── 📂 SCREENSHOTs/             #    Failure screenshots
│       ├── 📂 HtmlTables/              #    Summary HTML tables
│       ├── 📂 MASTERREPORTs/           #    Master UI dashboard
│       └── 📂 STEPs_REPORTs/           #    Per-step screenshot reports
│
└── 📂 target/                          # Maven build output
    ├── BISWAJIT_HYBRID_FRAMEWORK-B1.jar
    └── libs/                           # All dependency JARs
```

---

## ⚡ How to Set Up

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java JDK | 17+ | Runtime & compilation |
| Maven | 3.8+ | Build tool |
| Appium Server | 2.x | Mobile testing only (`npm install -g appium`) |
| Android SDK | Latest | Android device connection |
| Node.js | 18+ | Required for Appium |
| Chrome/Firefox | Latest | Web testing |
| Appium Inspector | Latest | For finding mobile locators |

### Build the Project

```bash
# Navigate to project root
cd BISWAJIT-HYBRID-FRAMEWORK

# Clean + compile + package into JAR
mvn clean package -DskipTests
```

This creates:
- `target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar` — The executable JAR
- `target/libs/` — All dependency JARs

---

## 🚀 How to Run

### Method 1: Default (uses `MainController.xlsx` in project root)

```bash
java -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

### Method 2: Custom MainController File

```bash
java -DuserInputMainController="XPL_MainController.xlsx" \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

### Method 3: Custom MainController + Custom DataSheet Folder

```bash
java -DuserInputMainController="XPL_MainController.xlsx" \
     -DuserInputDataSheetFolderPath="DataSheet" \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

### Method 4: Full Configuration (Recommended for CI/CD)

```bash
java -DuserInputMainController="/absolute/path/to/MainController.xlsx" \
     -DuserInputDataSheetFolderPath="/absolute/path/to/DataSheet" \
     -DuserInputConfigFilePath="/absolute/path/to/config.yaml" \
     -Denv="qa" \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

### Method 5: With Application ID

```bash
java -DapplicationId=MF25013100001149 \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

### All Available JVM Properties (`-D` flags)

| Property | Default | Description |
|----------|---------|-------------|
| `-DuserInputMainController` | `MainController.xlsx` | Path to MainController file |
| `-DuserInputDataSheetFolderPath` | `DataSheet/` | Path to DataSheet folder |
| `-DuserInputConfigFilePath` | `config.yaml` | Path to config YAML file |
| `-Denv` | `defaultEnv` from YAML | Environment: `qa`, `dev`, etc. |
| `-DapplicationId` | `MF25013100001149` | Application ID to use |
| `-DuserInput` | *(none)* | DataSheet filename override |
| `-DrunScenario` | *(none)* | **[NEW]** Run only this ScenarioNo (e.g. `SC_01`). Must be used **together** with `-DrunVertical`. |
| `-DrunVertical` | *(none)* | **[NEW]** Run only this VerticalName (e.g. `XPL`). Must be used **together** with `-DrunScenario`. |

> 💡 **CLI Filter Behaviour:**
> - **Both provided** → The `MAIN_CONTROLLER` query becomes `WHERE RunStatus='Y' AND ScenarioNo='...' AND VerticalName='...'`. Only the matching scenario runs.
> - **Only one provided** → Warning printed, filter is ignored, all `RunStatus='Y'` scenarios run (normal mode).
> - **Neither provided** → Exactly the original behaviour — all `RunStatus='Y'` scenarios run.

### Method 6: Run a Specific Scenario + Vertical (New)

```bash
# Run ONLY SC_01 for the XPL vertical (RunStatus must be Y for it)
java -DrunScenario=SC_01 \
     -DrunVertical=XPL \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar

# Combined with other options
java -DrunScenario=SC_02 \
     -DrunVertical=BAU \
     -DuserInputMainController="MainController.xlsx" \
     -Denv="qa" \
     -jar target/BISWAJIT_HYBRID_FRAMEWORK-B1.jar
```

> Even if SC_01 BAU, SC_03 XPL, SC_04 SALPL all have `RunStatus=Y`, **only** the matching scenario executes when the filter is active.

---

## ⚙ Configuration (config.yaml)

```yaml
defaultEnv: qa          # Used if -Denv is not passed

common:                 # Shared across all environments
  sapcode: 100005482
  password: MyPassword
  mPin: "0008"

qa:                     # QA environment
  browser: chrome
  urls:
    base: https://web-sit.aws.mmfss.net/user
    login: /login
  users:
    admin:
      username: admin
      password: 1234

dev:                    # Dev environment
  browser: firefox
  urls:
    base: https://dev.app.com
    login: /login
```

**Access in DataSheet:** Use `ConfigManager.get("urls.base")` — supports dot-notation for nested keys. Checks environment section first, then falls back to `common`.

---

## 🚀 Execution Modes

| Mode | PlatForm | ExecutionType | What Happens |
|------|----------|---------------|--------------|
| **Web Local** | Web | local | Launches Chrome/Firefox/Edge on your machine |
| **Web Remote** | Web | remote | Connects to BrowserStack grid (any OS + browser) |
| **Mobile Local** | Mobile | local | Appium + USB device/Emulator at localhost:4723 |
| **Mobile Remote** | Mobile | remote | BrowserStack real device cloud |

### Local Mobile Setup

1. Install Appium Server: `npm install -g appium`
2. Install Android driver: `appium driver install uiautomator2`
3. Start Appium: `appium --port 4723`
4. Connect your device via USB (Developer Mode ON)

### BrowserStack Remote Setup

In `MOBILE_CONFIGURATION` sheet:
```
UserName       = your_browserstack_username
AccessKey      = your_browserstack_access_key
TestingPlatform = BrowserStack
```

### BrowserStack with Multiple Data Rows

When `SkipLine = Y` in MAIN_CONTROLLER and you have multiple Sheet2 rows:
- Row 1 → full execution from start to finish (driver started fresh)
- Row 2+ → execution skips to the `RowSkipForRemote` marker step (reuses existing session)

This prevents re-launching the app for every data row on BrowserStack (saves time & cost).

---

## 🎬 Supported Actions

These are the actions you can use in the `Action` column of your DataSheet Sheet1:

### App & Browser Lifecycle

| Action | Description |
|--------|-------------|
| `START_APPLICATION` | Start mobile app (reads MOBILE_CONFIGURATION, inits Appium driver) |
| `INSTALLANDSTARTAPPLICATION` | Install APK and start app |
| `STARTBROWSER` | Launch web browser (Chrome/Firefox/Edge) |
| `BROWSERURL` | Navigate to a URL |
| `NEWWINDOWBROWSWRTAB` | Open new browser tab |
| `NAVIGATEBACK` | Go back one page |
| `PAGEREFRESH` | Refresh current page |
| `QUIT` | Close driver/browser |
| `STARTDRIVER` | Start a new driver session without app |
| `OPENAPP_USINGONLYAPPPACKAGE` | Open app by package name |
| `TERMINATEAPP_USINGONLYAPPPACKAGE` | Force-stop an app |

### Element Interactions

| Action | Description |
|--------|-------------|
| `CLICK` | Click an element |
| `JAVASCRIPTCLICK` | Click via JavaScript (for hidden elements) |
| `CHECKANDCLICK` | Click only if element exists |
| `SENDKEYS` | Type text into an element |
| `CLICKCLEARSENDKEYS` | Click, clear field, then type |
| `SENDKEYSANDENTERKEY` | Type text and press Enter |
| `CLEAR` | Clear input field |
| `JS_CLEARSENDKEYS` | Clear and type using JavaScript |
| `SELECTUPLOADFILE` | Upload a file |
| `GETATTRIBUTEVALUE` | Read an attribute from element |

### Validations

| Action | Description |
|--------|-------------|
| `CHECKVISIBILITY` | Verify element is visible (PASS/FAIL with screenshot) |
| `ISENABLE` | Verify element is enabled |
| `MOBILEGETTEXT` | Get text from mobile element |
| `WEBGETTEXT` | Get text from web element |

### Mobile-Specific

| Action | Description |
|--------|-------------|
| `MPIN` | Enter MPIN digits |
| `GOBACK` / `HIDEKEYBOARD` | Press back / hide keyboard |
| `HIDEKEYBOARDUSINGENTERKEY` | Hide keyboard via Enter key |
| `HIDEKEYBOARDIFITOPEN` | Conditionally hide keyboard |
| `KEYBOARDSENDKEYS` | Send keys via Android KeyEvent |
| `CAMERAIMAGEINJECTION` | Inject camera image (BrowserStack) |
| `PUSHFILETOBROWSERSTACKDEVICE` | Push file to cloud device |
| `SCROLLDOWN` / `SCROLLUP` | Scroll on mobile |
| `SCROLLDOWNTILLELEMENTFOUND` | Scroll until element appears |
| `SWIPELEFTTORIGHT` / `SWIPERIGHTOLEFT` | Swipe gestures |

### Data & Utility

| Action | Description |
|--------|-------------|
| `SENDKEYSUSING_CONFIGVALUE` | Type value from config.yaml |
| `GENERATERANDOMNUMBER` | Generate random number |
| `STOREAPPLICATIONID` | Save application ID to Excel |
| `UPDATEAPPLICATIONID` | Update stored application ID |
| `WAIT` | Static wait (seconds in DataField) |
| `WAIT_FOR_NEXTELEMENT` | Wait for next element to appear |
| `MONITORING_PROPERTIES` | Log current monitoring state |
| `GETPAGESOURCE` | Dump page source to log |

### Web-Specific

| Action | Description |
|--------|-------------|
| `SELECTDROPDOWN` | Select dropdown by visible text |
| `WEBSCROLLDOWN` | Scroll web page down |
| `IFRAME` | Switch to iframe |
| `DEFAULTCONTENT` | Switch back from iframe |
| `SWITCHWINDOW` | Switch browser window/tab |
| `ALERTACCEPT` / `ALERTDISMISS` | Handle alerts |
| `MOUSEOVER` | Hover over element |
| `DOUBLECLICK` | Double-click element |
| `RIGHTCLICK` | Right-click element |
| `DRAGANDDROP` | Drag element to target |

### Locator Strategies (PropertyName column)

| PropertyName | Example PropertyValue | Use When |
|---|---|---|
| `xpath` | `//android.widget.Button[@text='Login']` | Most flexible — mobile & web |
| `id` | `com.app:id/loginBtn` | Best for Android resource IDs |
| `accessibilityid` | `LoginButton` | iOS & Android accessibility |
| `css` | `.login-btn` | Web only |
| `name` | `username` | Web forms |
| `classname` | `android.widget.EditText` | Android class-based |
| `uiautomator` | `new UiSelector().text("Login")` | Advanced Android |
| `linktext` | `Click here` | Web anchor links |
| `partiallinktext` | `Click` | Web partial link match |
| `tagname` | `button` | Web HTML tag |

---

## 🛡 Error Handling & Recovery

The framework has a **multi-level error handling** strategy to ensure maximum test coverage even when failures occur:

```mermaid
flowchart TD
    subgraph LEVEL1["Level 1: Step-Level Recovery"]
        S1["Step fails<br/>(element not found)"]
        S1 --> S2{"Retry<br/>enabled?"}
        S2 -->|"YES"| S3["🔄 Retry up to<br/>RetryCount times<br/>(2s delay between)"]
        S3 --> S4{"Success?"}
        S4 -->|"YES ✅"| S5["Continue normally"]
        S4 -->|"NO ❌"| S6["Log error + screenshot<br/>fail++ → AbortCondition()"]
        S2 -->|"NO"| S6
    end

    subgraph LEVEL2["Level 2: Abort Condition (Scenario-Level)"]
        S6 --> A1{"Abort=Y in<br/>MainController?"}
        A1 -->|"NO"| A2["Continue to<br/>next step"]
        A1 -->|"YES"| A3{"consecutiveFails ≥<br/>RepeatedFailed?"}
        A3 -->|"NO"| A2
        A3 -->|"YES ⚠️"| A4["scenarioAborted=true<br/>Skip remaining steps<br/>Skip remaining processes"]
    end

    subgraph LEVEL3["Level 3: Scenario-Level Recovery"]
        A4 --> B1["Scenario marked FAIL 🔴"]
        B1 --> B2["📝 Record in ScenarioResult"]
        B2 --> B3["▶️ NEXT SCENARIO<br/>continues normally"]
    end

    subgraph LEVEL4["Level 4: Driver Init Failure"]
        D1["Driver init fails<br/>(Appium won't start)"]
        D1 --> D2["throw RuntimeException<br/>(NOT System.exit!)"]
        D2 --> D3["Caught by process catch block"]
        D3 --> D4["scenarioAborted=true<br/>scenarioHasFailed=true"]
        D4 --> B1
    end

    subgraph LEVEL5["Level 5: Global Config Errors"]
        G1["MainController.xlsx not found<br/>DataSheet/ folder missing<br/>config.yaml invalid"]
        G1 --> G2["❌ System.exit(0)<br/>Nothing can run!"]
    end

    style LEVEL1 fill:#e8f5e9,stroke:#2e7d32
    style LEVEL2 fill:#fff3e0,stroke:#e65100
    style LEVEL3 fill:#e3f2fd,stroke:#1565c0
    style LEVEL4 fill:#fce4ec,stroke:#c62828
    style LEVEL5 fill:#f44336,color:#fff,stroke:#b71c1c
```

### Error Classification

| Error Type | System.exit? | Behavior |
|------------|:------------:|----------|
| MainController.xlsx not found | ✅ YES | Nothing can run — fatal |
| Required columns missing in MAIN_CONTROLLER | ✅ YES | Nothing can run — fatal |
| Base DataSheet/ folder missing | ✅ YES | Nothing can run — fatal |
| config.yaml not found/empty | ✅ YES | Nothing can run — fatal |
| License expired | ✅ YES | Not authorized — fatal |
| Driver init failed (Appium/Selenium) | ❌ NO | Skip scenario → continue |
| MOBILE_CONFIGURATION missing/error | ❌ NO | Skip scenario → continue |
| DataSheet file not found | ❌ NO | Skip scenario → continue |
| Element not found (step failure) | ❌ NO | Retry → AbortCondition → continue |
| Email send failed | ❌ NO | Log warning → continue |

---

## 📊 Reporting System

The framework generates **5 types of reports** automatically:

```mermaid
graph LR
    EX["Execution<br/>Completes"] --> R1["📄 Extent HTML Report<br/>RESULT/.../REPORTs/"]
    EX --> R2["📸 Screenshots<br/>RESULT/.../SCREENSHOTs/"]
    EX --> R3["📊 HTML Summary Table<br/>RESULT/.../HtmlTables/"]
    EX --> R4["🎨 Master UI Dashboard<br/>RESULT/.../MASTERREPORTs/"]
    EX --> R5["📋 ScenarioExecutionReport.xlsx<br/>(audit trail)"]
    
    R1 -.-> |"Includes"| R1A["Test steps + status<br/>Pass/Fail screenshots<br/>Expected vs Actual"]
    R3 -.-> |"Includes"| R3A["Project name, total steps<br/>Pass/fail counts<br/>Validation summary"]
    R4 -.-> |"Includes"| R4A["Premium animated dashboard<br/>KPI cards, progress bars<br/>Links to Extent Report"]
    R5 -.-> |"Includes"| R5A["Date+Time, Scenario, Vertical<br/>Verdict, Failed Process<br/>Duration per scenario"]

    style R1 fill:#4CAF50,color:#fff
    style R2 fill:#FF9800,color:#fff
    style R3 fill:#2196F3,color:#fff
    style R4 fill:#9C27B0,color:#fff
    style R5 fill:#607D8B,color:#fff
```

### Report Path Convention

All reports are saved to:
```
RESULT/{Year}/{Month}/{Day}/{ApplicationName}/{ReportType}/
```

Example:
```
RESULT/2026/June/17/SuperApp/REPORTs/SC_01_XPL_LOGIN_Report_14_30_22.html
```

### Audit Trail (ScenarioExecutionReport.xlsx)

Every scenario execution is appended to `ScenarioExecutionReport.xlsx` with full details:

| Si No | Date | ScenarioID | Application | Vertical | Scenario | Status | Failed At | Time |
|-------|------|------------|-------------|----------|----------|--------|-----------|------|
| 1 | 20/06/2026 09:15:10 | SC_01 | SuperApp | XPL | Till UW | PASS | | 02m 14s |
| 2 | 20/06/2026 09:28:45 | SC_02 | SuperApp | SALPL | Full Journey | FAIL | LOGIN | 01m 30s |
| 3 | 20/06/2026 14:30:05 | SC_01 | SuperApp | XPL | Till UW | PASS | | 02m 08s |

> **Key behavior:** Each scenario is written to the audit file **immediately after it completes** (not at the end of all scenarios). Re-runs on the same day are distinguishable by the timestamp. Data is **never overwritten** — every execution appends new rows.

---

## 📧 Email Notifications

### Two Types of Emails

| Email Type | When Sent | Triggered By |
|------------|-----------|--------------|
| **Process Email** | After each process completes | `MailSend.mailSend()` (if MAIL_SEND RunStatus=Y) |
| **Scenario Summary Email** | After ALL scenarios complete | `MailSend.sendScenarioSummaryEmail()` (if SendScenarioSummary=Y) |

### Process Email Content

Color-coded HTML table with:

| Column | Color | Value |
|--------|-------|-------|
| Project | Green | Application name |
| Execution Type | Yellow | LOCAL / REMOTE |
| Device Platform | Blue | ANDROID / IOS / WEB |
| Total Test Cases | Purple | Unique test cases run |
| Total Test Steps | Blue | All steps executed |
| Passed Steps | Green | Steps that passed |
| Failed Steps | Red | Steps that failed |
| Total Validations | Blue | CheckVisibility count |
| Passed Validations | Green | Validation passes |
| Failed Validations | Red | Validation fails |
| Execution Time | Green | HH:MM:SS duration |

### Scenario Summary Email Content

Premium HTML email containing:

- **KPI Cards** — Total scenarios, passed, failed, duration
- **Progress Bar** — Visual pass/fail percentage
- **Scenario Details Table** — Each scenario with status badge, failed process, and execution time
- **Professional Footer** — Auto-generated by framework

> **Setup:** Add `SendScenarioSummary=Y` column to your `MAIL_SEND` sheet in MainController.xlsx. Configure `HOST`, `Password`, `MAIL_TO`, `MAIL_CC`.

---

## 🔐 License & Security

The `LicenceClass.java` acts as a **commercial license guard** for this framework.

**License validation flow:**
```
Framework JAR starts
   └─ Static block fires BEFORE main()
         └─ Checks today's date vs LICENSE_EXPIRY date
               ├─ Expired? → Popup + System.exit(1) — framework won't run
               ├─ < 30 days left? → Warning popup shown (non-blocking)
               └─ Valid → Welcome banner printed → Execution continues
```

**Key license properties (inside `LicenceClass.java`):**

| Property | Description |
|---|---|
| `LICENSE_EXPIRY` | Expiry date in `dd/MM/yyyy` format |
| `ALLOWED_MACHINE_ID` | Machine restriction (currently disabled) |
| `SUPPORT_EMAIL` | Contact for renewals |

> 🔒 **This is a commercial framework.** Unauthorized copying, redistribution, or use on unlicensed machines is prohibited.
> Contact `support@biswajitautomation.com` for license renewal or queries.

---

## ❓ FAQ for Freshers

**Q: I'm a fresher. Do I need to know Java to use this?**
> ❌ No! You only need Excel. Find the locator (XPath/ID) from the app, paste it in the sheet, pick an Action keyword. Done.

**Q: How do I find XPath for a mobile element?**
> Use Appium Inspector → connect to your device → browse the element tree → copy XPath.

**Q: How do I find XPath for a web element?**
> Open Chrome DevTools (F12) → Inspector tab → right-click element → Copy → Copy XPath.

**Q: My test failed. Where do I look?**
> 1. Check the console output — it prints the failed step clearly
> 2. Open the log file in `RESULT/.../LOGs/` — detailed Log4j logs
> 3. Open the Extent Report in `RESULT/.../REPORTs/` — shows exactly which step failed with screenshot
> 4. Check the email report — summary counts

**Q: Can I run the same steps with 3 different logins?**
> Yes! Add 3 rows in TestScript Sheet2 (RunStatus=Y for all 3). Each row = one full run with your steps.

**Q: What if the element keeps failing to find?**
> 1. Run Appium Inspector / Chrome DevTools to verify the locator is correct
> 2. Check `ExplicityWait` — maybe the page is slow; increase wait seconds
> 3. Enable `Retry=Y` and set `RetryCount=3` in MAIN_CONTROLLER
> 4. If N consecutive steps fail (N = `RepeatedFailed`), the framework aborts that scenario automatically

**Q: Where is the report after execution?**
> `RESULT/` folder → Year → Month → Day → AppName → report type.

**Q: How do I send tests to BrowserStack?**
> Set `ExecutionType=remote` in MainController and fill in BrowserStack username + access key in MOBILE_CONFIGURATION sheet. That's it.

**Q: Do I need to rebuild the JAR every time I change Excel?**
> ❌ No! The JAR reads Excel at runtime. Just change the Excel and re-run the JAR.

**Q: What happens if one scenario fails? Does the whole execution stop?**
> ❌ No! The failed scenario is recorded as FAIL, and the framework **continues with the next scenario**. Only global config errors (missing MainController.xlsx, etc.) stop everything.

---

## 🏢 Team & Credits

| Role | Name |
|------|------|
| **Framework Developer** | Biswajit Sahoo |
| **SVP** | Naresh Yadav |
| **Test Leads** | Vikrant, Shankar, Shruti |
| **Team Members** | Shantesh, Namrata, Shubham, Dinesh, Dhurvesh, Mohini |
| **Organization** | Mahindra & Mahindra Financial Services Limited |

---

## 📋 Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 16+ | Core language |
| Maven | 3.8+ | Build & dependency management |
| Selenium | 4.31.0 | Web browser automation |
| Appium Java Client | 9.3.0 | Mobile app automation |
| Fillo | 1.18 | Excel file read/write (SQL-like queries) |
| Apache POI | 3.15 | Excel workbook manipulation |
| ExtentReports | 3.1.5 | HTML test reports |
| Log4j2 | 2.22.0 | Logging |
| SnakeYAML | 2.2 | YAML config parsing |
| JavaMail | 1.6.2 | SMTP email sending |
| REST-Assured | 5.3.0 | API testing support |
| JavaFaker | 1.0.2 | Random test data generation |
| Jackson | 2.15.3 | JSON processing |
| PostgreSQL | 42.7.5 | Database connectivity |
| iText7 | 8.0.5 | PDF generation |

---

## 🔧 Method Call Sequence (Developer Reference)

For developers who need to modify or debug the framework:

```mermaid
sequenceDiagram
    participant F as Framework.main()
    participant LC as LicenceClass
    participant CM as ConfigManager
    participant MC as ConnectToMainController
    participant MOB as MobileConfiguration
    participant DRV as Android_IOS_Driver
    participant DS as ConnectToDataSheet
    participant LM as LocatorManager
    participant FN as Function
    participant UA as UtilsActivity
    participant MS as MailSend

    F->>LC: validateLicense()
    LC-->>F: ✅ License OK
    F->>CM: static { loadConfig(), resolveEnvironment() }
    F->>LC: Run()
    LC->>MC: mainControllerSheet()
    MC->>MC: loadAndGroupMainControllerRows()
    
    loop For each Scenario Group
        MC->>MC: Reset counters
        loop For each Process in Scenario
            MC->>MC: MainControlerDataSheet(Process)
            MC->>MC: testDataSheetCheck(DataSheetFile)
            MC->>MOB: mobileConfigurationSheet()
            MC->>DRV: InitialisationDriverLocal/Remote()
            Note over DRV: If fails → RuntimeException<br/>→ caught → scenario FAILED
            MC->>DS: extractAllData(sheet2rowCount)
            DS->>DS: readAndValidateStepRows()
            
            loop For each Sheet2 data row
                DS->>UA: extentReport()
                loop For each Step row
                    DS->>LM: mapToLocator()
                    LM->>FN: ActionRDS()
                    FN->>UA: passTestCase() / failTestCase()
                end
                DS->>UA: CreateHtmlTable()
                DS->>UA: webUIReport()
                DS->>MS: mailSend() [per-process]
            end
        end
        MC->>MC: Record ScenarioResult
        MC->>UA: saveScenarioAuditToReport()
    end
    
    MC->>MS: sendScenarioSummaryEmail()
    F->>LC: printExecutionTime()
```

---

<div align="center">

### 📞 Support

For questions, issues, or license renewal:

**biswajit.sahoo@mahindrafinance.com**

---

*© 2026 Mahindra Finance QA Automation Team — Biswajit AI-POWERED Self-Healing Automation Framework*

*Empowering teams to automate without code since Day 1*

</div>
