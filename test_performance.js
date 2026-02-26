import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 100 },   // Ramp up: 0 to 100 users in 30s
    { duration: '1m', target: 500 },    // Ramp up: 100 to 500 users in 1min
    { duration: '2m', target: 1000 },   // Ramp up: 500 to 1000 users in 2min
    { duration: '2m', target: 1000 },   // Stabilize: 1000 users for 2min
    { duration: '1m', target: 0 },      // Ramp down: 1000 to 0 users in 1min
  ],
  thresholds: {
    http_req_duration: ['p(95)<50', 'p(99)<100'], // P95 < 50ms, P99 < 100ms
    http_req_failed: ['rate<0.001'],               // Error rate < 0.1%
    http_reqs: ['rate>10000'],                      // Throughput > 10,000 req/s
  },
};

export default function () {
  const baseUrl = 'http://localhost:8081/api/v1/tasks';
  
  // GET request - List tasks
  const getResponse = http.get(baseUrl);
  const getSuccess = check(getResponse, {
    'GET status is 200': (r) => r.status === 200,
    'GET response time < 100ms': (r) => r.timings.duration < 100,
  });
  errorRate.add(!getSuccess);
  
  // POST request - Create task (occasionally)
  if (Math.random() < 0.1) { // 10% of requests are POST
    const payload = JSON.stringify({
      title: `Task ${__VU}-${__ITER}`,
      description: 'Task created during performance test',
      status: 'PENDING',
      priority: Math.floor(Math.random() * 5)
    });
    
    const params = {
      headers: { 'Content-Type': 'application/json' },
    };
    
    const postResponse = http.post(baseUrl, payload, params);
    const postSuccess = check(postResponse, {
      'POST status is 201': (r) => r.status === 201,
      'POST response time < 200ms': (r) => r.timings.duration < 200,
    });
    errorRate.add(!postSuccess);
  }
  
  sleep(0.1); // 100ms between requests
}
