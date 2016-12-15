package clearcl;

import coremem.enums.NativeTypeEnum;
import coremem.interfaces.SizedInBytes;
import coremem.util.Size;

/**
 * ClearCLLocalMemory is the ClearCL abstraction for local memory passed to kernels.
 *
 * @author royer
 */
public class ClearCLLocalMemory implements SizedInBytes
{
  public NativeTypeEnum mNativeTypeEnum;
  public long mNumberOfElements;

  /**
   * Instantiates a local memory object,
   * @param pNativeTypeEnum
   * @param pNumberOfElements
   */
  public ClearCLLocalMemory(NativeTypeEnum pNativeTypeEnum,
                     long pNumberOfElements)
  {

    mNativeTypeEnum = pNativeTypeEnum;
    mNumberOfElements = pNumberOfElements;
  }

  @Override
  public long getSizeInBytes()
  {
    return Size.of(mNativeTypeEnum)*mNumberOfElements;
  }

}