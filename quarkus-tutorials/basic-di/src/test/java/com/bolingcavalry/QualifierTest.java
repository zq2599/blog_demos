package com.bolingcavalry;

import com.bolingcavalry.annonation.MyQualifier;
import com.bolingcavalry.service.HelloQualifier;
import com.bolingcavalry.service.impl.HelloQualifierA;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;

@QuarkusTest
public class QualifierTest {

//    @Inject
    @MyQualifier("")
    HelloQualifier helloQualifier;

    @Test
    public void testQualifier() {
        Assertions.assertEquals(HelloQualifierA.class.getSimpleName(),
                helloQualifier.hello());
    }
}
