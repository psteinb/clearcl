
__kernel void sampleKernel(	__global const float *a,
														__global const float *b,
														__global float *c)
{
	int x = get_global_id(0);
	//printf("this is a test string %d \n", x);

	c[x] = a[x] + b[x];
 
}