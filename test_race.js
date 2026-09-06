import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    race_test: {
      executor: 'shared-iterations',
      vus: 100,
      iterations: 1000,
      maxDuration: '10m',
    },
  },
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0azYiLCJpYXQiOjE3ODg0NDQ4OTMsImV4cCI6MTc4ODUzMTI5M30.0qN2--2beQ6Zl_yGs2nETs10pRbQLLAqI_5Q5qAGUihE5i8PZab1g0aE1-vx2YpDejcwRQ3OWyy3oRfEULA75A";

export default function () {
  const url = 'http://10.43.24.98:8080/parking-slot-reservations/add';

  const payload = JSON.stringify({
    parkingSlot: { id: 1 },
    customer: { id: 1 },
    startTimestamp: "2027-01-01T10:00:00.000+00:00",
    durationInMinutes: 60,
    confirmName: "Test Multi-Node Redlock",
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
    'status is 200': (r) => r.status === 200,
  });
}
