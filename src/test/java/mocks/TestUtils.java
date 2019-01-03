package mocks;

import java.util.HashSet;
import java.util.Set;

public class TestUtils {
    public static <T> Set<T> union(Set<T>... sets) {
        Set<T> set = new HashSet<T>();
        for(Set<T> s : sets){
            set.addAll(s);
        }
        return set;
    }
}
