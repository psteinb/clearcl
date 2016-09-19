package clearcl;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.DataType;
import coremem.ContiguousMemoryInterface;

public class ClearCLBuffer extends ClearCLBase
{

	private ClearCLContext mClearCLContext;
	private final HostAccessType mHostAccessType;
	private final KernelAccessType mKernelAccessType;
	private final DataType mDataType;
	private final long mLength;




	public ClearCLBuffer(	ClearCLContext pClearCLContext,
												ClearCLPeerPointer pBufferPointer,
												HostAccessType pHostAccessType,
												KernelAccessType pKernelAccessType, 
												DataType pDataType,
												long pLength)
	{
		super(pClearCLContext.getBackend(), pBufferPointer);
		mClearCLContext = pClearCLContext;
		mHostAccessType = pHostAccessType;
		mKernelAccessType = pKernelAccessType;
		mDataType = pDataType;
		mLength = pLength;
	}
	
	
	public void writeTo(ContiguousMemoryInterface pContiguousMemory,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);
		
		getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue().getPeerPointer(), 
		                                   getPeerPointer(), 
		                                   pBlockingRead, 
		                                   pOffsetInBuffer*getDataType().getSizeInBytes(), 
		                                   pLengthInBuffer*getDataType().getSizeInBytes(), 
		                                   lHostMemPointer);
	}
	
	public void writeTo(Buffer pBuffer,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);
		
		getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue().getPeerPointer(), 
		                                   getPeerPointer(), 
		                                   pBlockingRead, 
		                                   pOffsetInBuffer*getDataType().getSizeInBytes(), 
		                                   pLengthInBuffer*getDataType().getSizeInBytes(), 
		                                   lHostMemPointer);
	}
	
	public void readFrom(ContiguousMemoryInterface pContiguousMemory,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);
		
		getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue().getPeerPointer(), 
		                                   getPeerPointer(), 
		                                   pBlockingRead, 
		                                   pOffsetInBuffer*getDataType().getSizeInBytes(), 
		                                   pLengthInBuffer*getDataType().getSizeInBytes(), 
		                                   lHostMemPointer);
	}
	
	public void readFrom(Buffer pBuffer,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);
		
		getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue().getPeerPointer(), 
		                                   getPeerPointer(), 
		                                   pBlockingRead, 
		                                   pOffsetInBuffer*getDataType().getSizeInBytes(), 
		                                   pLengthInBuffer*getDataType().getSizeInBytes(), 
		                                   lHostMemPointer);
	}
	
	
	
	
	
	
	
	
	

	public HostAccessType getBufferType()
	{
		return mHostAccessType;
	}
	
	public DataType getDataType()
	{
		return mDataType;
	}

	public long getLengthInElements()
	{
		return mLength;
	}
	
	public long getSizeInBytes()
	{
		return getLengthInElements()*mDataType.getSizeInBytes();
	}
	

	
	

	@Override
	public String toString()
	{
		return String.format(	"ClearCLBuffer [mBufferType=%s, mDataType=%s, getLengthInElements()=%s, getSizeInBytes()=%s]",
													mHostAccessType,
													mDataType,
													getLengthInElements(),
													getSizeInBytes());
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseBuffer(getPeerPointer());
	}



}
