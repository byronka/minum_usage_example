package example1;

import example1.FunctionalTests;
import example1.TheRegister;
import example1.sampledomain.ListPhotosTests;
import minum.Constants;
import minum.Context;
import minum.FullSystem;
import minum.testing.TestLogger;
import minum.utils.*;
import minum.web.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class Tests {

  public static void main(String[] args) {
    var tests = new Tests();
    tests.start();
  }

  private void start() {
    try {
      unitAndIntegrationTests();
      testFullSystem_Soup_To_Nuts();
      indicateTestsFinished();
    } catch (Exception ex) {
      MyThread.sleep(100);
      ex.printStackTrace();
    }
  }

  private final Constants constants;

  public Tests() {
    constants = new Constants();
  }

  private void indicateTestsFinished() {
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
   * stop short of running {@link FullSystem}.
   */
  private void unitAndIntegrationTests() throws Exception {
    ExecutorService es = ExtendedExecutor.makeExecutorService(constants);
    Context context = new Context(es, constants, null);
    TestLogger logger = new TestLogger(context);
    context.setLogger(logger);

    new ListPhotosTests(context).tests();

    logger.writeTestReport();
    FileUtils.deleteDirectoryRecursivelyIfExists(Path.of(constants.DB_DIRECTORY), logger);
    new ActionQueueKiller(context).killAllQueues();
    context.getExecutorService().shutdownNow();
  }

  /**
   * Run a test of the entire system.  In particular, runs code
   * from {@link FullSystem}
   */
  private void testFullSystem_Soup_To_Nuts() throws Exception {
    System.out.println("Starting a soup-to-nuts tests of the full system");
    var constants = new Constants();
    final var es = ExtendedExecutor.makeExecutorService(constants);
    var fs = new FullSystem(es, constants);
    TestLogger testLogger = new TestLogger(fs.getContext());
    fs.getContext().setLogger(testLogger);
    var wf = fs.start().getWebFramework();
    new TheRegister(wf).registerDomains();
    new FunctionalTests(wf).test();
    FileUtils.deleteDirectoryRecursivelyIfExists(Path.of(constants.DB_DIRECTORY), wf.getLogger());
    wf.getFullSystem().removeShutdownHook();
    wf.getFullSystem().close();
    wf.getFullSystem().getExecutorService().shutdownNow();
  }

}
