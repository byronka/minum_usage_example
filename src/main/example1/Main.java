package example1;

import minum.FullSystem;
import minum.web.WebFramework;

public class Main {

    public static void main(String[] args) {
        try (WebFramework wf = FullSystem.initialize()) {
          new TheRegister(wf).registerDomains();
        }
    }
}
