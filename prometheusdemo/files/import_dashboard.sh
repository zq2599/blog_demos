#!/bin/bash



GRAFANA_HOST=$1
API_KEY=$2

echo "grafana host ["${GRAFANA_HOST}"]"
echo "api key ["${API_KEY}"]"

echo "start create datasource"
#curl -H "Authorization: Bearer ${API_KEY}" http://${GRAFANA_HOST}:3000/api/dashboards/home

curl -X POST \
  http://${GRAFANA_HOST}:3000/api/datasources \
-H "Content-Type:application/json" \
-H "Authorization: Bearer ${API_KEY}" \
-d '{"name":"Prometheus","type":"prometheus","url":"http://prometheus:9090","access":"proxy","basicAuth":false}' \



echo ""
echo "start create host dashboard"

curl -X POST \
  http://${GRAFANA_HOST}:3000/api/dashboards/db \
  -H 'Accept: application/json' \
  -H "Authorization: Bearer ${API_KEY}" \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 2d3c3d60-4c5a-4936-836f-1572d447f473' \
  -H 'cache-control: no-cache' \
  -d '{
  "dashboard": {
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "description": "重要指标展示：CPU 内存 磁盘 IO 网络等",
  "editable": true,
  "gnetId": 9276,
  "graphTooltip": 0,
  "iteration": 1552124595156,
  "links": [],
  "panels": [
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "decimals": 1,
      "description": "",
      "format": "s",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 5,
        "w": 4,
        "x": 0,
        "y": 0
      },
      "hideTimeOverride": true,
      "id": 15,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "time() - node_boot_time_seconds{instance=~\"$node\"}",
          "format": "time_series",
          "hide": false,
          "instant": true,
          "intervalFactor": 2,
          "refId": "A",
          "step": 40
        }
      ],
      "thresholds": "",
      "title": "系统运行时间",
      "transparent": false,
      "type": "singlestat",
      "valueFontSize": "100%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "description": "",
      "format": "short",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 4,
        "x": 4,
        "y": 0
      },
      "id": 14,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "minSpan": 4,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "count(count(node_cpu_seconds_total{instance=~\"$node\", mode='\''system'\''}) by (cpu))",
          "format": "time_series",
          "instant": true,
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A",
          "step": 20
        }
      ],
      "thresholds": "",
      "title": "CPU 核数",
      "type": "singlestat",
      "valueFontSize": "100%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(50, 172, 45, 0.97)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(245, 54, 54, 0.9)"
      ],
      "datasource": "Prometheus",
      "decimals": 2,
      "description": "",
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": true,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 5,
        "w": 4,
        "x": 8,
        "y": 0
      },
      "id": 167,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "minSpan": 2,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": true
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "100 - (avg(irate(node_cpu_seconds_total{instance=~\"$node\",mode=\"idle\"}[1m])) * 100)",
          "format": "time_series",
          "hide": false,
          "instant": false,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A",
          "step": 20
        }
      ],
      "thresholds": "50,80",
      "title": "CPU使用率（1m）",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(50, 172, 45, 0.97)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(245, 54, 54, 0.9)"
      ],
      "datasource": "Prometheus",
      "decimals": 0,
      "description": "",
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": true,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 5,
        "w": 4,
        "x": 12,
        "y": 0
      },
      "hideTimeOverride": false,
      "id": 172,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "minSpan": 4,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": true
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "((node_memory_MemTotal_bytes{instance=~\"$node\"} - node_memory_MemFree_bytes{instance=~\"$node\"} - node_memory_Buffers_bytes{instance=~\"$node\"} - node_memory_Cached_bytes{instance=~\"$node\"}) / (node_memory_MemTotal_bytes{instance=~\"$node\"} )) * 100",
          "format": "time_series",
          "hide": false,
          "interval": "10s",
          "intervalFactor": 1,
          "refId": "A",
          "step": 20
        }
      ],
      "thresholds": "80,90",
      "title": "内存使用率",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(50, 172, 45, 0.97)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(245, 54, 54, 0.9)"
      ],
      "datasource": "Prometheus",
      "decimals": null,
      "description": "通过变量maxmount获取最大的分区。",
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": true,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 5,
        "w": 4,
        "x": 16,
        "y": 0
      },
      "id": 154,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "minSpan": 4,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "repeat": null,
      "repeatDirection": "h",
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": true
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "100 - ((node_filesystem_avail_bytes{instance=~\"$node\",mountpoint=\"$maxmount\",fstype=~\"ext4|xfs\"} * 100) / node_filesystem_size_bytes {instance=~\"$node\",mountpoint=\"$maxmount\",fstype=~\"ext4|xfs\"})",
          "format": "time_series",
          "interval": "10s",
          "intervalFactor": 1,
          "refId": "A",
          "step": 20
        }
      ],
      "thresholds": "70,90",
      "title": "最大分区($maxmount)使用率",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "#299c46",
        "rgba(237, 129, 40, 0.89)",
        "#d44a3a"
      ],
      "datasource": "Prometheus",
      "format": "none",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 5,
        "w": 4,
        "x": 20,
        "y": 0
      },
      "id": 174,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": true
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "node_load1{instance=\"$node\"}",
          "format": "time_series",
          "hide": false,
          "instant": false,
          "intervalFactor": 1,
          "legendFormat": "1m",
          "refId": "A"
        }
      ],
      "thresholds": "1,4",
      "title": "系统平均负载（1m）",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "avg"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "decimals": null,
      "description": "",
      "format": "bytes",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 3,
        "w": 4,
        "x": 4,
        "y": 2
      },
      "id": 75,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "minSpan": 4,
      "nullPointMode": "null",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "70%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "node_memory_MemTotal_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "instant": true,
          "intervalFactor": 1,
          "legendFormat": "{{instance}}",
          "refId": "A",
          "step": 20
        }
      ],
      "thresholds": "",
      "title": "内存总量",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "aliasColors": {
        "Idle - Waiting for something to happen": "#052B51",
        "guest": "#9AC48A",
        "idle": "#052B51",
        "iowait": "#EAB839",
        "irq": "#BF1B00",
        "nice": "#C15C17",
        "sdb_每秒I/O操作%": "#d683ce",
        "softirq": "#E24D42",
        "steal": "#FCE2DE",
        "system": "#508642",
        "user": "#5195CE",
        "磁盘花费在I/O操作占比": "#ba43a9"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "decimals": 2,
      "description": "",
      "fill": 1,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 0,
        "y": 5
      },
      "id": 7,
      "legend": {
        "alignAsTable": true,
        "avg": true,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "rightSide": false,
        "show": true,
        "sideWidth": null,
        "sort": null,
        "sortDesc": null,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "minSpan": 4,
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "repeat": null,
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "avg(irate(node_cpu_seconds_total{instance=~\"$node\",mode=\"system\"}[1m]))",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "System",
          "refId": "A",
          "step": 20
        },
        {
          "expr": "avg(irate(node_cpu_seconds_total{instance=~\"$node\",mode=\"user\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "User",
          "refId": "B",
          "step": 240
        },
        {
          "expr": "avg(irate(node_cpu_seconds_total{instance=~\"$node\",mode=\"idle\"}[1m]))",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "Idle",
          "refId": "F",
          "step": 240
        },
        {
          "expr": "avg(irate(node_cpu_seconds_total{instance=~\"$node\",mode=\"iowait\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "Iowait",
          "refId": "D",
          "step": 240
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "CPU使用率（%）",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "transparent": false,
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": null,
          "format": "percentunit",
          "label": "",
          "logBase": 1,
          "max": "1",
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": false
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "15分钟": "#6ED0E0",
        "1分钟": "#BF1B00",
        "5分钟": "#CCA300"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {},
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 12,
        "y": 5
      },
      "height": "300",
      "id": 13,
      "legend": {
        "alignAsTable": true,
        "avg": true,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "rightSide": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "minSpan": 4,
      "nullPointMode": "null as zero",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "repeat": null,
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "node_load1{instance=~\"$node\"}",
          "format": "time_series",
          "instant": false,
          "interval": "10s",
          "intervalFactor": 2,
          "legendFormat": "1m",
          "metric": "",
          "refId": "A",
          "step": 20,
          "target": ""
        },
        {
          "expr": "node_load5{instance=~\"$node\"}",
          "format": "time_series",
          "instant": false,
          "interval": "10s",
          "intervalFactor": 2,
          "legendFormat": "5m",
          "refId": "B",
          "step": 20
        },
        {
          "expr": "node_load15{instance=~\"$node\"}",
          "format": "time_series",
          "instant": false,
          "interval": "10s",
          "intervalFactor": 2,
          "legendFormat": "15m",
          "refId": "C",
          "step": 20
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "系统平均负载",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "内存_Avaliable": "#6ED0E0",
        "内存_Cached": "#EF843C",
        "内存_Free": "#629E51",
        "内存_Total": "#6d1f62",
        "内存_Used": "#eab839"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "decimals": 2,
      "fill": 1,
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 14
      },
      "height": "300",
      "id": 156,
      "legend": {
        "alignAsTable": true,
        "avg": true,
        "current": true,
        "max": false,
        "min": false,
        "rightSide": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "node_memory_MemTotal_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "总内存",
          "refId": "A",
          "step": 4
        },
        {
          "expr": "node_memory_MemTotal_bytes{instance=~\"$node\"} - (node_memory_Cached_bytes{instance=~\"$node\"} + node_memory_Buffers_bytes{instance=~\"$node\"} + node_memory_MemFree_bytes{instance=~\"$node\"})",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "已用",
          "refId": "B",
          "step": 4
        },
        {
          "expr": "node_memory_MemFree_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "hide": true,
          "intervalFactor": 2,
          "legendFormat": "内存_Free",
          "refId": "C",
          "step": 4
        },
        {
          "expr": "node_memory_Buffers_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "hide": true,
          "intervalFactor": 2,
          "legendFormat": "内存_Buffers",
          "refId": "D",
          "step": 4
        },
        {
          "expr": "node_memory_Cached_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "hide": true,
          "intervalFactor": 2,
          "legendFormat": "内存_Cached",
          "refId": "E",
          "step": 4
        },
        {
          "expr": "node_memory_MemAvailable_bytes{instance=~\"$node\"}",
          "format": "time_series",
          "hide": false,
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "可用",
          "refId": "F",
          "step": 4
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "内存信息",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "decbytes",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "columns": [],
      "datasource": "Prometheus",
      "fontSize": "120%",
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 14
      },
      "id": 164,
      "links": [],
      "pageSize": null,
      "scroll": true,
      "showHeader": true,
      "sort": {
        "col": null,
        "desc": false
      },
      "styles": [
        {
          "alias": "Time",
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "pattern": "Time",
          "type": "hidden"
        },
        {
          "alias": "分区",
          "colorMode": null,
          "colors": [
            "rgba(50, 172, 45, 0.97)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(245, 54, 54, 0.9)"
          ],
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "decimals": 2,
          "mappingType": 1,
          "pattern": "mountpoint",
          "thresholds": [
            ""
          ],
          "type": "string",
          "unit": "bytes"
        },
        {
          "alias": "已用空间",
          "colorMode": null,
          "colors": [
            "rgba(245, 54, 54, 0.9)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(50, 172, 45, 0.97)"
          ],
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "decimals": 2,
          "mappingType": 1,
          "pattern": "Value #A",
          "thresholds": [
            "10000000000",
            "20000000000"
          ],
          "type": "number",
          "unit": "bytes"
        },
        {
          "alias": "使用率",
          "colorMode": "cell",
          "colors": [
            "rgba(50, 172, 45, 0.97)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(245, 54, 54, 0.9)"
          ],
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "decimals": 2,
          "mappingType": 1,
          "pattern": "Value #B",
          "thresholds": [
            "70",
            "90"
          ],
          "type": "number",
          "unit": "percentunit"
        },
        {
          "alias": "总空间",
          "colorMode": null,
          "colors": [
            "rgba(245, 54, 54, 0.9)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(50, 172, 45, 0.97)"
          ],
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "decimals": 1,
          "link": false,
          "mappingType": 1,
          "pattern": "Value #C",
          "thresholds": [],
          "type": "number",
          "unit": "bytes"
        },
        {
          "alias": "文件系统",
          "colorMode": null,
          "colors": [
            "rgba(245, 54, 54, 0.9)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(50, 172, 45, 0.97)"
          ],
          "dateFormat": "YYYY-MM-DD HH:mm:ss",
          "decimals": 2,
          "link": false,
          "mappingType": 1,
          "pattern": "fstype",
          "thresholds": [],
          "type": "number",
          "unit": "short"
        },
        {
          "alias": "",
          "colorMode": null,
          "colors": [
            "rgba(245, 54, 54, 0.9)",
            "rgba(237, 129, 40, 0.89)",
            "rgba(50, 172, 45, 0.97)"
          ],
          "decimals": 2,
          "pattern": "/.*/",
          "preserveFormat": true,
          "sanitize": false,
          "thresholds": [],
          "type": "hidden",
          "unit": "short"
        }
      ],
      "targets": [
        {
          "expr": "node_filesystem_size_bytes{instance=~'\''$node'\'',fstype=~\"ext4|xfs\"}-node_filesystem_avail_bytes {instance=~'\''$node'\'',fstype=~\"ext4|xfs\"}",
          "format": "table",
          "hide": false,
          "instant": true,
          "interval": "10s",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A"
        },
        {
          "expr": "node_filesystem_size_bytes{instance=~'\''$node'\'',fstype=~\"ext4|xfs\"}",
          "format": "table",
          "hide": false,
          "instant": true,
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "C"
        },
        {
          "expr": "1-(node_filesystem_free_bytes{instance=~'\''$node'\'',fstype=~\"ext4|xfs\"} / node_filesystem_size_bytes{instance=~'\''$node'\'',fstype=~\"ext4|xfs\"})",
          "format": "table",
          "hide": false,
          "instant": true,
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "B"
        },
        {
          "expr": "",
          "format": "table",
          "interval": "10s",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "D"
        }
      ],
      "title": "各分区可用空间",
      "transform": "table",
      "transparent": false,
      "type": "table"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 22
      },
      "height": "300",
      "id": 157,
      "legend": {
        "alignAsTable": true,
        "avg": true,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [
        {
          "alias": "/.*_out上传$/",
          "transform": "negative-Y"
        }
      ],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_network_receive_bytes_total{instance=~'\''$node'\'',device=~'\''$nic'\''}[5m])*8",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "{{device}}_进带宽(in)",
          "refId": "A",
          "step": 4
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "网络进带宽（in）",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "bps",
          "label": "上传（-）/下载（+）",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": false
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 22
      },
      "height": "300",
      "id": 178,
      "legend": {
        "alignAsTable": true,
        "avg": true,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [
        {
          "alias": "/.*_out上传$/",
          "transform": "negative-Y"
        }
      ],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_network_transmit_bytes_total{instance=~'\''$node'\'',device=~'\''$nic'\''}[5m])*8",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "{{device}}_出带宽(out)",
          "refId": "B",
          "step": 4
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "网络出带宽（out）",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "bps",
          "label": "上传（-）/下载（+）",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": false
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "vda_write": "#6ED0E0"
      },
      "bars": true,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "Reads completed: 每个磁盘分区每秒读完成次数\n\nWrites completed: 每个磁盘分区每秒写完成次数\n\nIO now 每个磁盘分区每秒正在处理的输入/输出请求数",
      "fill": 2,
      "gridPos": {
        "h": 8,
        "w": 8,
        "x": 0,
        "y": 30
      },
      "height": "300",
      "id": 161,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": false,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [
        {
          "alias": "/.*_读取$/",
          "transform": "negative-Y"
        }
      ],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_disk_reads_completed_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": false,
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "{{device}}_读取",
          "refId": "A",
          "step": 10
        },
        {
          "expr": "irate(node_disk_writes_completed_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "{{device}}_写入",
          "refId": "B",
          "step": 10
        },
        {
          "expr": "node_disk_io_now{instance=~\"$node\"}",
          "format": "time_series",
          "hide": true,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "{{device}}",
          "refId": "C"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "磁盘读写速率（IOPS）",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": null,
          "format": "iops",
          "label": "读取（-）/写入（+）I/O ops/sec",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "vda_write": "#6ED0E0"
      },
      "bars": true,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "Read bytes 每个磁盘分区每秒读取的比特数\nWritten bytes 每个磁盘分区每秒写入的比特数",
      "fill": 2,
      "gridPos": {
        "h": 8,
        "w": 8,
        "x": 8,
        "y": 30
      },
      "height": "300",
      "id": 168,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": false,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [
        {
          "alias": "/.*_读取$/",
          "transform": "negative-Y"
        }
      ],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_disk_read_bytes_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "{{device}}_读取",
          "refId": "A",
          "step": 10
        },
        {
          "expr": "irate(node_disk_written_bytes_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "{{device}}_写入",
          "refId": "B",
          "step": 10
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "磁盘读写容量大小",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": null,
          "format": "Bps",
          "label": "读取（-）/写入（+）",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": false
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "vda": "#6ED0E0"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "Read time ms 每个磁盘分区读操作花费的秒数\n\nWrite time ms 每个磁盘分区写操作花费的秒数\n\nIO time ms 每个磁盘分区输入/输出操作花费的秒数\n\nIO time weighted 每个磁盘分区输入/输出操作花费的加权秒数",
      "fill": 3,
      "gridPos": {
        "h": 8,
        "w": 8,
        "x": 16,
        "y": 30
      },
      "height": "300",
      "id": 160,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [
        {
          "alias": "/,*_读取$/",
          "transform": "negative-Y"
        }
      ],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_disk_io_time_seconds_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": true,
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "{{device}}",
          "refId": "A",
          "step": 10
        },
        {
          "expr": "irate(node_disk_io_time_weighted_seconds_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": true,
          "intervalFactor": 1,
          "legendFormat": "{{device}}_加权",
          "refId": "D"
        },
        {
          "expr": "irate(node_disk_read_time_seconds_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": false,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "{{device}}_读取",
          "refId": "B"
        },
        {
          "expr": "irate(node_disk_write_time_seconds_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "{{device}}_写入",
          "refId": "C"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "磁盘IO读写时间",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "s",
          "label": "读取（-）/写入（+）",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": false
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "node_disk_io_time_seconds_total：\n磁盘花费在输入/输出操作上的毫秒数。该值为累加值。（Milliseconds Spent Doing I/Os）\n\nirate(node_disk_io_time_seconds_total[1m])：\n计算每秒的速率：(last值-last前一个值)/时间戳差值，即：1秒钟内磁盘花费在I/O操作的时间占比。",
      "fill": 1,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 0,
        "y": 38
      },
      "id": 176,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "hideEmpty": true,
        "hideZero": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "irate(node_disk_io_time_seconds_total{instance=~\"$node\"}[1m])",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "{{device}}_每秒I/O操作%",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "磁盘每秒的I/O操作耗费时间（%）",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "TCP": "#6ED0E0"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "CurrEstab - 当前状态为 ESTABLISHED 或 CLOSE-WAIT 的 TCP 连接数\n\nActiveOpens - 已从 CLOSED 状态直接转换到 SYN-SENT 状态的 TCP 平均连接数(1分钟内)\n\nPassiveOpens - 已从 LISTEN 状态直接转换到 SYN-RCVD 状态的 TCP 平均连接数(1分钟内)\n\nTCP_alloc - 已分配（已建立、已申请到sk_buff）的TCP套接字数量\n\nTCP_inuse - 正在使用（正在侦听）的TCP套接字数量\n\nTCP_tw - 等待关闭的TCP连接数",
      "fill": 0,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 12,
        "y": 38
      },
      "height": "300",
      "id": 158,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "rightSide": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "node_netstat_Tcp_CurrEstab{instance=~'\''$node'\''}",
          "format": "time_series",
          "hide": false,
          "interval": "10s",
          "intervalFactor": 1,
          "legendFormat": "ESTABLISHED",
          "refId": "A",
          "step": 20
        },
        {
          "expr": "node_sockstat_TCP_tw{instance=~'\''$node'\''}",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "TCP_tw",
          "refId": "D"
        },
        {
          "expr": "irate(node_netstat_Tcp_ActiveOpens{instance=~'\''$node'\''}[1m])",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "ActiveOpens",
          "refId": "B"
        },
        {
          "expr": "irate(node_netstat_Tcp_PassiveOpens{instance=~'\''$node'\''}[1m])",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "PassiveOpens",
          "refId": "C"
        },
        {
          "expr": "node_sockstat_TCP_alloc{instance=~'\''$node'\''}",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "TCP_alloc",
          "refId": "E"
        },
        {
          "expr": "node_sockstat_TCP_inuse{instance=~'\''$node'\''}",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "TCP_inuse",
          "refId": "F"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "TCP 连接情况",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "transparent": false,
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    }
  ],
  "refresh": false,
  "schemaVersion": 16,
  "style": "dark",
  "tags": [
    "StarsL",
    "Prometheus"
  ],
  "templating": {
    "list": [
      {
        "allFormat": "glob",
        "allValue": null,
        "current": {
          "tags": [],
          "text": "prometheus",
          "value": "prometheus"
        },
        "datasource": "Prometheus",
        "definition": "",
        "hide": 0,
        "includeAll": false,
        "label": "分组名称",
        "multi": false,
        "multiFormat": "regex values",
        "name": "job",
        "options": [],
        "query": "label_values(up,job)",
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 1,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      },
      {
        "allFormat": "glob",
        "allValue": null,
        "current": {
          "text": "node-exporterhost:9100",
          "value": "node-exporterhost:9100"
        },
        "datasource": "Prometheus",
        "definition": "",
        "hide": 0,
        "includeAll": false,
        "label": "IP地址",
        "multi": false,
        "multiFormat": "regex values",
        "name": "node",
        "options": [],
        "query": "label_values(up{job=\"$job\"},instance)",
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 2,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      },
      {
        "allValue": null,
        "current": {
          "text": "/home",
          "value": "/home"
        },
        "datasource": "Prometheus",
        "definition": "",
        "hide": 2,
        "includeAll": false,
        "label": "",
        "multi": false,
        "name": "maxmount",
        "options": [],
        "query": "query_result(topk(1,sort_desc (max(node_filesystem_size_bytes{instance=~'\''$node'\'',fstype=~\"ext4|xfs\"}) by (mountpoint))))",
        "refresh": 1,
        "regex": "/.*\\\"(.*)\\\".*/",
        "skipUrlSync": false,
        "sort": 0,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      },
      {
        "allValue": null,
        "current": {
          "isNone": true,
          "text": "None",
          "value": ""
        },
        "datasource": "Prometheus",
        "definition": "",
        "hide": 2,
        "includeAll": false,
        "label": "网卡",
        "multi": false,
        "name": "nic",
        "options": [],
        "query": "query_result(node_network_up{interface=\"eth0\"})",
        "refresh": 1,
        "regex": "/interface=\"(\\S*)\",job/",
        "skipUrlSync": false,
        "sort": 0,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      }
    ]
  },
  "time": {
    "from": "now-15m",
    "to": "now"
  },
  "timepicker": {
    "now": true,
    "refresh_intervals": [
      "5s",
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ],
    "time_options": [
      "5m",
      "15m",
      "1h",
      "6h",
      "12h",
      "24h",
      "2d",
      "7d",
      "30d"
    ]
  },
  "timezone": "browser",
  "title": "1.主机基础监控(cpu，内存，磁盘，网络)",
  "version": 1
},
  "folderId": 0,
  "overwrite": false
}'



