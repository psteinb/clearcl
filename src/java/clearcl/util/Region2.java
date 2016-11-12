package clearcl.util;

public class Region2
{

  public static long[] origin(long... pArray)
  {
    if (pArray.length == 2)
      return pArray;
    long[] lArray = new long[2];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 0;
    for (int i = 0; i < Math.min(pArray.length,lArray.length); i++)
      lArray[i] = pArray[i];
    return lArray;
  }
  
  public static long[] region(long... pArray)
  {
    if (pArray.length == 2)
      return pArray;
    long[] lArray = new long[2];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 1;
    for (int i = 0; i <Math.min(pArray.length,lArray.length); i++)
      lArray[i] = pArray[i];
    return lArray;
  }

  public static long[] originZero()
  {
    return new long[2];
  }

}
