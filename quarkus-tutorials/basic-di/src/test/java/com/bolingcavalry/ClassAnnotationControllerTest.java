package com.bolingcavalry;

import com.bolingcavalry.service.impl.ClassAnnotationBean;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class ClassAnnotationControllerTest {

    @Test
    public void testGetEndpoint() {
        given()
                .when().get("/classannotataionbean")
                .then()
                .statusCode(200)
                // 检查body内容，是否含有ClassAnnotationBean.hello方法返回的字符串
                .body(containsString("from " + ClassAnnotationBean.class.getSimpleName()));
    }
}