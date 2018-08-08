package archsummit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldService {

    @Value("${greeting:Hello}")
    private String greeting;

    @Value("${REPLY:World}")
    private String name;

    public String getHelloMessage() {
        return greeting + " " + this.name;
    }

}