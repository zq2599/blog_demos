package com.bolingcavalry;

import com.bolingcavalry.service.impl.OtherServiceImpl;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class FieldAnnotationControllerTest {

    @Test
    public void testGetEndpoint() {
        given()
                .when().get("/fieldannotataionbean")
                .then()
                .statusCode(200)
                // 检查body内容，OtherServiceImpl.hello方法返回的字符串
                .body(containsString("from " + OtherServiceImpl.class.getSimpleName()));
    }
}