package com.reqres.tests.api;

import com.reqres.tests.base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class UserTests extends BaseTest {

    @Test
    public void testGetUser() {
        Response response = given()
                .when()
                .get("/users/2")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);

        String email = response.jsonPath().getString("data.email");
        Assert.assertEquals(email, "janet.weaver@reqres.in");
    }
}