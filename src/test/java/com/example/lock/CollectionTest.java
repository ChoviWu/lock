package com.example.lock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ChoviWu
 * @date 2018/12/17
 * Description :
 */
public class CollectionTest {


    @Test
    public void test1(){
        List<String> list = new ArrayList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        System.out.println(Collections.binarySearch(list,"a"));
        System.out.println(list.get(Collections.binarySearch(list,"b")-1));

    }
}
