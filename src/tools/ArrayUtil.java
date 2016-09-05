package tools;

public class ArrayUtil {

    /**
     * Concatenates multiple int arrays.
     * @param args
     * @return
     */
    public static int[] concat(int[]... args){
        //efficient? I guess not.
        int totalLength = 0;
        for(int[] arg : args){
            totalLength += arg.length;
        }
        int[] res = new int[totalLength];
        int i = 0;
        for(int[] arg : args){
            for(int element : arg){
                res[i] = element;
                i++;
            }
        }
        return res;
    }

    /**
     *
     * @param intArray
     * @return
     */
    public static int absoluteMax(int[] intArray){
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < intArray.length; i++){
            if(Math.abs(intArray[i]) > max && intArray[i] != 0){
                max = intArray[i];
            }
        }
        return max == Integer.MIN_VALUE ? 0 : max;
    }
}
