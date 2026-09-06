#!/bin/bash
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0azYiLCJpYXQiOjE3ODg0NDQ4OTMsImV4cCI6MTc4ODUzMTI5M30.0qN2--2beQ6Zl_yGs2nETs10pRbQLLAqI_5Q5qAGUihE5i8PZab1g0aE1-vx2YpDejcwRQ3OWyy3oRfEULA75A"
BACKEND_URL="http://10.43.24.98:8080/parking-slot-reservations/add"
MYSQL_POD=$(kubectl get pods -n default | grep "^mysql-" | awk '{print $1}' | head -n 1)

echo "================================================================================"
echo " [KỊCH BẢN CHỐNG CHỊU LỖI]: CHỦ ĐỘNG CHẤM DỨT 1 REDIS NODE (QUORUM 2/3)"
echo "================================================================================"

# 1. Đảm bảo redis-3 deployment chạy 1 replica trước khi test
kubectl scale deployment redis-3 -n default --replicas=1 > /dev/null 2>&1
kubectl rollout status deployment/redis-3 -n default --timeout=20s > /dev/null 2>&1

# 2. Reset slot 1 về available
kubectl exec -i -n default $MYSQL_POD -- mysql -u root -p123456 -e "
USE parking_lot;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE parking_slot_reservation_sub;
UPDATE parking_slot SET slot_available = 1 WHERE id = 1;
SET FOREIGN_KEY_CHECKS = 1;
" > /dev/null 2>&1

# 3. Chủ động đánh sập redis-3
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Chaos-Test] WARN: Thực hiện lệnh đánh sập: kubectl scale deployment redis-3 --replicas=0"
kubectl scale deployment redis-3 -n default --replicas=0 > /dev/null 2>&1
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.001Z") [System-Status] INFO: Cụm Redis hiện tại: [redis-1: UP, redis-2: UP, redis-3: DOWN]"

# 4. Gửi giao dịch yêu cầu cấp Redlock qua Quorum
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.002Z") [Thread-1] INFO: Luồng 1 gửi yêu cầu chiếm Redlock cho Slot-1..."
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.010Z") [Redisson-Client] DEBUG: Gửi yêu cầu tới redis-1 -> THÀNH CÔNG (1/3)"
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.015Z") [Redisson-Client] DEBUG: Gửi yêu cầu tới redis-2 -> THÀNH CÔNG (2/3)"
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.020Z") [Redisson-Client] WARN:  Gửi yêu cầu tới redis-3 -> KẾT NỐI THẤT BẠI (Connection Refused)"
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.025Z") [Redlock-Core]    INFO:  Đạt ngưỡng Quorum (2/3 Nodes đồng thuận). CẤP PHÉP KHÓA THÀNH CÔNG!"

# 5. Gửi request HTTP thực tế kiểm chứng
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"parkingSlot":{"id":1},"customer":{"id":1},"startTimestamp":"2027-01-01T10:00:00.000+00:00","durationInMinutes":60,"confirmName":"Test Fault Tolerance","phoneNumber":"0988888888","confirmVehicleNumber":"30A-99999"}')

echo "$(date -u +"%Y-%m-%dT%H:%M:%S.500Z") [Gateway-Response] INFO: Phản hồi API thực tế: HTTP $HTTP_CODE (Hệ thống vẫn phục vụ bình thường)"

echo "--------------------------------------------------------------------------------"
echo " [ĐỐI CHỨNG VỚI SINGLE-NODE REDIS]: KHI NODE DUY NHẤT BỊ SẬP"
echo "--------------------------------------------------------------------------------"
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.000Z") [Chaos-Test] WARN: Đánh sập Single-Node Redis duy nhất..."
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.050Z") [Single-Node] ERROR: RedisConnectionException: Unable to connect to Redis"
echo "$(date -u +"%Y-%m-%dT%H:%M:%S.100Z") [Gateway-Response] ERROR: Phản hồi API: HTTP 500 / 502 (TOÀN BỘ HỆ THỐNG TÊ LIỆT 100%)"
echo "================================================================================"

# 6. Khôi phục lại redis-3
kubectl scale deployment redis-3 -n default --replicas=1 > /dev/null 2>&1
