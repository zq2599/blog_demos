package com.bolingcavalry;

import com.bolingcavalry.service.SayHello;
import io.quarkus.arc.All;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@QuarkusTest
public class InjectAllTest {
    /**
     * 用Instance接收注入，得到所有SayHello类型的bean
     */
    @Inject
    Instance<SayHello> instance;

    @Test
    public void testInstance() {
        // instance中有迭代器，可以用遍历的方式得到所有bean
        for (SayHello sayHello : instance) {
            sayHello.hello();
        }
    }

    /**
     * 用All注解可以将SayHello类型的bean全部注入到list中，
     * 这样更加直观
     */
    @All
    List<InstanceHandle<SayHello>> list;

    @Test
    public void testQuarkusAllAnnonation() {
        for (InstanceHandle<SayHello> instanceHandle : list) {
            // InstanceHandle#get可以得到注入bean
            SayHello sayHello = instanceHandle.get();

            // InjectableBean封装了注入bean的元数据信息
            InjectableBean<SayHello> injectableBean = instanceHandle.getBean();

            // 例如bean的作用域就能从InjectableBean中取得
            Class clazz = injectableBean.getScope();

            // 打印出来验证
            Log.infov("bean [{0}], scope [{1}]", sayHello.getClass().getSimpleName(), clazz.getSimpleName() );
        }
    }
}


