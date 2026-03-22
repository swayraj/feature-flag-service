  import http from 'k6/http';
  import { check } from 'k6';
  import { Counter } from 'k6/metrics';

  const rateLimited = new Counter('rate_limited_requests');

  export const options = {
    stages: [
      { duration: '10s', target: 50 },
      { duration: '20s', target: 50 },
      { duration: '10s', target: 0  },
    ],
    thresholds: {
      http_req_duration: ['p(95)<1000'],
    },
  };

  const BASE_URL = 'http://localhost:8080';
  const API_KEY = 'test-key-123';

  export default function () {
    const res = http.get(`${BASE_URL}/api/flags`, {
      headers: { 'X-API-Key': API_KEY },
    });

    if (res.status === 429) {
      rateLimited.add(1);
    }

    check(res, {
      'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
      'not a server error': (r) => r.status < 500,
    });
  }