echo ""
echo "start create jvm dashboard"


curl -X POST \
  http://${GRAFANA_HOST}:3000/api/dashboards/db \
  -H 'Accept: application/json' \
  -H "Authorization: Bearer ${API_KEY}" \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 942fcc2f-e7e5-4945-9403-4248f2dbe29b' \
  -H 'cache-control: no-cache' \
  -d '{
  "dashboard": {
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "description": "Dashboard for Spring Boot 1.x apps using Micrometer and Prometheus",
  "editable": true,
  "gnetId": 7731,
  "graphTooltip": 0,
  "iteration": 1552124824231,
  "links": [],
  "panels": [
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 0
      },
      "id": 2,
      "panels": [],
      "repeat": null,
      "title": "Status",
      "type": "row"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "#299c46",
        "rgba(237, 129, 40, 0.89)",
        "#d44a3a"
      ],
      "datasource": "Prometheus",
      "format": "none",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 5,
        "x": 0,
        "y": 1
      },
      "id": 4,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "up{job=\"$job\"}",
          "format": "time_series",
          "instant": true,
          "intervalFactor": 1,
          "refId": "A"
        }
      ],
      "thresholds": "",
      "title": "Status",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "UP",
          "value": "1"
        },
        {
          "op": "=",
          "text": "DOWN",
          "value": "0"
        },
        {
          "op": "=",
          "text": "DOWN",
          "value": "null"
        }
      ],
      "valueName": "avg"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 8,
        "w": 19,
        "x": 5,
        "y": 1
      },
      "id": 12,
      "legend": {
        "avg": false,
        "current": false,
        "hideEmpty": false,
        "hideZero": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "nullPointMode": "null as zero",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "rate(http_server_requests_seconds_sum{job=\"$job\"}[5m])/rate(http_server_requests_seconds_count{job=\"$job\"}[5m])",
          "format": "time_series",
          "instant": false,
          "intervalFactor": 1,
          "legendFormat": "{{method}}-{{status}}-{{uri}}",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Mean response time",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "transparent": false,
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "s",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "#299c46",
        "rgba(237, 129, 40, 0.89)",
        "#d44a3a"
      ],
      "datasource": "Prometheus",
      "decimals": 1,
      "format": "s",
      "gauge": {
        "maxValue": 100000000,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 5,
        "x": 0,
        "y": 3
      },
      "id": 18,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "process_uptime_seconds{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 1,
          "refId": "A"
        }
      ],
      "thresholds": "",
      "timeShift": null,
      "title": "Up Time",
      "type": "singlestat",
      "valueFontSize": "100%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "#299c46",
        "rgba(237, 129, 40, 0.89)",
        "#d44a3a"
      ],
      "datasource": "Prometheus",
      "decimals": null,
      "format": "none",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 5,
        "x": 0,
        "y": 5
      },
      "id": 20,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(137, 15, 2, 0.50)",
        "full": true,
        "lineColor": "#890f02",
        "show": true
      },
      "tableColumn": "instance",
      "targets": [
        {
          "expr": "max_over_time(logback_events_total{job=\"$job\", level=\"error\"}[1h])",
          "format": "time_series",
          "instant": false,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A"
        }
      ],
      "thresholds": "",
      "title": "Errors in logs",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "max"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": false,
      "colors": [
        "#299c46",
        "rgba(237, 129, 40, 0.89)",
        "#d44a3a"
      ],
      "datasource": "Prometheus",
      "decimals": 0,
      "format": "none",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 5,
        "x": 0,
        "y": 7
      },
      "id": 21,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "50%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(193, 92, 23, 0.50)",
        "full": true,
        "lineColor": "#c15c17",
        "show": true
      },
      "tableColumn": "application",
      "targets": [
        {
          "expr": "max_over_time(logback_events_total{job=\"$job\", level=\"warn\"}[1h])",
          "format": "time_series",
          "instant": false,
          "intervalFactor": 1,
          "refId": "A"
        }
      ],
      "thresholds": "",
      "title": "Warnings in logs",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "avg"
    },
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 9
      },
      "id": 8,
      "panels": [],
      "title": "API stats",
      "type": "row"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 0,
        "y": 10
      },
      "id": 10,
      "legend": {
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 2,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "rate(http_server_requests_seconds_count{job=\"$job\"}[1m])",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "{{method}}-{{status}}-{{uri}}",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Requests per second",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 12,
        "y": 10
      },
      "id": 16,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null as zero",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "topk(10, sum by(uri, method) (rate(http_server_requests_seconds_count{job=\"$job\"}[1m])))",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Top 10 APIs",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 19
      },
      "id": 23,
      "panels": [],
      "title": "Tomcat",
      "type": "row"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 12,
        "x": 0,
        "y": 20
      },
      "id": 27,
      "legend": {
        "avg": false,
        "current": true,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(rate(http_server_requests_seconds_sum{job=\"$job\", status!~\"5..\"}[1m]))/sum(rate(http_server_requests_seconds_count{job=\"$job\", status!~\"5..\"}[1m]))",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "Average",
          "refId": "A"
        },
        {
          "expr": "max(http_server_requests_seconds_max{job=\"$job\", status!~\"5..\"})",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "Maximum",
          "refId": "B"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Requests duration",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "s",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {
        "HTTP": "#890f02",
        "HTTP - 5xx": "#bf1b00"
      },
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 6,
        "x": 12,
        "y": 20
      },
      "id": 29,
      "legend": {
        "avg": false,
        "current": true,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"$job\", status=~\"5..\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "5xx - Server Errors",
          "refId": "A"
        },
        {
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"$job\", status=~\"4..\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "4xx - Client Errors",
          "refId": "B"
        },
        {
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"$job\", status=~\"2..\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "2xx - Success",
          "refId": "C"
        },
        {
          "expr": "sum(rate(http_server_requests_seconds_count{job=\"$job\", status=~\"3..\"}[1m]))",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "3xx - Redirections",
          "refId": "D"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "HTTP Responses",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "decimals": null,
          "format": "ops",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "description": "",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 6,
        "x": 18,
        "y": 20
      },
      "id": 25,
      "legend": {
        "alignAsTable": false,
        "avg": false,
        "current": true,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(tomcat_threads_busy{job=\"$job\"})",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "Busy",
          "refId": "A"
        },
        {
          "expr": "sum(tomcat_threads_current{job=\"$job\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "In Use",
          "refId": "B"
        },
        {
          "expr": "sum(tomcat_threads_config_max{job=\"$job\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "Max Available",
          "refId": "C"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Tomcat - Threads",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 27
      },
      "id": 31,
      "panels": [],
      "title": "JVM - Memory",
      "type": "row"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "decimals": 2,
      "editable": true,
      "error": false,
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 8,
        "w": 4,
        "x": 0,
        "y": 28
      },
      "id": 37,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "70%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "100-(sum(jvm_memory_used_bytes{job=\"$job\"})*100/sum(jvm_memory_max_bytes{job=\"$job\"}))",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "",
          "refId": "A",
          "step": 14400
        }
      ],
      "thresholds": "10,30",
      "title": "Free memory",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 8,
        "w": 20,
        "x": 4,
        "y": 28
      },
      "id": 43,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "rightSide": true,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(jvm_memory_used_bytes{job=\"$job\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "used",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_committed_bytes{job=\"$job\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "committed",
          "refId": "B",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_max_bytes{job=\"$job\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "max",
          "refId": "C",
          "step": 2400
        },
        {
          "expr": "process_memory_vss_bytes{job=\"$job\"}",
          "format": "time_series",
          "hide": true,
          "intervalFactor": 2,
          "legendFormat": "vss",
          "metric": "",
          "refId": "D",
          "step": 2400
        },
        {
          "expr": "process_memory_rss_bytes{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "rss",
          "refId": "E",
          "step": 2400
        },
        {
          "expr": "process_memory_pss_bytes{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "pss",
          "refId": "F",
          "step": 2400
        },
        {
          "expr": "process_memory_swap_bytes{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "swap",
          "refId": "G",
          "step": 2400
        },
        {
          "expr": "process_memory_swappss_bytes{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "swappss",
          "refId": "H",
          "step": 2400
        },
        {
          "expr": "process_memory_pss_bytes{job=\"$job\"} + process_memory_swap_bytes{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "phys (pss+swap)",
          "refId": "I",
          "step": 2400
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "JVM Memory - Total",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "mbytes",
        "short"
      ],
      "yaxes": [
        {
          "format": "bytes",
          "label": "",
          "logBase": 1,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "decimals": 2,
      "editable": true,
      "error": false,
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 12,
        "x": 0,
        "y": 36
      },
      "id": 33,
      "interval": null,
      "links": [],
      "mappingType": 1,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "70%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "100-(sum(jvm_memory_used_bytes{job=\"$job\", area=\"heap\"})*100/sum(jvm_memory_max_bytes{job=\"$job\", area=\"heap\"}))",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "",
          "refId": "A",
          "step": 14400
        }
      ],
      "thresholds": "10,30",
      "title": "Free Heap",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        }
      ],
      "valueName": "current"
    },
    {
      "cacheTimeout": null,
      "colorBackground": false,
      "colorValue": true,
      "colors": [
        "rgba(245, 54, 54, 0.9)",
        "rgba(237, 129, 40, 0.89)",
        "rgba(50, 172, 45, 0.97)"
      ],
      "datasource": "Prometheus",
      "decimals": 2,
      "editable": true,
      "error": false,
      "format": "percent",
      "gauge": {
        "maxValue": 100,
        "minValue": 0,
        "show": false,
        "thresholdLabels": false,
        "thresholdMarkers": true
      },
      "gridPos": {
        "h": 2,
        "w": 12,
        "x": 12,
        "y": 36
      },
      "id": 35,
      "interval": null,
      "links": [],
      "mappingType": 2,
      "mappingTypes": [
        {
          "name": "value to text",
          "value": 1
        },
        {
          "name": "range to text",
          "value": 2
        }
      ],
      "maxDataPoints": 100,
      "nullPointMode": "connected",
      "nullText": null,
      "postfix": "",
      "postfixFontSize": "50%",
      "prefix": "",
      "prefixFontSize": "70%",
      "rangeMaps": [
        {
          "from": "null",
          "text": "N/A",
          "to": "null"
        },
        {
          "from": "-99999999999999999999999999999999",
          "text": "N/A",
          "to": "0"
        }
      ],
      "sparkline": {
        "fillColor": "rgba(31, 118, 189, 0.18)",
        "full": false,
        "lineColor": "rgb(31, 120, 193)",
        "show": false
      },
      "tableColumn": "",
      "targets": [
        {
          "expr": "100-(sum(jvm_memory_used_bytes{job=\"$job\", area=\"nonheap\"})*100/sum(jvm_memory_max_bytes{job=\"$job\", area=\"nonheap\"}))",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "",
          "refId": "A",
          "step": 14400
        }
      ],
      "thresholds": "10,30",
      "title": "Free Non-Heap",
      "type": "singlestat",
      "valueFontSize": "80%",
      "valueMaps": [
        {
          "op": "=",
          "text": "N/A",
          "value": "null"
        },
        {
          "op": "=",
          "text": "x",
          "value": ""
        }
      ],
      "valueName": "current"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 7,
        "w": 12,
        "x": 0,
        "y": 38
      },
      "id": 39,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "rightSide": true,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(jvm_memory_used_bytes{job=\"$job\", area=\"heap\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "used",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_committed_bytes{job=\"$job\", area=\"heap\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "committed",
          "refId": "B",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_max_bytes{job=\"$job\", area=\"heap\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "max",
          "refId": "C",
          "step": 2400
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "JVM Memory - Heap",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "mbytes",
        "short"
      ],
      "yaxes": [
        {
          "format": "bytes",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 7,
        "w": 12,
        "x": 12,
        "y": 38
      },
      "id": 41,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "rightSide": true,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "sum(jvm_memory_used_bytes{job=\"$job\", area=\"nonheap\"})",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "used",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_committed_bytes{job=\"$job\", area=\"nonheap\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "committed",
          "refId": "B",
          "step": 2400
        },
        {
          "expr": "sum(jvm_memory_max_bytes{job=\"$job\", area=\"nonheap\"})",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "max",
          "refId": "C",
          "step": 2400
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "JVM Memory - Non-Heap",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "mbytes",
        "short"
      ],
      "yaxes": [
        {
          "format": "bytes",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 45
      },
      "id": 45,
      "panels": [],
      "title": "JVM - Garbage Collection",
      "type": "row"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 8,
        "x": 0,
        "y": 46
      },
      "id": 47,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "rate(jvm_gc_pause_seconds_count{job=\"$job\"}[1m])",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 2,
          "legendFormat": "{{action}} ({{cause}})",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Collections",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "ops",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": "",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 8,
        "x": 8,
        "y": 46
      },
      "id": 49,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "rate(jvm_gc_pause_seconds_sum{job=\"$job\"}[1m])/rate(jvm_gc_pause_seconds_count{job=\"$job\"}[1m])",
          "format": "time_series",
          "hide": false,
          "instant": false,
          "intervalFactor": 1,
          "legendFormat": "avg {{action}} ({{cause}})",
          "refId": "A"
        },
        {
          "expr": "jvm_gc_pause_seconds_max{job=\"$job\"}",
          "format": "time_series",
          "hide": false,
          "instant": false,
          "intervalFactor": 1,
          "legendFormat": "max {{action}} ({{cause}})",
          "refId": "B"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Pause Durations",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "s",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": "",
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 7,
        "w": 8,
        "x": 16,
        "y": 46
      },
      "id": 51,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "rate(jvm_gc_memory_allocated_bytes_total{job=\"$job\"}[1m])",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "allocated",
          "refId": "A"
        },
        {
          "expr": "rate(jvm_gc_memory_promoted_bytes_total{job=\"$job\"}[1m])",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "promoted",
          "refId": "B"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Allocated/Promoted",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "bytes",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": "0",
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "collapsed": false,
      "gridPos": {
        "h": 1,
        "w": 24,
        "x": 0,
        "y": 53
      },
      "id": 53,
      "panels": [],
      "title": "JVM - Misc",
      "type": "row"
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 8,
        "w": 6,
        "x": 0,
        "y": 54
      },
      "id": 55,
      "legend": {
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "system_cpu_usage{job=\"$job\"}",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "system",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "process_cpu_usage{job=\"$job\"}",
          "format": "time_series",
          "hide": false,
          "intervalFactor": 1,
          "legendFormat": "process",
          "refId": "B"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "CPU",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "short",
        "short"
      ],
      "yaxes": [
        {
          "decimals": 1,
          "format": "percentunit",
          "label": "",
          "logBase": 1,
          "max": "1",
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 8,
        "w": 6,
        "x": 6,
        "y": 54
      },
      "id": 57,
      "legend": {
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "system_load_average_1m{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "System load average",
          "metric": "",
          "refId": "A",
          "step": 2400
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "System Load Average",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "short",
        "short"
      ],
      "yaxes": [
        {
          "decimals": 1,
          "format": "short",
          "label": "",
          "logBase": 1,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "decimals": 0,
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 8,
        "w": 6,
        "x": 12,
        "y": 54
      },
      "id": 59,
      "legend": {
        "alignAsTable": true,
        "avg": false,
        "current": true,
        "hideEmpty": false,
        "max": true,
        "min": false,
        "rightSide": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "jvm_threads_live{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "live",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "jvm_threads_daemon{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "daemon",
          "metric": "",
          "refId": "B",
          "step": 2400
        },
        {
          "expr": "jvm_threads_peak{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "peak",
          "refId": "C",
          "step": 2400
        },
        {
          "expr": "process_threads{job=\"$job\"}",
          "format": "time_series",
          "interval": "",
          "intervalFactor": 2,
          "legendFormat": "process",
          "refId": "D",
          "step": 2400
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "Threads",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "short",
        "short"
      ],
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    },
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "editable": true,
      "error": false,
      "fill": 1,
      "grid": {
        "leftLogBase": 1,
        "leftMax": null,
        "leftMin": null,
        "rightLogBase": 1,
        "rightMax": null,
        "rightMin": null
      },
      "gridPos": {
        "h": 8,
        "w": 6,
        "x": 18,
        "y": 54
      },
      "id": 61,
      "legend": {
        "avg": false,
        "current": true,
        "max": true,
        "min": false,
        "show": true,
        "total": false,
        "values": true
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "process_open_fds{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "open",
          "metric": "",
          "refId": "A",
          "step": 2400
        },
        {
          "expr": "process_max_fds{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "max",
          "metric": "",
          "refId": "B",
          "step": 2400
        },
        {
          "expr": "process_files_open{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "open",
          "refId": "C"
        },
        {
          "expr": "process_files_max{job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 2,
          "legendFormat": "max",
          "refId": "D"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "File Descriptors",
      "tooltip": {
        "msResolution": false,
        "shared": true,
        "sort": 0,
        "value_type": "cumulative"
      },
      "type": "graph",
      "x-axis": true,
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "y-axis": true,
      "y_formats": [
        "short",
        "short"
      ],
      "yaxes": [
        {
          "decimals": 0,
          "format": "short",
          "label": null,
          "logBase": 10,
          "max": null,
          "min": 0,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    }
  ],
  "refresh": false,
  "schemaVersion": 16,
  "style": "dark",
  "tags": [
    "java",
    "spring",
    "spring boot",
    "prometheus",
    "tomcat",
    "micrometer",
    "jvm"
  ],
  "templating": {
    "list": [
      {
        "allValue": null,
        "current": {
          "text": "proemtheusdemo",
          "value": "proemtheusdemo"
        },
        "datasource": "Prometheus",
        "definition": "",
        "hide": 0,
        "includeAll": false,
        "label": "Prometheus Job",
        "multi": false,
        "name": "job",
        "options": [],
        "query": "label_values(job)",
        "refresh": 2,
        "regex": "",
        "skipUrlSync": false,
        "sort": 1,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      }
    ]
  },
  "time": {
    "from": "now-15m",
    "to": "now"
  },
  "timepicker": {
    "refresh_intervals": [
      "5s",
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ],
    "time_options": [
      "5m",
      "15m",
      "1h",
      "6h",
      "12h",
      "24h",
      "2d",
      "7d",
      "30d"
    ]
  },
  "timezone": "",
  "title": "Spring Boot 1.x",
  "version": 1
},
  "folderId": 0,
  "overwrite": false
}'



echo ""
echo "start create customize dashboard"

curl -X POST \
  http://${GRAFANA_HOST}:3000/api/dashboards/db \
  -H 'Accept: application/json' \
  -H "Authorization: Bearer ${API_KEY}" \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
  "dashboard": {
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "description": "展示在业务代码中调用API上报的数据",
  "editable": true,
  "gnetId": null,
  "graphTooltip": 0,
  "iteration": 1552126723806,
  "links": [],
  "panels": [
    {
      "aliasColors": {},
      "bars": false,
      "dashLength": 10,
      "dashes": false,
      "datasource": "Prometheus",
      "fill": 1,
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 0,
        "y": 0
      },
      "id": 2,
      "legend": {
        "avg": false,
        "current": false,
        "max": false,
        "min": false,
        "show": true,
        "total": false,
        "values": false
      },
      "lines": true,
      "linewidth": 1,
      "links": [],
      "nullPointMode": "null",
      "percentage": false,
      "pointradius": 5,
      "points": false,
      "renderer": "flot",
      "seriesOverrides": [],
      "spaceLength": 10,
      "stack": false,
      "steppedLine": false,
      "targets": [
        {
          "expr": "my_sample_counter_total{status=\"success\", job=\"$job\"}",
          "format": "time_series",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A"
        }
      ],
      "thresholds": [],
      "timeFrom": null,
      "timeRegions": [],
      "timeShift": null,
      "title": "my_sample_counter曲线图",
      "tooltip": {
        "shared": true,
        "sort": 0,
        "value_type": "individual"
      },
      "type": "graph",
      "xaxis": {
        "buckets": null,
        "mode": "time",
        "name": null,
        "show": true,
        "values": []
      },
      "yaxes": [
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        },
        {
          "format": "short",
          "label": null,
          "logBase": 1,
          "max": null,
          "min": null,
          "show": true
        }
      ],
      "yaxis": {
        "align": false,
        "alignLevel": null
      }
    }
  ],
  "schemaVersion": 16,
  "style": "dark",
  "tags": [
    "business",
    "java"
  ],
  "templating": {
    "list": [
      {
        "allValue": null,
        "current": {
          "text": "proemtheusdemo",
          "value": "proemtheusdemo"
        },
        "datasource": "Prometheus",
        "definition": "label_values(job)",
        "hide": 0,
        "includeAll": false,
        "label": "请选择应用",
        "multi": false,
        "name": "job",
        "options": [],
        "query": "label_values(job)",
        "refresh": 2,
        "regex": "",
        "skipUrlSync": false,
        "sort": 0,
        "tagValuesQuery": "",
        "tags": [],
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      }
    ]
  },
  "time": {
    "from": "now-15m",
    "to": "now"
  },
  "timepicker": {
    "refresh_intervals": [
      "5s",
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ],
    "time_options": [
      "5m",
      "15m",
      "1h",
      "6h",
      "12h",
      "24h",
      "2d",
      "7d",
      "30d"
    ]
  },
  "timezone": "",
  "title": "业务自定义监控",
  "version": 6
},
  "folderId": 0,
  "overwrite": false
}'
