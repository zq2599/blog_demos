package simpleprovider.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 提供web服务的controller
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/7/28 11:08
 */
@RestController
public class ProviderController {

    @RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
    public String hello(@PathVariable("name") String name){
        return "hello " + name + ", " + new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss").format(new Date());
    }
}
