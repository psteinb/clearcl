package clearcl.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsynchronousNotification
{
  private static final int cNumberOfThreads = 10;
  static Executor sExecutor = Executors.newFixedThreadPool(cNumberOfThreads);
  
  
  public static void notifyChange(Runnable pRunnable)
  {
    sExecutor.execute(pRunnable);
  }


}
