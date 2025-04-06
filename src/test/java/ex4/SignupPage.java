package ex4;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class SignupPage {

    @FindBy(id = "id_gender1")
    public WebElement titleMrRadioButton;

    @FindBy(id = "name")
    public WebElement nameInput;

    @FindBy(id = "email")
    public WebElement emailInput;

    @FindBy(id = "password")
    public WebElement passwordInput;

    @FindBy(id = "days")
    public WebElement daysDropdown;

    @FindBy(id = "months")
    public WebElement monthsDropdown;

    @FindBy(id = "years")
    public WebElement yearsDropdown;

    @FindBy(id = "newsletter")
    public WebElement newsletterCheckbox;

    @FindBy(id = "optin")
    public WebElement optinCheckbox;

    @FindBy(id = "first_name")
    public WebElement firstNameInput;

    @FindBy(id = "last_name")
    public WebElement lastNameInput;

    @FindBy(id = "company")
    public WebElement companyInput;

    @FindBy(id = "address1")
    public WebElement address1Input;

    @FindBy(id = "address2")
    public WebElement address2Input;

    @FindBy(id = "country")
    public WebElement countryDropdown;

    @FindBy(id = "state")
    public WebElement stateInput;

    @FindBy(id = "city")
    public WebElement cityInput;

    @FindBy(id = "zipcode")
    public WebElement zipcodeInput;

    @FindBy(id = "mobile_number")
    public WebElement mobileNumberInput;

    @FindBy(xpath = "//button[@data-qa='create-account']")
    public WebElement createAccountButton;

    public SignupPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }
}