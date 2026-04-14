package corejava;

import java.util.HashMap;
import java.util.Map;

public class MapCollection {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();

        map.put("A", 1);
        map.computeIfAbsent("A", Integer::valueOf);
        map.computeIfPresent("A", (k, v) -> v + 1);
        System.out.println(map);
    }
}
