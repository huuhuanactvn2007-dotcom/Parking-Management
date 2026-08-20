import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 100,
  iterations: 1000,
  insecureSkipTLSVerify: true,
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4NzIyNDg0MiwiZXhwIjoxNzg3MzExMjQyfQ.4u4g5HSAN16tCEhparLRWqCrdppDHVFCcTu3al6feBmeAeG3drDX8ZyhGiL4qoAZ6NV4EeZzZikkepJyJ4A5Kw";

export default function () {
  const url = 'https://localhost:4009/parking-slot-reservations/add';

  const payload = JSON.stringify({
    parkingSlot: { id: 1 },
    customer: { id: 1 },
    startTimestamp: "2027-01-01T10:00:00.000+00:00",
    durationInMinutes: 60,
    confirmName: "Test Race Condition Baseline",
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
  check(res, { 'status is 200/201': (r) => r.status === 200 || r.status === 201 });
}
