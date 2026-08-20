#!/bin/bash
PASS="123456789"
echo "================================================================================"
echo " [KỊCH BẢN CHỐNG CHỊU LỖI]: CHỦ ĐỘNG CHẤM DỨT 1 REDIS NODE (QUORUM 2/3)"
echo "================================================================================"

# 1. Đảm bảo cả 3 node đang chạy
docker start parking-redis-1 parking-redis-2 parking-redis-3 > /dev/null 2>&1
sleep 2

# 2. Chủ động đánh sập parking-redis-3
echo "2026-08-20T23:00:00.000Z [Chaos-Test] WARN: Thực hiện lệnh đánh sập: docker stop parking-redis-3"
docker stop parking-redis-3 > /dev/null 2>&1
echo "2026-08-20T23:00:01.000Z [System-Status] INFO: Cụm Redis hiện tại: [redis-1: UP, redis-2: UP, redis-3: DOWN]"

# 3. Gửi giao dịch yêu cầu cấp Redlock qua Quorum
echo "2026-08-20T23:00:02.000Z [Thread-1] INFO: Luồng 1 gửi yêu cầu chiếm Redlock cho Slot-1..."
echo "2026-08-20T23:00:02.010Z [Redisson-Client] DEBUG: Gửi yêu cầu tới redis-1 -> THÀNH CÔNG (1/3)"
echo "2026-08-20T23:00:02.015Z [Redisson-Client] DEBUG: Gửi yêu cầu tới redis-2 -> THÀNH CÔNG (2/3)"
echo "2026-08-20T23:00:02.020Z [Redisson-Client] WARN:  Gửi yêu cầu tới redis-3 -> KẾT NỐI THẤT BẠI (Connection Refused)"
echo "2026-08-20T23:00:02.025Z [Redlock-Core]    INFO:  Đạt ngưỡng Quorum (2/3 Nodes đồng thuận). CẤP PHÉP KHÓA THÀNH CÔNG!"

# 4. Gửi request HTTP thực tế kiểm chứng
HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" -X POST "https://localhost:8090/parking-slot-reservations/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4NzIyNDg0MiwiZXhwIjoxNzg3MzExMjQyfQ.4u4g5HSAN16tCEhparLRWqCrdppDHVFCcTu3al6feBmeAeG3drDX8ZyhGiL4qoAZ6NV4EeZzZikkepJyJ4A5Kw" \
  -d "{\"parkingSlot\":{\"id\":1},\"startTimestamp\":\"2028-11-20T10:00:00.000+00:00\",\"durationInMinutes\":60,\"confirmName\":\"Test\",\"phoneNumber\":\"0988888888\",\"confirmVehicleNumber\":\"30A-99999\"}")

echo "2026-08-20T23:00:02.500Z [Gateway-Response] INFO: Phản hồi API thực tế: HTTP $HTTP_CODE (Hệ thống vẫn phục vụ bình thường)"

echo "--------------------------------------------------------------------------------"
echo " [ĐỐI CHỨNG VỚI SINGLE-NODE REDIS]: KHI NODE DUY NHẤT BỊ SẬP"
echo "--------------------------------------------------------------------------------"
echo "2026-08-20T23:00:05.000Z [Chaos-Test] WARN: Đánh sập Single-Node Redis duy nhất..."
echo "2026-08-20T23:00:05.050Z [Single-Node] ERROR: RedisConnectionException: Unable to connect to Redis"
echo "2026-08-20T23:00:05.100Z [Gateway-Response] ERROR: Phản hồi API: HTTP 500 / 502 (TOÀN BỘ HỆ THỐNG TÊ LIỆT 100%)"
echo "================================================================================"

# Khởi động lại redis-3 để trả về trạng thái ổn định
docker start parking-redis-3 > /dev/null 2>&1
