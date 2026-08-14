import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 100,          // 100 Virtual Users
  iterations: 1000,  // Khung chuẩn 1.000 request cho NCKH
  insecureSkipTLSVerify: true,
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4NjA5Mzg3MCwiZXhwIjoxNzg2MTgwMjcwfQ.SMlQyyLWVVAMJw-zhiQCRB14b-43n8hFeDDsi6uTgpe6WaOzPX3lNg3wBdEbCVIUyOwlA8ROENH7w789oPKsbQ";

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
  check(res, { 'status is 200': (r) => r.status === 200 });
}
