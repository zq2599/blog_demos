import http from 'k6/http';
import { check } from 'k6';

export default function () {
  const res = http.get(`http://${__ENV.MY_HOSTNAME}/remote/get_obj_by_obj_key_remote_query?pod_name=${__ENV.POD_NAME}`);
  check(res, {
    'is status 200': (res) => res.status === 200,
    'body size is > 0': (r) => r.body.length > 0,
  });
}
