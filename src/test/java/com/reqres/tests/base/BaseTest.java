package com.reqres.tests.base;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
public class BaseTest {

    @BeforeClass
    public static void setup() {
        // Configura a URL base da API
        RestAssured.baseURI = "https://reqres.in/api";

        // Configurações adicionais (opcional)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}