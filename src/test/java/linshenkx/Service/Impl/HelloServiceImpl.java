package linshenkx.Service.Impl;

import com.github.linshenkx.annotation.Component;
import linshenkx.Service.HelloService;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */
@Component
public class HelloServiceImpl implements HelloService {

    public HelloServiceImpl() {
        System.out.println();
        System.out.println("build:"+this.getClass().getName());
        System.out.println();
    }

    @Override
    public String sayHello(String name) {
        return "Hello!"+name;
    }
}
