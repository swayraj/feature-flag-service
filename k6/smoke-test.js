import http from 'k6/http';
  import { check, sleep } from 'k6';

  export const options = {
    vus: 1,           // 1 virtual user
    iterations: 10,   // run exactly 10 requests total
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

    sleep(0.5); // wait 0.5s between requests
  }