package clearcl.util;

public class Region3
{

  public static long[] origin(long... pArray)
  {
    if (pArray.length == 3)
      return pArray;
    long[] lArray = new long[3];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 0;
    for (int i = 0; i < pArray.length; i++)
      lArray[i] = pArray[i];
    return lArray;
  }
  
  public static long[] region(long... pArray)
  {
    if (pArray.length == 3)
      return pArray;
    long[] lArray = new long[3];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 1;
    for (int i = 0; i < pArray.length; i++)
      lArray[i] = pArray[i];
    return lArray;
  }

  public static long[] originZero()
  {
    return new long[3];
  }

}
