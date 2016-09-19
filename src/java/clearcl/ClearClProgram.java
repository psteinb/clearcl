package clearcl;

import clearcl.enums.BuildStatus;

public class ClearCLProgram extends ClearCLBase
{
	private final ClearCLDevice mDevice;
	private final ClearCLContext mContext;
	private final String[] mSourceCode;
	private String mOptions;




	public ClearCLProgram(ClearCLDevice pDevice, ClearCLContext pClearCLContext,
												ClearCLPeerPointer pProgramPointer,
												String[] pSourceCode)
	{
		super(pClearCLContext.getBackend(), pProgramPointer);
		mDevice = pDevice;
		mContext = pClearCLContext;
		mSourceCode = pSourceCode;
	}
	
	public ClearCLDevice getDevice()
	{
		return mDevice;
	}

	public ClearCLContext getContext()
	{
		return mContext;
	}

	public BuildStatus build()
	{
		getBackend().buildProgram(getPeerPointer(), mOptions);
		return getBuildStatus();
	}
	
	public BuildStatus getBuildStatus()
	{
		BuildStatus lBuildStatus = getBackend().getBuildStatus(getDevice().getPeerPointer(), getPeerPointer());
		return lBuildStatus;
	}
	
	public String getBuildLog()
	{
		String lBuildLog = getBackend().getBuildLog(getDevice().getPeerPointer(), getPeerPointer()).trim();
		return lBuildLog;
	}

	public ClearCLKernel createKernel(String pKernelName)
	{
		ClearCLPeerPointer lKernelPointer = getBackend().createKernel(this.getPeerPointer(),
																															pKernelName);

		ClearCLKernel lClearCLKernel = new ClearCLKernel(	getContext(),
		                                                 	this,
																											lKernelPointer,
																											pKernelName);
		return lClearCLKernel;
	}

	public int getNumberLinesOfCode()
	{
		int lCounter = 0;
		for (String lSourceCodeFile : mSourceCode)
		{
			String[] lSplit = lSourceCodeFile.split("\\r?\\n");
			lCounter += lSplit.length;
		}
		return lCounter;
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseProgram(getPeerPointer());
	}

	@Override
	public String toString()
	{
		return String.format(	"ClearCLProgram [mOptions=%s, lines of code:%d]",
													mOptions,
													getNumberLinesOfCode());
	}






}
