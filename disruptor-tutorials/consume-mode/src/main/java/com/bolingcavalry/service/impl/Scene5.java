package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.ConsumeModeService;
import com.bolingcavalry.service.MailEventHandler;
import com.bolingcavalry.service.MailWorkHandler;
import com.bolingcavalry.service.SmsEventHandler;
import org.springframework.stereotype.Service;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: C1、C2独立消费，C3依赖C1和C2
 * @date 2021/5/23 11:05
 */
@Service("scene5")
public class Scene5 extends ConsumeModeService {

    @Override
    protected void disruptorOperate() {
        MailEventHandler c1 = new MailEventHandler(eventCountPrinter);
        MailEventHandler c2 = new MailEventHandler(eventCountPrinter);
        MailEventHandler c3 = new MailEventHandler(eventCountPrinter);

        disruptor
                // C1、C2独立消费
                .handleEventsWith(c1, c2)
                // C3依赖C1和C2
                .then(c3);
    }
}
