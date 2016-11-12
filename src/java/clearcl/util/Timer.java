package clearcl.util;

public class Timer
{
  public static void printTime(String pDescription, Runnable pRunnable)
  {
    long lNanosStart = System.nanoTime();
    pRunnable.run();
    long lNanosStop = System.nanoTime();

    long lElapsedNanos = lNanosStop - lNanosStart;
    double lElapsedTimeInMilliseconds = lElapsedNanos * 1e-6;
    System.out.format("%g ms for %s \n",
                      lElapsedTimeInMilliseconds,
                      pDescription);

  }
}
