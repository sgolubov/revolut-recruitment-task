package ua.com.golubov.revolut;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static io.restassured.RestAssured.when;

@RunWith(JUnit4.class)
public class ApplicationIT {


    @BeforeClass
    public static void setUp() {
        Application.main();
    }

    @Test
    public void test() {
        when().get("/v1/account/list").then().statusCode(200);
    }


}
