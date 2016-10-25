package clearcl;

import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;
import coremem.offheap.OffHeapMemory;
import coremem.types.NativeTypeEnum;
import coremem.util.Size;

public class ClearCLHostImage implements ClearCLMemInterface
{

  private final ContiguousMemoryInterface mContiguousMemory;
  private final long[] mDimensions;

  public ClearCLHostImage(NativeTypeEnum pNativeType,
                          long... pDimensions)
  {
    super();
    mContiguousMemory = OffHeapMemory.allocateBytes(Size.of(pNativeType) * getVolume(pDimensions));
    mDimensions = pDimensions;
  }

  public ClearCLHostImage(ContiguousMemoryInterface pContiguousMemoryInterface,
                          NativeTypeEnum pNativeType,
                          long... pDimensions)
  {
    super();
    mContiguousMemory = pContiguousMemoryInterface;
    mDimensions = pDimensions;
  }

  public long[] getDimensions()
  {
    return mDimensions;
  }

  private static final long getVolume(long[] pDimensions)
  {
    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];
    return lVolume;
  }

  @Override
  public long getSizeInBytes()
  {
    return mContiguousMemory.getSizeInBytes();
  }
}
