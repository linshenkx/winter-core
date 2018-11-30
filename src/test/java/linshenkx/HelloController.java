package linshenkx;


import com.github.linshenkx.annotation.Autowired;
import com.github.linshenkx.annotation.Component;
import com.github.linshenkx.annotation.Qualifier;
import linshenkx.Service.HelloService;
import linshenkx.Service.Impl.HelloServiceImpl2;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */
@Component
public class HelloController {
    @Autowired
    private HelloService helloService;

    @Autowired
    private HelloService myHelloService2;

    @Autowired
    @Qualifier("myHelloService1")
    private HelloService helloService1;

    @Autowired
    @Qualifier("myHelloService2")
    private HelloService helloService2;

    @Autowired
    private HelloServiceImpl2 helloService22;

    public String hello(String name){
        return helloService1.sayHello(name);
    }
    public String hello2(String name){
        return myHelloService2.sayHello(name);
    }

    public String helloDefault(String name){
        return helloService.sayHello(name);
    }
}
