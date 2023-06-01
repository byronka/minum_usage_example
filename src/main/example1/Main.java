package example1;

import minum.web.FullSystem;
import minum.web.WebFramework;

public class Main {

    public static void main(String[] args) {
        try (WebFramework wf = FullSystem.initialize()) {
          TheRegister.registerDomains(wf);
        }
    }


}
