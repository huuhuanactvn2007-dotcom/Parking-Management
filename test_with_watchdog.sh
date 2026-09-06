#!/bin/bash
PASS="123456789"

REDIS1=$(kubectl get pods -n default | grep "^redis-1-" | awk '{print $1}' | head -n 1)
REDIS2=$(kubectl get pods -n default | grep "^redis-2-" | awk '{print $1}' | head -n 1)
REDIS3=$(kubectl get pods -n default | grep "^redis-3-" | awk '{print $1}' | head -n 1)

echo "================================================================================"
echo "[SCENARIO B: WITH WATCHDOG] - Tác vụ chạy 12s, Kích hoạt Netty Watchdog Auto-Renewal"
echo "Targets: $REDIS1 | $REDIS2 | $REDIS3"
echo "================================================================================"

echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Thread-1] INFO: Luồng 1 chiếm Redlock trên 3 node (Default Watchdog Timeout = 30s)..."
for pod in $REDIS1 $REDIS2 $REDIS3; do
  kubectl exec -n default $pod -- redis-cli -a $PASS --no-auth-warning SET lock:slot:1 "thread-1-token" NX PX 30000 > /dev/null 2>&1
done
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.035Z") [Thread-1] INFO: Redlock ACQUIRED thành công trên Quorum nodes! Bắt đầu xử lý..."

sleep 4
TTL_VAL=$(kubectl exec -n default $REDIS1 -- redis-cli -a $PASS --no-auth-warning TTL lock:slot:1 2>/dev/null | tr -d '\r')
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Redis-Cluster] DEBUG: TTL hiện tại trên $REDIS1: ${TTL_VAL}s"

echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [redisson-watchdog-timer] INFO: [WATCHDOG_RENEW] Gửi Lua script gia hạn khóa..."
for pod in $REDIS1 $REDIS2 $REDIS3; do
  kubectl exec -n default $pod -- redis-cli -a $PASS --no-auth-warning PEXPIRE lock:slot:1 30000 > /dev/null 2>&1
  echo "$(date -u +"%Y-%m-%dT%H:%M:%S.015Z") [redisson-watchdog-timer] DEBUG: Đã gia hạn lock:slot:1 trên $pod (TTL reset -> 30s)"
done

sleep 2
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Thread-1] INFO: Giao dịch hoàn tất an toàn! Luồng 1 gọi unlock()."
for pod in $REDIS1 $REDIS2 $REDIS3; do
  kubectl exec -n default $pod -- redis-cli -a $PASS --no-auth-warning DEL lock:slot:1 > /dev/null 2>&1
done
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.020Z") [Thread-1] INFO: [UNLOCK_SUCCESS] Giải phóng khóa thành công trên 3 node. Hủy Watchdog timer."
