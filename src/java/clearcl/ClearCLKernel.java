package clearcl;

public class ClearCLKernel extends ClearCLBase
{
	private ClearCLContext mClearCLContext;
	private ClearCLProgram mClearCLProgram;
	private String mName;

	public ClearCLKernel(	ClearCLContext pClearCLContext,
												ClearCLProgram pClearCLProgram,
												ClearCLPeerPointer pKernelPointer,
												String pKernelName)
	{
		super(pClearCLProgram.getBackend(), pKernelPointer);
		mClearCLContext = pClearCLContext;
		mClearCLProgram = pClearCLProgram;
		mName = pKernelName;
	}

	public void setArguments(Object... pArguments)
	{
		int i = 0;
		for (Object lObject : pArguments)
		{
			setArgument(i, lObject);
			i++;
		}
	}

	public void setArgument(int pIndex, Object pObject)
	{
		getBackend().setKernelArgument(	this.getPeerPointer(),
																		pIndex,
																		pObject);
	}

	public void run(boolean pBlockingRun, long... pGlobalSizes)
	{
		run(mClearCLContext.getDefaultQueue(),
				pBlockingRun,
				pGlobalSizes.length,
				null, //getConstantArray(pGlobalSizes.length, 0)
				pGlobalSizes,
				getConstantArray(pGlobalSizes.length, 1));
	}

	public void run(boolean pBlockingRun,
									int pNumberOfDimension,
									long[] pGlobalOffsets,
									long[] pGlobalSizes,
									long[] pLocalSize)
	{
		run(mClearCLContext.getDefaultQueue(),
				pBlockingRun,
				pNumberOfDimension,
				pGlobalOffsets,
				pGlobalSizes,
				pLocalSize);
	}

	public void run(ClearCLQueue pClearCLQueue,
									boolean pBlockingRun,
									int pDimensions,
									long[] pGlobalOffsets,
									long[] pGlobalSizes,
									long[] pLocalSizes)
	{
		getBackend().enqueueKernelExecution(pClearCLQueue.getPeerPointer(),
																				getPeerPointer(),
																				pDimensions,
																				pGlobalOffsets,
																				pGlobalSizes,
																				pLocalSizes);

		if (pBlockingRun)
			pClearCLQueue.waitToFinish();
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseKernel(getPeerPointer());
	}

	@Override
	public String toString()
	{
		return String.format(	"ClearCLKernel [mClearCLProgram=%s, mName=%s]",
													mClearCLProgram,
													mName);
	}

	private static final long[] getConstantArray(	int pDimension,
																								int pValue)
	{
		long[] lArray = new long[pDimension];
		for (int i = 0; i < pDimension; i++)
			lArray[i] = pValue;
		return lArray;
	}

}
