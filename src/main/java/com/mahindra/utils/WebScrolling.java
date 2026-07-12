package com.mahindra.utils;

import com.mahindra.actions.LocatorManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class WebScrolling {


	public static void scrollUp(WebDriver driver, int Scroll) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		System.out.println("ScrollUP");
		js.executeScript("window.scrollBy(0, " + -Scroll + ")", "");

	}

	public static void scrollDown(WebDriver driver, int Scroll) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		System.out.println("ScrollDown");
		js.executeScript("window.scrollBy(0, " + Scroll + ")", "");
	}

	public static void ClearFieldUsingJavaScript(WebDriver driver, WebElement element, String Value) {

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].value = '';", element);
		js.executeScript("arguments[0].value = '"+Value+"';", element);
		js.executeScript("arguments[0].dispatchEvent(new Event('input'));", element);

	}

	public static void ScrollwebElementUntilVisible(WebDriver driver, WebElement element) {

		JavascriptExecutor js = (JavascriptExecutor) driver;
//		js.executeScript("arguments[0].scrollIntoView();", element);
		js.executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element);
//		System.out.println("insidethat block");
	}



	public static void ScrollUpAndDownwebElementUntilVisible(WebDriver driver, String element, int Scroll) throws InterruptedException {

		int maxScrollLimit = 30;
		int currentScrollCount = 1;
		boolean elementIsVisible = false;


		while(!elementIsVisible) {

			System.out.println("currentScrollCount = " + currentScrollCount);
			try {
				WebElement scrollElement = driver.findElement(By.xpath(element));
				if (scrollElement.isDisplayed()) {
					System.out.println("Element is visible!");
					elementIsVisible = true;
					break;
				}
			} catch (Exception e) {
				// Element not found yet, continue scrolling
//		        	System.out.println("inside atch block");
			}


			JavascriptExecutor js = (JavascriptExecutor) driver;
			if (currentScrollCount < maxScrollLimit / 2) {
				js.executeScript("window.scrollBy(0, " + Scroll + ")");
			} else {
				if (currentScrollCount == maxScrollLimit){
					break;
				}  else {
					js.executeScript("window.scrollBy(0, " + -Scroll + ")");
				}
			}

			currentScrollCount++;
		}

		if (!elementIsVisible) {
			System.out.println("Ohh Sorry... Scrolling limit completed. Element not found.");
		}

	}


	//best for every webElement scroll 
	public static void UniversalPerfectScrollUpAndDownWebElementUntilVisible(WebDriver driver, String element, int Scroll) throws InterruptedException {


		int maxScrollLimit = 30;
		int currentScrollCount = 1;
		boolean elementIsVisible = false;

		while (!elementIsVisible && currentScrollCount <= maxScrollLimit) {
			System.out.println("currentScrollCount = " + currentScrollCount);
			try {
				WebElement scrollElement = driver.findElement(By.xpath(element));
				if (scrollElement.isDisplayed()) {
					// Scroll the element into view to ensure it's fully visible in viewport
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", scrollElement);
					Thread.sleep(500); // optional: give time to scroll animation
					elementIsVisible = true;
					break;
				}
			} catch (Exception e) {
				// Element not found yet
			}

			// Scroll manually if element is not yet visible
			JavascriptExecutor js = (JavascriptExecutor) driver;
			if (currentScrollCount < maxScrollLimit / 2) {
				js.executeScript("window.scrollBy(0, " + Scroll + ")");
			} else {
				js.executeScript("window.scrollBy(0, " + -Scroll + ")");
			}

			currentScrollCount++;
			Thread.sleep(300); // optional: small wait after each scroll
		}

		if (!elementIsVisible) {
			System.out.println("Ohh Sorry... Scrolling limit completed. Element not found.");
		}

	}

}