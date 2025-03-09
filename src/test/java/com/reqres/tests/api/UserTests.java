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

import static com.reqres.tests.utils.Utils.getCellValueAsInt;
import static com.reqres.tests.utils.Utils.getCellValueAsString;
import static io.restassured.RestAssured.given;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;


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
        File schemaFile = new File("src/test/resources/schemas/userSchema.json");

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
        FileInputStream file = new FileInputStream("src/test/resources/files/testData.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        int rowCount = sheet.getPhysicalNumberOfRows();
        Object[][] data = new Object[rowCount - 1][3];

        for (int i = 1; i < rowCount; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            data[i - 1][0] = getCellValueAsString(row.getCell(0));
            data[i - 1][1] = getCellValueAsString(row.getCell(1));
            data[i - 1][2] = getCellValueAsInt(row.getCell(2));

        }

        workbook.close();
        file.close();
        return data;
    }

    @Test
    public void testUsersPerPage() {
        Response response = given()
                .when()
                .get("/users")
                .then()
                .extract()
                .response();

        int usersPerPage = response.jsonPath().getInt("per_page");
        int totalUsers = response.jsonPath().getInt("total");
        int totalPages = response.jsonPath().getInt("total_pages");

        Assert.assertTrue(usersPerPage > 0, "[LOG] O número de usuários por página deve ser maior que 0.");
        Assert.assertTrue(usersPerPage <= totalUsers, "[LOG] O número de usuários por página não pode ser maior que o total de usuários.");
        Assert.assertEquals(totalPages, (int) Math.ceil((double) totalUsers / usersPerPage),
                "[LOG] O número total de páginas deve ser consistente com o total de usuários e usuários por página.");
    }

    @Test
    public void testListUsersSchemaValidation() {
        File schemaFile = new File("src/test/resources/schemas/listUsersSchema.json");

        given()
                .when()
                .get("/users")
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
    }

    @Test
    public void testTotalPages() {
        Response response = given()
                .when()
                .get("/users")
                .then()
                .extract()
                .response();

        int totalPages = response.jsonPath().getInt("total_pages");
        int totalUsers = response.jsonPath().getInt("total");
        int usersPerPage = response.jsonPath().getInt("per_page");

        Assert.assertTrue(totalPages > 0, "[LOG] O número total de páginas deve ser maior que 0.");
        Assert.assertEquals(totalPages, (int) Math.ceil((double) totalUsers / usersPerPage),
                "[LOG] O número total de páginas deve ser consistente com o total de usuários e usuários por página.");
    }

    @Test
    public void testPage3HasNoUsers() {
        Response response = given()
                .when()
                .get("/users?page=3")
                .then()
                .extract()
                .response();

        int totalPages = response.jsonPath().getInt("total_pages");

        if (totalPages < 3) {
            Assert.assertTrue(response.jsonPath().getList("data").isEmpty(),
                    "[LOG] A página 3 não deve conter usuários, pois não existe.");
        } else {
            Assert.assertFalse(response.jsonPath().getList("data").isEmpty(),
                    "[LOG] A página 3 deve conter usuários.");
        }
    }

}