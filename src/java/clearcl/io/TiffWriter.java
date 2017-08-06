package clearcl.io;

import java.io.File;

import clearcl.ClearCLImage;
import clearcl.interfaces.ClearCLImageInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

/**
 * Image TIFF writer
 * 
 * TODO: Replace the line marked with "Dirty hack" below. Otherwise, it may not work with
 * all kinds of images. Maybe, the ClearCLImage.writeTo method should also be listed in
 * ClearCLImageInterface
 *
 * @author royer
 * @author haesleinhuepf
 */
public class TiffWriter extends WriterBase implements WriterInterface
{
  byte[] mTransferArray;
  OffHeapMemory mTransferMemory;


  /**
   * Instanciates a Image TIFF writer. The voxel values produced by the phantom
   * are scaled accoding to y = a*x+b, and the data is saved using the provided
   * data type.
   * 
   * @param pNativeTypeEnum
   *          native type to save to.
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public TiffWriter(NativeTypeEnum pNativeTypeEnum,
                    float pScaling,
                    float pOffset)
  {
    super(pNativeTypeEnum, pScaling, pOffset);
  }

  @Override
  public boolean write(ClearCLImageInterface pImage,
                       File pFile) throws Throwable
  {
    pFile.getParentFile().mkdirs();


    int lWidth = (int) pImage.getDimensions()[0];
    int lHeight = (int) pImage.getDimensions()[1];
    int lDepth = (int) pImage.getDimensions()[2];
    
    int lPixelType = FormatTools.UINT16;
    long lUINT16BitSizeInBytes = lWidth * lHeight
                                 * FormatTools.getBytesPerPixel(lPixelType);
    long lFloatSizeInBytes = lWidth * lHeight * lDepth * Size.FLOAT;

    if (mTransferMemory == null
            || lFloatSizeInBytes != mTransferMemory.getSizeInBytes())
    {
      mTransferMemory =
              OffHeapMemory.allocateBytes("PhantomTiffWriter",
                      lFloatSizeInBytes);
    }

    if (mTransferArray == null
            || lUINT16BitSizeInBytes != mTransferArray.length
            * Size.SHORT)
    {
      mTransferArray =
              new byte[Math.toIntExact(lUINT16BitSizeInBytes)];
    }
    
    // Dirty hack:
    ClearCLImage lImage = (ClearCLImage)pImage;

    lImage.writeTo(mTransferMemory, new long[]
            { 0, 0, 0 }, new long[]
            { lWidth, lHeight, lDepth }, true);

    
    ServiceFactory factory = new ServiceFactory();
    OMEXMLService service = factory.getInstance(OMEXMLService.class);
    IMetadata meta = service.createOMEXMLMetadata();
    
    MetadataTools.populateMetadata(meta,
                                   0,
                                   null,
                                   false,
                                   "XYZCT",
                                   FormatTools.getPixelTypeString(lPixelType),
                                   lWidth,
                                   lHeight,
                                   lDepth,
                                   1,
                                   1,
                                   1);
    
    String lFileName = pFile.getAbsolutePath();
    
    System.out.println("Writing image to '" + lFileName + "'...");
    loci.formats.out.TiffWriter writer = new loci.formats.out.TiffWriter();
    writer.setCompression(loci.formats.out.TiffWriter.COMPRESSION_LZW);
    writer.setMetadataRetrieve(meta);
    writer.setId(lFileName);
    
    ContiguousBuffer lBuffer = new ContiguousBuffer(mTransferMemory);


    float lScaling = getScaling();
    float lOffset = getOffset();

    for (int z = 0; z < lDepth; z++)
    {
      int i = 0;
      while (lBuffer.hasRemainingFloat() && i < mTransferArray.length)
      {
        float lFloatValue = lBuffer.readFloat() * lScaling + lOffset;
        int lIntValue = Math.round(lFloatValue);
        byte lLowByte = (byte) (lIntValue & 0xFF);
        byte lHighByte = (byte) ((lIntValue >> 8) & 0xFF);
    
        mTransferArray[i++] = lHighByte;
        mTransferArray[i++] = lLowByte;
      }
      
      System.out.println("length="+mTransferArray.length);
      writer.saveBytes(z, mTransferArray);
    }
    writer.close();
    
    System.out.println("Done.");
    return true;
  }

}
