package ua.com.golubov.revolut;

import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class ApplicationIT {


    @BeforeClass
    public static void setUp() {
        Application.main();
    }

    @Test
    public void smokeTest() {
        RestAssured.config = newConfig()
                .jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));

        // Check initial account list
        when().get("/v1/account/list")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON)
                .body("size()", equalTo(2));

        // Add new account
        given()
                .body(Map.of("name", "Johnny Mnemonic"))
                .when()
                .post("/v1/account")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON);

        // Update existing account
        given()
                .body(Map.of("name", "John Doe Jr."))
                .when()
                .put("/v1/account/999")
                .then()
                .statusCode(204)
                .assertThat()
                .contentType(JSON);

        // Top up account
        given()
                .body(Map.of("toAcc", "999", "amount", "3500.50"))
                .when()
                .post("/v1/transaction/top-up")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON);

        // Check balance after
        when()
                .get("/v1/account/999/balance")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON)
                .body("balance", equalTo(new BigDecimal("8000.50")));

        // Execute transfer
        given()
                .body(Map.of("toAcc", "998", "fromAcc", "999", "amount", "1000.50"))
                .when()
                .post("/v1/transaction/transfer")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON);


        // Check balance after
        when()
                .get("/v1/account/999/balance")
                .then()
                .statusCode(200)
                .assertThat()
                .contentType(JSON)
                .body("balance", equalTo(new BigDecimal("7000.00")));

        // Check 404
        when()
                .get("/check-404")
                .then()
                .statusCode(404);

        // Check not existing account
        when()
                .get("/v1/account/1000/balance")
                .then()
                .statusCode(404);
    }


}
