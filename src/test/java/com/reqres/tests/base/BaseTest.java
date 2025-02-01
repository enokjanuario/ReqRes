package com.reqres.tests.base;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
public class BaseTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://reqres.in/api";

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}