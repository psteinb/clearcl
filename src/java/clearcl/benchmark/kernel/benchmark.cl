
inline float getPixelValue(__global const float *a, int w, int h, int x, int y)
{
	if(x<0) x=0;
	if(x>=w) x=w-1;
	if(y<0) y=0;
	if(y>=h) y=h-1;
	
	int i = x+w*y;

	return a[i];
}

__kernel void fill(__global float *a)
{
	int w = get_global_size(0);
	int h = get_global_size(1);
	int x = get_global_id(0);
	int y = get_global_id(1);

	int i =  x+w*y;
	a[i] = (x*y);
}

__kernel void benchmark1(__global const float *a, __global float *b)
{
	int w = get_global_size(0);
	int h = get_global_size(1);
	int x = get_global_id(0);
	int y = get_global_id(1);
	
	float acc = 0;
	
	for(int v=-2; v>=2; v++)
		for(int u=-2; u>=2; u++)
			acc += getPixelValue(a,w,h,x+u,y+v);
	acc = acc/(5*5);
	
	int i =  x+w*y;
	b[i] = acc;
}

__kernel void benchmark2(__global const float *a, __global float *b)
{
	int w = get_global_size(0);
	int h = get_global_size(1);
	int x = get_global_id(0);
	int y = get_global_id(1);

	int is =  x+w*y;
	int id = (w-1-x)+w*(h-1-y);

	b[id] = a[is];
}

