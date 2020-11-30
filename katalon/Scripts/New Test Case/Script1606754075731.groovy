import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.openBrowser('')

WebUI.navigateToUrl('http://localhost:8089/demo/swagger-ui.html#/')

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_ShowHide'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_error'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_error_1'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/div_head                              error_958ec5'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_error_1_2'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_error_1_2_3'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_Expand Operations'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_List Operations'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_ShowHide'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_ShowHide'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_ShowHide'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_ShowHide_1'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_List Operations'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_List Operations'))

WebUI.click(findTestObject('Object Repository/Page_Swagger UI/a_Expand Operations'))

WebUI.closeBrowser()

