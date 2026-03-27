package ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SlidingWindowRateLimiter {

    private int count;
    private long window;

    private Map<String, Deque<Long>> map = new HashMap<>();

    public SlidingWindowRateLimiter(int count, long window) {
        this.count = count;
        this.window = window;
    }

    public boolean isUserAllowed(String user) {

        long now = System.currentTimeMillis();

        Deque<Long> timestamps =  map.computeIfAbsent(user, k-> new ArrayDeque<>());
        while(!timestamps.isEmpty() && now - timestamps.peekFirst() >= window){
            timestamps.pollFirst();
        }
        if(count > timestamps.size()){
            timestamps.offerLast(now);
            return true;
        }
        return false;

    }



    public static void main(String[] args) throws InterruptedException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(3,5000);

        for(int i=1;i<=5;i++){
            System.out.println("request: "+i+":"+(rateLimiter.isUserAllowed("sanjai")?"Allow":"Deny"));
        }

        Thread.sleep(5000);
        for(int i=1;i<=1;i++){
            System.out.println("request: "+i+":"+(rateLimiter.isUserAllowed("sanjai")?"Allow":"Deny"));
        }
    }
}
