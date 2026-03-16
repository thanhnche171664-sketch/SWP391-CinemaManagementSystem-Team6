package com.swp391.team6.cinema.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeatLockService {
    private final Map<String, Long> locks = new ConcurrentHashMap<>();
    private static final long LOCK_DURATION = 5 * 60 * 1000;

    public boolean lockSeat(Long showtimeId, Long seatId) {
        String key = showtimeId + "_" + seatId;
        long now = System.currentTimeMillis();

        if (locks.containsKey(key) && locks.get(key) > now) {
            return false;
        }

        locks.put(key, now + LOCK_DURATION);
        return true;
    }

    public void releaseSeat(Long showtimeId, Long seatId) {
        locks.remove(showtimeId + "_" + seatId);
    }

    public boolean isLocked(Long showtimeId, Long seatId) {
        String key = showtimeId + "_" + seatId;
        return locks.containsKey(key) && locks.get(key) > System.currentTimeMillis();
    }
}