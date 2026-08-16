import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 50,          // 50 luồng ảo gửi request đồng thời
  duration: '2s',   // Bắn tải liên tục trong 2 giây
  insecureSkipTLSVerify: true,
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4Njg2MzY5NywiZXhwIjoxNzg2OTUwMDk3fQ.o5QfQjK4GRZZ338OOL_txFrkpO8yZbWmNpMcTN0uPBTRx7utTOpGlhpLFepNAENdhUpNc5yrTDMmpjvs92Y_9g";

export default function () {
  const url = 'https://localhost:8090/parking-slot-reservations/add';

  const payload = JSON.stringify({
    parkingSlot: { id: 1 },
    startTimestamp: "2027-01-01T10:00:00.000+00:00",
    durationInMinutes: 60,
    confirmName: "Test Race Condition",
    phoneNumber: "0988888888",
    confirmVehicleNumber: "30A-99999"
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${TOKEN}`
    },
  };

  const res = http.post(url, payload, params);
  check(res, {
    'status is 200/201 (Thành công)': (r) => r.status === 200 || r.status === 201,
    'status is 400/409 (Bị chặn do trùng lặp)': (r) => r.status === 400 || r.status === 409,
  });
}
