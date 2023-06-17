import http from 'k6/http';
import { check } from 'k6';

export default function () {
  const res = http.get(`http://${__ENV.MY_HOSTNAME}/basic/get_obj_by_obj_key?obj_key=${__ENV.OBJ_KEY}`);
  check(res, {
    'is status 200': (res) => res.status === 200,
    'body size is > 0': (r) => r.body.length > 0,
  });
}