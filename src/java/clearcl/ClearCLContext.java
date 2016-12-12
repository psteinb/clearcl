package clearcl;

import java.io.IOException;

import clearcl.abs.ClearCLBase;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import coremem.enums.NativeTypeEnum;

/**
 * ClearCLContext is the ClearCL abstraction for OpenCl contexts.
 *
 * @author royer
 */
public class ClearCLContext extends ClearCLBase
{

  private final ClearCLDevice mDevice;

  private final ClearCLQueue mDefaultQueue;

  /**
   * Construction of this object is done from within a ClearClDevice.
   * 
   * @param pClearCLDevice
   *          device
   * @param pContextPointer
   *          context peer pointer
   */
  ClearCLContext(ClearCLDevice pClearCLDevice,
                 ClearCLPeerPointer pContextPointer)
  {
    super(pClearCLDevice.getBackend(), pContextPointer);
    mDevice = pClearCLDevice;

    mDefaultQueue = createQueue();
  }

  /**
   * Returns the default queue. All devices are created with a default queue.
   * 
   * @return default queue
   */
  public ClearCLQueue getDefaultQueue()
  {
    return mDefaultQueue;
  }

  /**
   * Creates a queue.
   * 
   * @return queue
   */
  public ClearCLQueue createQueue()
  {
    ClearCLPeerPointer lQueuePointer =
                                     getBackend().getQueuePeerPointer(mDevice.getPeerPointer(),
                                                                      getPeerPointer(),
                                                                      true);
    ClearCLQueue lClearCLQueue =
                               new ClearCLQueue(this, lQueuePointer);
    return lClearCLQueue;
  }

