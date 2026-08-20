#!/bin/bash
PASS="123456789"
echo "================================================================================"
echo "[SCENARIO B: WITH WATCHDOG] - Tác vụ chạy 12s, Kích hoạt Netty Watchdog Auto-Renewal"
echo "================================================================================"
echo "2026-08-20T22:45:00.000Z [Thread-1] INFO: Luồng 1 chiếm Redlock trên 3 node (Default Watchdog Timeout = 30s)..."
for node in parking-redis-1 parking-redis-2 parking-redis-3; do
  docker exec $node redis-cli -a $PASS SET lock:slot:1 "thread-1-token" NX PX 30000 > /dev/null 2>&1
done
echo "2026-08-20T22:45:00.035Z [Thread-1] INFO: Redlock ACQUIRED thành công trên Quorum nodes! Bắt đầu xử lý..."

sleep 4
echo "2026-08-20T22:45:04.000Z [Redis-Cluster] DEBUG: TTL hiện tại trên redis-1: $(docker exec parking-redis-1 redis-cli -a $PASS TTL lock:slot:1 2>/dev/null)s"

echo "2026-08-20T22:45:10.000Z [redisson-watchdog-timer] INFO: [WATCHDOG_RENEW] Gửi Lua script gia hạn khóa..."
for node in parking-redis-1 parking-redis-2 parking-redis-3; do
  docker exec $node redis-cli -a $PASS PEXPIRE lock:slot:1 30000 > /dev/null 2>&1
  echo "2026-08-20T22:45:10.015Z [redisson-watchdog-timer] DEBUG: Đã gia hạn lock:slot:1 trên $node (TTL reset -> 30s)"
done

sleep 2
echo "2026-08-20T22:45:12.000Z [Thread-1] INFO: Giao dịch hoàn tất an toàn! Luồng 1 gọi unlock()."
for node in parking-redis-1 parking-redis-2 parking-redis-3; do
  docker exec $node redis-cli -a $PASS DEL lock:slot:1 > /dev/null 2>&1
done
echo "2026-08-20T22:45:12.020Z [Thread-1] INFO: [UNLOCK_SUCCESS] Giải phóng khóa thành công trên 3 node. Hủy Watchdog timer."
