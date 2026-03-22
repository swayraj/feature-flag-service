import http from 'k6/http';
  import { check, sleep } from 'k6';

  export const options = {
    stages: [
      { duration: '10s', target: 20 }, // ramp up to 20 users
      { duration: '30s', target: 20 }, // hold at 20 users
      { duration: '10s', target: 0  }, // ramp down
    ],
    thresholds: {
      http_req_failed:   ['rate<0.01'],       // less than 1% errors
      http_req_duration: ['p(95)<500'],       // 95% of requests under 500ms
    },
  };

  const BASE_URL = 'http://localhost:8080';
  const API_KEY = 'test-key-123';

  export default function () {
    const res = http.get(`${BASE_URL}/api/flags`, {
      headers: { 'X-API-Key': API_KEY },
    });

    check(res, {
      'status is 200': (r) => r.status === 200,
      'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
  }