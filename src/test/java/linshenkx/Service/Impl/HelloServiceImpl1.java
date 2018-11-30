package linshenkx.Service.Impl;

import com.github.linshenkx.annotation.Component;
import linshenkx.Service.HelloService;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */
@Component("myHelloService1")
public class HelloServiceImpl1 implements HelloService {

    public HelloServiceImpl1() {
        System.out.println();
        System.out.println("build:"+this.getClass().getName());
        System.out.println();
    }

    @Override
    public String sayHello(String name) {
        return "Hello1!"+name;
    }
}
