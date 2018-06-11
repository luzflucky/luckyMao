package test;
/*
*这里出现了两个坑，一个是需要在消息类上加上注解Message，另一个就是必须要有默认的无参构造器，不然就会报如下的错误：
*org.msgpack.template.builder.BuildContext build
*SEVERE: builder: 这个问题在github上有个issue解释了
*/

import org.msgpack.annotation.Message;

@Message
public class UserInfo {
    private String username;
    private String age;
    public String getUsername() {
        return username;
    }
    public String getAge() {
        return age;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public UserInfo(String username, String age) {
        super();
        this.username = username;
        this.age = age;
    }
    
    public UserInfo(){
        
    }
    
    
}
