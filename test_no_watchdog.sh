#!/bin/bash
PASS="123456789"

REDIS1=$(kubectl get pods -n default | grep "^redis-1-" | awk '{print $1}' | head -n 1)

echo "================================================================================"
echo "[SCENARIO A: NO WATCHDOG] - Tác vụ chạy 10s, Khóa cố định LeaseTime (TTL) = 4s"
echo "Target: $REDIS1"
echo "================================================================================"

echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Thread-1] INFO: Luồng 1 bắt đầu chiếm khóa Slot-1 (TTL = 4000ms)..."
kubectl exec -n default $REDIS1 -- redis-cli -a $PASS --no-auth-warning SET lock:slot:1 "thread-1-token" NX PX 4000 > /dev/null 2>&1
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.020Z") [Thread-1] INFO: Luồng 1 chiếm khóa thành công! Bắt đầu xử lý giao dịch nặng (10s)..."

for i in 1 2 3 4 5; do
  sleep 1
  TTL_NOW=$(kubectl exec -n default $REDIS1 -- redis-cli -a $PASS --no-auth-warning TTL lock:slot:1 2>/dev/null | tr -d '\r')
  echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Redis-Cluster] DEBUG: Đang kiểm tra lock:slot:1 -> TTL còn lại: ${TTL_NOW}s"
done

echo "$(date -u +"%Y-%m-%dT%H:%M:%S.100Z") [Thread-2] WARN: Khóa đã hết hạn sớm! Luồng 2 nhảy vào chiếm khóa Slot-1..."
kubectl exec -n default $REDIS1 -- redis-cli -a $PASS --no-auth-warning SET lock:slot:1 "thread-2-token" NX PX 4000 > /dev/null 2>&1
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.120Z") [Thread-2] ERROR: XẢY RA XÂM PHẠM TRANH CHẤP (Race Condition - Cả 2 luồng cùng ghi DB)!"