  /**
   * Creates an OpenCL buffer with a given memory allocation mode, host and
   * kernel access and a template image to match for dimensions, data type and
   * number of channels.
   *
   * @param pMemAllocMode
   *          allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pTemplate
   *          image to use as template
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(MemAllocMode pMemAllocMode,
                                    HostAccessType pHostAccessType,
                                    KernelAccessType pKernelAccessType,
                                    ClearCLImage pTemplate)
  {
    return createBuffer(pMemAllocMode,
                        pHostAccessType,
                        pKernelAccessType,
                        pTemplate.getNumberOfChannels(),
                        pTemplate.getNativeType(),
                        pTemplate.getDimension());
  }

  /**
   * Creates an OpenCL buffer with a given data type and length. The host and
   * kernel access policy is read and write access for both.
   * 
   * @param pNativeType
   *          native type
   * @param pBufferLengthInElements
   *          length in elements
   * @return
   */
  public ClearCLBuffer createBuffer(NativeTypeEnum pNativeType,
                                    long pBufferLengthInElements)
  {
    return createBuffer(MemAllocMode.AllocateHostPointer,
                        HostAccessType.ReadWrite,
                        KernelAccessType.ReadWrite,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given access policy, data type and length.
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNativeType
   *          data type
   * @param pBufferLengthInElements
   *          length in elements
   * @return created buffer
   */
  public ClearCLBuffer createBuffer(HostAccessType pHostAccessType,
                                    KernelAccessType pKernelAccessType,
                                    NativeTypeEnum pNativeType,
                                    long pBufferLengthInElements)
  {
    return createBuffer(MemAllocMode.AllocateHostPointer,
                        pHostAccessType,
                        pKernelAccessType,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given access policy, data type, memory
   * allocation mode and length. The host and kernel access policy is read and
   * write access for both.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pNativeType
   *          native type
   * @param pBufferLengthInElements
   *          length in elements
   * @return
   */
  public ClearCLBuffer createBuffer(MemAllocMode pMemAllocMode,
                                    NativeTypeEnum pNativeType,
                                    long pBufferLengthInElements)
  {
    return createBuffer(pMemAllocMode,
                        HostAccessType.ReadWrite,
                        KernelAccessType.ReadWrite,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given data type, access policy, memory
   * allocation mode, native type, and length.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pDataType
   *          data type
   * @param pBufferLengthInElements
   *          length in elements
   * @return
   */
  public ClearCLBuffer createBuffer(MemAllocMode pMemAllocMode,
                                    HostAccessType pHostAccessType,
                                    KernelAccessType pKernelAccessType,
                                    NativeTypeEnum pNativeType,
                                    long pBufferLengthInElements)
  {
    return createBuffer(pMemAllocMode,
                        pHostAccessType,
                        pKernelAccessType,
                        1,
                        pNativeType,
                        pBufferLengthInElements);
  }

  /**
   * Creates an OpenCL buffer with a given data type, memory allocation mode and
   * access policy, memory allocation mode, native type, and dimensions. In this
   * case the buffer can be interpreted as an image.
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pDataType
   *          data type
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pNumberOfChannels
   *          number of channels per
   * @param pNativeType
   *          native type per channel per pixel/voxel
   * @param pDimensions
   *          image buffer dimensions
   * @return
   */
  public ClearCLBuffer createBuffer(MemAllocMode pMemAllocMode,
                                    HostAccessType pHostAccessType,
                                    KernelAccessType pKernelAccessType,
                                    long pNumberOfChannels,
                                    NativeTypeEnum pNativeType,
                                    long... pDimensions)
  {

    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];

    long lBufferSizeInBytes = lVolume * pNumberOfChannels
                              * pNativeType.getSizeInBytes();

    ClearCLPeerPointer lBufferPointer =
                                      getBackend().getBufferPeerPointer(getPeerPointer(),
                                                                        pMemAllocMode,
                                                                        pHostAccessType,
                                                                        pKernelAccessType,
                                                                        lBufferSizeInBytes);

    ClearCLBuffer lClearCLBuffer = new ClearCLBuffer(this,
                                                     lBufferPointer,
                                                     pHostAccessType,
                                                     pKernelAccessType,
                                                     pNumberOfChannels,
                                                     pNativeType,
                                                     pDimensions);
    return lClearCLBuffer;
  }

  /**
   * Creates 1D, 2D, or 3D single channel images with a given channel data type,
   * and dimensions. The host and kernel access policy is read and write access
   * for both.
   * 
   * 
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createSingleChannelImage(ImageChannelDataType pImageChannelType,
                                               long... pDimensions)
  {
    return createImage(MemAllocMode.AllocateHostPointer,
                       HostAccessType.ReadWrite,
                       KernelAccessType.ReadWrite,
                       mDevice.getType()
                              .isCPU() ? ImageChannelOrder.Intensity
                                       : ImageChannelOrder.R,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given channel order, channel data type,
   * and dimensions. The host and kernel access policy is read and write access
   * for both.
   * 
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(ImageChannelOrder pImageChannelOrder,
                                  ImageChannelDataType pImageChannelType,
                                  long... pDimensions)
  {
    return createImage(MemAllocMode.AllocateHostPointer,
                       HostAccessType.ReadWrite,
                       KernelAccessType.ReadWrite,
                       pImageChannelOrder,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D single channel image with a given memory allocation
   * and access policy, channel data type, and dimensions.
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createSingleChannelImage(HostAccessType pHostAccessType,
                                               KernelAccessType pKernelAccessType,
                                               ImageChannelDataType pImageChannelType,
                                               long... pDimensions)
  {
    return createImage(MemAllocMode.AllocateHostPointer,
                       pHostAccessType,
                       pKernelAccessType,
                       mDevice.getType()
                              .isCPU() ? ImageChannelOrder.Intensity
                                       : ImageChannelOrder.R,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given memory allocation and access
   * policy, channel order, channel data type, and dimensions.
   * 
   * 
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(HostAccessType pHostAccessType,
                                  KernelAccessType pKernelAccessType,
                                  ImageChannelOrder pImageChannelOrder,
                                  ImageChannelDataType pImageChannelType,
                                  long... pDimensions)
  {
    return createImage(MemAllocMode.AllocateHostPointer,
                       pHostAccessType,
                       pKernelAccessType,
                       pImageChannelOrder,
                       pImageChannelType,
                       pDimensions);
  }

  /**
   * Creates 1D, 2D, or 3D image with a given memory allocation and access
   * policy, channel order, channel data type, and dimensions.
   * 
   * 
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @return 1D,2D, or 3D image
   */
  public ClearCLImage createImage(MemAllocMode pMemAllocMode,
                                  HostAccessType pHostAccessType,
                                  KernelAccessType pKernelAccessType,
                                  ImageChannelOrder pImageChannelOrder,
                                  ImageChannelDataType pImageChannelType,
                                  long... pDimensions)
  {
    ImageType lImageType = ImageType.fromDimensions(pDimensions);

    ClearCLPeerPointer lImage =
                              getBackend().getImagePeerPointer(mDevice.getPeerPointer(),
                                                               getPeerPointer(),
                                                               pMemAllocMode,
                                                               pHostAccessType,
                                                               pKernelAccessType,
                                                               lImageType,
                                                               pImageChannelOrder,
                                                               pImageChannelType,
                                                               pDimensions);

    ClearCLImage lClearCLImage = new ClearCLImage(this,
                                                  lImage,
                                                  pHostAccessType,
                                                  pKernelAccessType,
                                                  lImageType,
                                                  pImageChannelOrder,
                                                  pImageChannelType,
                                                  pDimensions);

    return lClearCLImage;
  }

  /**
   * Creates a blank program, source code must be added to it.
   * 
   * @return program
   */
  public ClearCLProgram createProgram(String... pSourceCode)
  {
    ClearCLProgram lClearCLProgram = new ClearCLProgram(mDevice,
                                                        this,
                                                        null);
    for (String lSourceCode : pSourceCode)
      lClearCLProgram.addSource(lSourceCode);

    return lClearCLProgram;
  }

  /**
   * Creates a program given a list of resources locate relative to a reference
   * class.
   * 
   * @param pClassForRessource
   *          reference class to locate resources
   * @param pRessourceNames
   *          Resource file names (relative to reference class)
   * @return program
   * @throws IOException
   *           if IO problem while accessing resources
   */
  public ClearCLProgram createProgram(Class<?> pClassForRessource,
                                      String... pRessourceNames) throws IOException
  {
    ClearCLProgram lClearCLProgram = createProgram();

    for (String lRessourceName : pRessourceNames)
      lClearCLProgram.addSource(pClassForRessource, lRessourceName);

    return lClearCLProgram;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLContext [device=%s]",
                         mDevice.toString());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseContext(getPeerPointer());
    setPeerPointer(null);
  }

}
