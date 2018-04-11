import org.junit.*;

import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SeleniumTests {

    static WebDriver driver;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("webdriver.chrome.driver","/Users/Mikkel/Drivers/chromedriver");
        driver = new ChromeDriver();
        driver.get("http://localhost:3000/");
    }

    @Before
    public void setup() {
    }

    /**
     * 1. Verification test
     */
    @Test
    public void test1() {
        List<WebElement> rows = DOM.getRows();
        assertEquals(5, rows.size());
    }

    /**
     * 2. Filter test
     */
    @Test
    public void test2() {
        WebElement filterInput = DOM.getFilter();
        filterInput.click();
        filterInput.sendKeys("2002");
        assertEquals(2, DOM.getRows().size());
    }

    /**
     * 3. Clear test
     * Test deletion of text in field. Seleniums clear method doesn't work here.
     * It clears the text, but Angular is not updating the rows.
     */
    @Test
    public void test3() {
        WebElement filterInput = DOM.getFilter();
        filterInput.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertEquals(5, DOM.getRows().size());
    }

    /**
     * 4. Sort test
     */
    @Test
    public void test4() {
        driver.findElement(By.id("h_year")).click();

        List<WebElement> rows = DOM.getRows();
        WebElement topRow = rows.get(0);
        String firstId = topRow.findElements(By.tagName("td")).get(0).getText();
        assertEquals("938", firstId);

        WebElement bottomRow = rows.get(rows.size() - 1);
        String lastId = bottomRow.findElements(By.tagName("td")).get(0).getText();
        assertEquals("940", lastId);
    }

    /**
     * 5. Edit test
     */
    @Test
    public void test5() {
        List<WebElement> rows = DOM.getRows();

        // Get the row where id = 938
        Optional<WebElement> id938 = rows.stream().filter(webElement -> webElement.findElements(By.tagName("td")).get(0).getText().equals("938")).findFirst();

        assertTrue(id938.isPresent());
        WebElement id938Element = id938.get();
        List<WebElement> tds = id938Element.findElements(By.tagName("td"));

        // Click on edit button
        tds.get(tds.size() - 1).findElements(By.tagName("a")).get(0).click();

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.click();
        descriptionField.clear();
        descriptionField.sendKeys("Cool car");

        DOM.getSaveButton().click();

        tds = id938Element.findElements(By.tagName("td"));
        assertEquals("Cool car", tds.get(5).getText());
    }


    // 6. New car error test
    @Test
    public void test6() {
        DOM.getNewCarButton().click();
        DOM.getSaveButton().click();

        String content = DOM.getSubmitErr().getText();

        assertEquals("All fields are required", content);
        assertEquals(5, DOM.getRows().size());
    }

    @Test
    public void test7() {
        DOM.getNewCarButton().click();

        WebElement yearElement = driver.findElement(By.id("year"));
        WebElement registeredElement = driver.findElement(By.id("registered"));
        WebElement makeElement = driver.findElement(By.id("make"));
        WebElement modelElement = driver.findElement(By.id("model"));
        WebElement descriptionElement = driver.findElement(By.id("description"));
        WebElement priceElement = driver.findElement(By.id("price"));

        yearElement.sendKeys("2008");
        registeredElement.sendKeys("2002-5-5");
        makeElement.sendKeys("Kia");
        modelElement.sendKeys("Rio");
        descriptionElement.sendKeys("As new");
        priceElement.sendKeys("31000");

        DOM.getSaveButton().click();

        List<WebElement> rows = DOM.getRows();
        assertEquals(6, rows.size());

        List<WebElement> data = rows.get(rows.size() - 1).findElements(By.tagName("td"));
        assertEquals("2008", data.get(1).getText());
        assertEquals("5/5/2002", data.get(2).getText());
        assertEquals("Kia", data.get(3).getText());
        assertEquals("Rio", data.get(4).getText());
        assertEquals("As new", data.get(5).getText());
        assertEquals("31.000,00 kr.", data.get(6).findElement(By.tagName("span")).getText());
    }

    /**
     * DOM inner class to hold methods and an instance of tBody
     */
    private static class DOM {
        private static WebElement tBody = driver.findElement(By.id("tbodycars"));

        private static WebElement getTBody() {
            return tBody;
        }

        private static List<WebElement> getRows() {
            return tBody.findElements(By.tagName("tr"));
        }

        private static WebElement getFilter() {
            return driver.findElement(By.id("filter"));
        }

        private static WebElement getSaveButton() {
            return driver.findElement(By.id("save"));
        }

        private static WebElement getNewCarButton() {
            return driver.findElement(By.id("new"));
        }

        private static WebElement getSubmitErr() {
            return driver.findElement(By.id("submiterr"));
        }
    }

    @AfterClass
    public static void tearDown() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:3000/reset");
        target.request().get();
        driver.quit();
    }
}

