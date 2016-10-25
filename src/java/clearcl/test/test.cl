
// You can include other resources
// Path relative to class OCLlib, the package is found automatically (first in class path if several exist)
//include [OCLlib] "linear/matrix.cl" 

// You can also do absolute includes:
// Note, this is more brittle to refactoring. 
// Ideally you can move code and if the kernels 
// stay at the same location relative to the classes 
// everything is ok.
//include "clearcl/test/testinclude.cl" 

// If you include something that cannot be found, 
// then it fails silently but the final source code gets annotated. 
// (check method: myprogram.getSourceCode())
//include "blu/tada.cl" 

//default:buffersum:p=0f
__kernel void buffersum(         const float p,
        		            __global const float *a,
        		            __global const float *b,
        		            __global       float *c)
{
	int x = get_global_id(0);
	
		c[x] = a[x] + b[x] + p * CONSTANT;
	
	if(x%100000==1)
	  printf("this is a test string c[%d] = %f + %f + %f = %f \n", x, a[x], b[x], p,  c[x]);
 
}

// A kernel to fill an image with beautiful garbage:
__kernel void fillimagexor(__write_only image3d_t img)
{
	int x = get_global_id(0); 
	int y = get_global_id(1);
	int z = get_global_id(2);
	
	write_imagef (img, (int4)(x, y, z, 0), x^(y+1)^(z+2));
}

