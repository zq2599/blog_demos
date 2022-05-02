package com.bolingcavalry;

import com.bolingcavalry.service.SayHello;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Named;

@QuarkusTest
public class SendMessageTest {

    @Named("A")
    SayHello sayHelloA;

    @Named("B")
    SayHello sayHelloB;

    @Named("C")
    SayHello sayHelloC;

    @Test
    public void testSendMessage() {
        sayHelloA.hello();
        Log.info("\n");
        sayHelloB.hello();
        Log.info("\n");
        sayHelloC.hello();
    }
}


