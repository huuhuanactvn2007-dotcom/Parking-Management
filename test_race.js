import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 50,          // 50 luồng đồng thời
  duration: '2s',   // Gửi dồn dập trong 2s
  insecureSkipTLSVerify: true,
};

const TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4NTkwMTQ3NCwiZXhwIjoxNzg1OTg3ODc0fQ.AS5sVeGmowOxaPKPow5K2X9YXEu6vmCs9V3K80RhKu-CmoTaFoomxJBneXlDdiqq95FiIENhlTvpfCyS2ZQcPw";

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
  check(res, { 'status is 200/201': (r) => r.status === 200 || r.status === 201 });
}
