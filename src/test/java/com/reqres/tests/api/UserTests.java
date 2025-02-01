package com.reqres.tests.api;

import com.reqres.tests.base.BaseTest;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;

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

    @Test
    public void testListUsers() {
        Response response = given()
                .when()
                .get("/users?page=2")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertNotNull(response.jsonPath().getList("data"));
        Assert.assertFalse(response.jsonPath().getList("data").isEmpty());
    }

    @Test
    public void testCreateUser() {
        String requestBody = "{ \"name\": \"morpheus\", \"job\": \"leader\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 201);
        Assert.assertEquals(response.jsonPath().getString("name"), "morpheus");
        Assert.assertEquals(response.jsonPath().getString("job"), "leader");
        Assert.assertNotNull(response.jsonPath().getString("id"));
        Assert.assertNotNull(response.jsonPath().getString("createdAt"));
    }

    @Test
    public void testUpdateUser() {
        String requestBody = "{ \"name\": \"morpheus\", \"job\": \"zion resident\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/users/2")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("name"), "morpheus");
        Assert.assertEquals(response.jsonPath().getString("job"), "zion resident");
        Assert.assertNotNull(response.jsonPath().getString("updatedAt"));
    }

    @Test
    public void testDeleteUser() {
        Response response = given()
                .when()
                .delete("/users/2")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 204);
    }

    @Test
    public void testSuccessfulLogin() {
        String requestBody = "{ \"email\": \"eve.holt@reqres.in\", \"password\": \"cityslicka\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertNotNull(response.jsonPath().getString("token"));
    }

    @Test
    public void testUnsuccessfulLogin() {
        String requestBody = "{ \"email\": \"peter@klaven\", \"password\": \"\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 400);
        Assert.assertEquals(response.jsonPath().getString("error"), "Missing password");
    }

    @Test
    public void testUserSchemaValidation() {
        File schemaFile = new File("src/test/resources/userSchema.json");

        given()
                .when()
                .get("/users/2")
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
    }

    @Test(dataProvider = "loginData")
    public void testDataDrivenLogin(String email, String password, int expectedStatus) {
        String requestBody = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), expectedStatus);
    }

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() throws IOException {
        FileInputStream file = new FileInputStream("src/test/resources/testData.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        int rowCount = sheet.getPhysicalNumberOfRows();
        Object[][] data = new Object[rowCount - 1][3]; // Ignora o cabeçalho

        for (int i = 1; i < rowCount; i++) {
            XSSFRow row = sheet.getRow(i);
            data[i - 1][0] = row.getCell(0).getStringCellValue(); // email
            data[i - 1][1] = row.getCell(1).getStringCellValue(); // password
            data[i - 1][2] = (int) row.getCell(2).getNumericCellValue(); // expectedStatus
        }

        workbook.close();
        file.close();
        return data;
    }

}