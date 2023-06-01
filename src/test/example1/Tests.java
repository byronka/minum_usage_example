package example1;

import minum.testing.TestLogger;
import minum.utils.ActionQueue;
import minum.utils.FileUtils;
import minum.utils.MyThread;
import minum.web.FullSystem;
import example1.sampledomain.ListPhotosTests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class Tests {

  public static void main(String[] args) {
    try {
      unitAndIntegrationTests();
      clearTestDatabase();
      testFullSystem_Soup_To_Nuts();
      clearTestDatabase();
      indicateTestsFinished();
    } catch (Exception ex) {
      MyThread.sleep(100);
      ex.printStackTrace();
    }
  }

  private static void indicateTestsFinished() {
    MyThread.sleep(20);
    System.out.println();
    System.out.println("-------------------------");
    System.out.println("----  Tests finished ----");
    System.out.println("-------------------------");
    System.out.println();
    System.out.println("See test report at out/reports/tests/tests.xml\n");
  }

  /**
   * These tests range in size from focusing on very small elements (unit tests)
   * to larger combinations of methods and classes (integration tests) but
   * stop short of running {@link FullSystem}.  For that purpose, see {@link #testFullSystem_Soup_To_Nuts()}
   */
  private static void unitAndIntegrationTests() throws IOException {
    TestLogger logger = TestLogger.makeTestLogger();
    var es = logger.getExecutorService();
    new ListPhotosTests(logger).tests(es);
    logger.writeTestReport();
    runShutdownSequence(es);
  }

  private static void clearTestDatabase() throws IOException {
      TestLogger logger = TestLogger.makeTestLogger();
      FileUtils.deleteDirectoryRecursivelyIfExists(Path.of("out/simple_db"), logger);
      runShutdownSequence(logger.getExecutorService());
  }

  private static void runShutdownSequence(ExecutorService es) {
    ActionQueue.killAllQueues();
    es.shutdown();
  }

  /**
   * Run a test of the entire system.  In particular, runs code
   * from {@link FullSystem}
   */
  private static void testFullSystem_Soup_To_Nuts() throws Exception {
    TestLogger logger = TestLogger.makeTestLogger();
    logger.test("Starting a soup-to-nuts tests of the full system");
    var es = logger.getExecutorService();
    var fs = new FullSystem(logger, es).start();
    TheRegister.registerDomains(fs.getWebFramework());
    new FunctionalTests(logger, fs.getServer()).test();
    fs.removeShutdownHook();
    fs.close();
    es.shutdownNow();
  }

}
