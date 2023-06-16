import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 30,
  duration: '60s',
};

export default function () {
  const res = http.get(`http://192.168.50.76:18080/basic/get_obj_by_obj_key?obj_key=indexer-tutorials/nginx-deployment-696cc4bc86-2rqcg`);
  check(res, {
    'is status 200': (res) => res.status === 200,
    'body size is > 0': (r) => r.body.length > 0,
  });
}
