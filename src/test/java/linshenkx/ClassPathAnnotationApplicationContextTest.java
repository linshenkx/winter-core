package linshenkx;

import com.github.linshenkx.beanFactory.ClassPathAnnotationApplicationContext;
import org.junit.Test;

/**
 * @version V1.0
 * @author: lin_shen
 * @date: 18-11-28
 * @Description: TODO
 */

public class ClassPathAnnotationApplicationContextTest {



    @Test
    public void test(){
        ClassPathAnnotationApplicationContext classPathAnnotationApplicationContext=new ClassPathAnnotationApplicationContext(this.getClass().getPackage().getName());
        try {
            HelloController helloController = (HelloController) classPathAnnotationApplicationContext.getBean(HelloController.class,"helloController",false);
            System.out.println(helloController.helloDefault(""));
            System.out.println(helloController.hello("lin"));
            System.out.println(helloController.hello2("lin"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
