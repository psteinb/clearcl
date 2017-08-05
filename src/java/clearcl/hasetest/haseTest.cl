


__kernel void testmethod(const float a,
        		            const float b,
        		            __global float *arr )
{
	int x = get_global_id(0);
    for (int i = 0; i < 100000; i++ ) {
        float c = a * b;
        arr[x] += c;
    }
    //printf("this is a test string %f + %f\n", a, b);
	if (x % 1000 == 1) {
        printf("position in space %d\n", x);
    }
}