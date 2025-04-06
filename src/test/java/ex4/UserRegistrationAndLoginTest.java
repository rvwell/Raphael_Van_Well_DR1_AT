package ex4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRegistrationAndLoginTest {

    WebDriver webDriver;
    HomePage homePage;
    SignupLoginPage signupLoginPage;
    SignupPage signupPage;

    @BeforeEach
    void setUp() {
        webDriver = new ChromeDriver();
        webDriver.get("https://automationexercise.com/");
        homePage = new HomePage(webDriver);
        signupLoginPage = new SignupLoginPage(webDriver);
        signupPage = new SignupPage(webDriver);
    }

    @Test
    public void signupSuccessfully(){
        homePage.linkSignupLogin.click();

        signupLoginPage.signupName.sendKeys("test");
        signupLoginPage.signupEmail.sendKeys("rtest@test.com.br");
        signupLoginPage.signupButton.click();

        signupPage.titleMrRadioButton.click(); // Seleciona "Mr"
        signupPage.passwordInput.sendKeys("senha123");

        Select daysSelect = new Select(signupPage.daysDropdown);
        daysSelect.selectByValue("1");

        Select monthsSelect = new Select(signupPage.monthsDropdown);
        monthsSelect.selectByVisibleText("January");

        Select yearsSelect = new Select(signupPage.yearsDropdown);
        yearsSelect.selectByValue("1990");

        if (!signupPage.newsletterCheckbox.isSelected()) {
            signupPage.newsletterCheckbox.click();
        }

        if (!signupPage.optinCheckbox.isSelected()) {
            signupPage.optinCheckbox.click();
        }

        signupPage.firstNameInput.sendKeys("Test");
        signupPage.lastNameInput.sendKeys("User");
        signupPage.companyInput.sendKeys("Test Company");
        signupPage.address1Input.sendKeys("Test Address 1");
        signupPage.address2Input.sendKeys("Test Address 2");

        Select countrySelect = new Select(signupPage.countryDropdown);
        countrySelect.selectByVisibleText("India"); // Substitua pelo país desejado

        signupPage.stateInput.sendKeys("Test State");
        signupPage.cityInput.sendKeys("Test City");
        signupPage.zipcodeInput.sendKeys("12345");
        signupPage.mobileNumberInput.sendKeys("1234567890");

        signupPage.createAccountButton.click();

        assertTrue(webDriver.getCurrentUrl().contains("account_created"), "Falha ao criar a conta.");
    }

    @Test
    public void loginSuccessfully() {
        homePage.linkSignupLogin.click();

        signupLoginPage.loginEmail.sendKeys("rtest@test.com.br");
        signupLoginPage.loginPassword.sendKeys("senha123");
        signupLoginPage.loginButton.click();

        assertTrue(isElementPresent(By.linkText("Logout")), "Falha ao logar: Link de Logout não encontrado.");
    }

    @Test
    public void deleteSuccessfully() {
        homePage.linkSignupLogin.click();

        signupLoginPage.loginEmail.sendKeys("rtest@test.com.br");
        signupLoginPage.loginPassword.sendKeys("senha123");
        signupLoginPage.loginButton.click();

        homePage.linkDeleteAccount.click();

        assertTrue(webDriver.getCurrentUrl().contains("delete_account"), "Falha ao criar a conta.");
    }

    @Test
    public void loginWithInvalidEmail() {
        homePage.linkSignupLogin.click();

        signupLoginPage.loginEmail.sendKeys("invalid@test.com");
        signupLoginPage.loginPassword.sendKeys("senha123");
        signupLoginPage.loginButton.click();

        assertTrue(isElementPresent(By.xpath("//p[@style='color: red;']")),"Mensagem de erro no login.");

    }

    @Test
    public void loginWithInvalidPassword() {
        homePage.linkSignupLogin.click();

        signupLoginPage.loginEmail.sendKeys("rtest@test.com.br");
        signupLoginPage.loginPassword.sendKeys("senha_incorreta");
        signupLoginPage.loginButton.click();

        assertTrue(isElementPresent(By.xpath("//p[@style='color: red;']")),"Mensagem de erro no login.");
    }

    @Test
    public void loginWithEmptyCredentials() {
        homePage.linkSignupLogin.click();

        signupLoginPage.loginEmail.sendKeys("");
        signupLoginPage.loginPassword.sendKeys("");
        signupLoginPage.loginButton.click();

        assertTrue(!isElementPresent(By.linkText("Logout")), "Usuário logado com credenciais vazias.");
    }

    private boolean isElementPresent(By by) {
        try {
            return webDriver.findElement(by).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    // @AfterMethod
    public void tearDown() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }
}
