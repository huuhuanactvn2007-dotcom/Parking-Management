#!/bin/bash
PASS="123456789"
echo "================================================================================"
echo "[SCENARIO A: NO WATCHDOG] - Tác vụ chạy 10s, Khóa cố định LeaseTime (TTL) = 4s"
echo "================================================================================"
echo "2026-08-20T22:40:00.000Z [Thread-1] INFO: Luồng 1 bắt đầu chiếm khóa Slot-1 (TTL = 4000ms)..."
docker exec parking-redis-1 redis-cli -a $PASS SET lock:slot:1 "thread-1-token" NX PX 4000 > /dev/null 2>&1
echo "2026-08-20T22:40:00.020Z [Thread-1] INFO: Luồng 1 chiếm khóa thành công! Bắt đầu xử lý giao dịch nặng (10s)..."

for i in 1 2 3 4 5; do
  sleep 1
  TTL=$(docker exec parking-redis-1 redis-cli -a $PASS TTL lock:slot:1 2>/dev/null)
  echo "2026-08-20T22:40:0$i.000Z [Redis-Cluster] DEBUG: Đang kiểm tra lock:slot:1 -> TTL còn lại: ${TTL}s"
done

echo "2026-08-20T22:40:05.100Z [Thread-2] WARN: Khóa đã hết hạn sớm! Luồng 2 nhảy vào chiếm khóa Slot-1..."
docker exec parking-redis-1 redis-cli -a $PASS SET lock:slot:1 "thread-2-token" NX PX 4000 > /dev/null 2>&1
echo "2026-08-20T22:40:05.120Z [Thread-2] ERROR: XẢY RA XÂM PHẠM TRANH CHẤP (Race Condition - Cả 2 luồng cùng ghi DB)!"
