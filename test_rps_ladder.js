// test_rps_ladder.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    rps_ladder: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 100,
      maxVUs: 300,
      stages: [
        { target: 50, duration: '30s' },
        { target: 100, duration: '30s' },
        { target: 200, duration: '30s' },
        { target: 300, duration: '30s' },
        { target: 400, duration: '30s' },
        { target: 500, duration: '30s' },
      ],
    },
  },
  insecureSkipTLSVerify: true,
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4NzIyNDg0MiwiZXhwIjoxNzg3MzExMjQyfQ.4u4g5HSAN16tCEhparLRWqCrdppDHVFCcTu3al6feBmeAeG3drDX8ZyhGiL4qoAZ6NV4EeZzZikkepJyJ4A5Kw";

export default function () {
  const url = 'https://localhost:8090/parking-slot-reservations/add';

  // Random slot từ 1 -> 50 để kiểm tra tải đều không chỉ 1 ô duy nhất
  const randomSlotId = Math.floor(Math.random() * 50) + 1;

  const payload = JSON.stringify({
    parkingSlot: { id: randomSlotId },
    startTimestamp: "2027-01-01T10:00:00.000+00:00",
    durationInMinutes: 60,
    confirmName: "Benchmark Load",
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
    'completed': (r) => r.status === 200 || r.status === 400 || r.status === 409,
  });
}
