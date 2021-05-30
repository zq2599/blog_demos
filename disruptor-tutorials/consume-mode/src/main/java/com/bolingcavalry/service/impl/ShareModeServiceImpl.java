package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.ConsumeModeService;
import com.bolingcavalry.service.MailWorkHandler;
import org.springframework.stereotype.Service;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 方法实现
 * @date 2021/5/23 11:05
 */
@Service("shareModeService")
public class ShareModeServiceImpl extends ConsumeModeService {
    @Override
    protected void disruptorOperate() {
        // mailWorkHandler1模拟一号邮件服务器
        MailWorkHandler mailWorkHandler1 = new MailWorkHandler(eventCountPrinter);

        // mailWorkHandler2模拟一号邮件服务器
        MailWorkHandler mailWorkHandler2 = new MailWorkHandler(eventCountPrinter);

        // 调用handleEventsWithWorkerPool，表示创建的多个消费者以共同消费的模式消费
        disruptor.handleEventsWithWorkerPool(mailWorkHandler1, mailWorkHandler2);
    }
}
