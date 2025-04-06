package ex4;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {
    @FindBy(linkText = "Signup / Login")
    public WebElement linkSignupLogin;

    @FindBy(linkText = "Delete Account")
    public WebElement linkDeleteAccount;

    public HomePage(WebDriver driver) { PageFactory.initElements(driver, this); }
}
