package com.bolingcavalry;

import com.bolingcavalry.service.impl.HelloServiceImpl;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class MethodAnnotationControllerTest {

    @Test
    public void testGetEndpoint() {
        given()
                .when().get("/methodannotataionbean")
                .then()
                .statusCode(200)
                // 检查body内容，HelloServiceImpl.hello方法返回的字符串
                .body(containsString("from " + HelloServiceImpl.class.getSimpleName()));
    }
}