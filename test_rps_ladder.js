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
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0azYiLCJpYXQiOjE3ODg0NDQ4OTMsImV4cCI6MTc4ODUzMTI5M30.0qN2--2beQ6Zl_yGs2nETs10pRbQLLAqI_5Q5qAGUihE5i8PZab1g0aE1-vx2YpDejcwRQ3OWyy3oRfEULA75A";

export default function () {
  const url = 'http://10.43.24.98:8080/parking-slot-reservations/add';

  const randomSlotId = Math.floor(Math.random() * 50) + 1;

  const payload = JSON.stringify({
    parkingSlot: { id: randomSlotId },
    customer: { id: 1 },
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
