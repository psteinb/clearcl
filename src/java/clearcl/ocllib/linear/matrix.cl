

inline float4 matrix_mult(float matrix[16], float4 vector) 
{
  const float4 result = (float4)(0,0,0,0);
  result.x = dot(vector, ((float4)(matrix[0],matrix[4],matrix[8],matrix[12])));
  result.y = dot(vector, ((float4)(matrix[1],matrix[5],matrix[9],matrix[13])));
  result.z = dot(vector, ((float4)(matrix[2],matrix[6],matrix[10],matrix[14])));
  result.w = dot(vector, ((float4)(matrix[3],matrix[7],matrix[11],matrix[15])));    
  return result;             
}

