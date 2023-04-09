import com.learn.myspring.bean.UserService;
import org.myspringfarmework.core.ApplicationContext;
import org.myspringfarmework.core.ClassPathXmlApplicationContext;

public class TestMyspring {

    public static void main(String[] args) {
        test1();
    }

    private static void test1() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("myspring.xml");

        Object user = applicationContext.getBean("user");
        System.out.println(user);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.save();
    }
}
