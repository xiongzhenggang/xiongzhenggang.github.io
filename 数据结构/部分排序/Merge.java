package xzg.paixun.com;

import java.util.Arrays;

public class Merge {  
    /** 
     * 归并排序 
     * 简介:将两个（或两个以上）有序表合并成一个新的有序表 即把待排序序列分为若干个子序列，
     * 每个子序列是有序的。然后再把有序子序列合并为整体有序序列 
     * 时间复杂度为O(nlogn) 
     * 稳定排序方式 
     * @param nums 待排序数组 
     * @return 输出有序数组 
     */  
    public static int[] sort(int[] nums, int low, int high) {  
        int mid = (low + high) / 2;  
        if (low < high) {  
            // 左边  
            sort(nums, low, mid);  
            // 右边  
            sort(nums, mid + 1, high);  
            // 左右归并  
            merge(nums, low, mid, high);  
        }  
        return nums;  
    }  
    public static void merge(int[] nums, int low, int mid, int high) {  
        int[] temp = new int[high - low + 1];  
        int i = low;// 左指针  
        int j = mid + 1;// 右指针  
        int k = 0;  
  
        // 把较小的数先移到新数组中  
        while (i <= mid && j <= high) {  
            if (nums[i] < nums[j]) {  
                temp[k++] = nums[i++];  
            } else {  
                temp[k++] = nums[j++];  
            }  
        }  
        // 把左边剩余的数移入数组  
        while (i <= mid) {  
            temp[k++] = nums[i++];  
        }  
        // 把右边边剩余的数移入数组  
        while (j <= high) {  
            temp[k++] = nums[j++];  
        }  
        // 把新数组中的数覆盖nums数组  
        for (int k2 = 0; k2 < temp.length; k2++) {  
            nums[k2 + low] = temp[k2];  
        }  
    }  
    // 归并排序的实现  
    public static void main(String[] args) {  
        int[] nums = { 2, 7, 8, 3, 1, 6, 9, 0, 5, 4 };  
        Merge.sort(nums, 0, nums.length-1);  
        System.out.println(Arrays.toString(nums));  
    }  
}
//===================================后语==============================================
 /**在并归排序中用了递归的算法。递归在工作中相较于循环更好理解，但是在效率上不如循环高。因此有时候需要将递归
  *转换成非递归的形式，最常用的就是使用栈。事实上大部分的编译器就是把递归转换成栈来实现的。通常当调用一个
 *方法时，编译器会把这个方法的所有参数及其返回的地址（这个方法返回时控制达到的地方）都压入栈中，然后把控制移交该这个方法
 *当这个方法返回的时候，这些值就退栈了，参数消失。并且控制权重新回到返回值的地方。
 */
