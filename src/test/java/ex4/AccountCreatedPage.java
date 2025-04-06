package ex4;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AccountCreatedPage {

    @FindBy(linkText = "Signup / Login")
    public WebElement linkSignupLogin;

    public AccountCreatedPage(WebDriver driver) { PageFactory.initElements(driver, this); }
}
