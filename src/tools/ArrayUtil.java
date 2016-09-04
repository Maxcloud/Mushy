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
}
