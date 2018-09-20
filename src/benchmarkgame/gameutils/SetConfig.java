package benchmarkgame.gameutils;

public class SetConfig {
  public static String configStr(int numBig, int numLITTLE) {
    String config = "0x";
    switch (numBig) {
      case 0: config += "0"; break;
      case 1: config += "8"; break;
      case 2: config += "9"; break;
      case 3: config += "e"; break;
      case 4: config += "f"; break;
      default: {
        System.err.println("Invalid num of bigs " + numBig);
      }
    }
    switch (numLITTLE) {
      case 0: config += "0"; break;
      case 1: config += "1"; break;
      case 2: config += "6"; break;
      case 3: config += "7"; break;
      case 4: config += "f"; break;
      default: {
        System.err.println("Invalid num of LITTLEs " + numLITTLE);
      }
    }
    return config;
  }

  public static int getProcessID() throws
    NoSuchFieldException,
    IllegalAccessException,
    NoSuchMethodException,
    java.lang.reflect.InvocationTargetException
  {
    java.lang.management.RuntimeMXBean runtime =
      java.lang.management.ManagementFactory.getRuntimeMXBean();
    java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
    jvm.setAccessible(true);
    sun.management.VMManagement mgmt =
      (sun.management.VMManagement) jvm.get(runtime);
    java.lang.reflect.Method pid_method =
      mgmt.getClass().getDeclaredMethod("getProcessId");
    pid_method.setAccessible(true);
    return (Integer)pid_method.invoke(mgmt);
  }

  public static void setConfig(int numBig, int numLITTLE) {
    try {
      Runtime r = Runtime.getRuntime();
      //
      // Build the command:
      String configStr = configStr(numBig, numLITTLE);
      final int pid = getProcessID();
      String cmd = "taskset -pa " + configStr + " " + pid;
      //
      // Set the configuration of cores:
      Process p = r.exec(cmd);
      // Check for ls failure
      if (p.waitFor() != 0) {
        System.err.println("exit value = " + p.exitValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
