# Parse NDJSON Stream Contract 1.1

Every line is one JSON object. Existing consumers may continue reading only `type` and `content`.

```json
{
  "schemaVersion": "1.1.0",
  "type": "message",
  "event": "file_result",
  "content": "✅ [3/12] order/cart.c — 정확히 파싱됐어요 (C · 287줄)",
  "phase": "PARSING",
  "status": "COMPLETED",
  "current": 3,
  "total": 12,
  "percent": 25,
  "file": "shop_mall/order/cart.c",
  "language": "c",
  "quality": "EXACT"
}
```

## Stable lifecycle names

- `run_started`
- `intake_started`, `intake_completed`, `file_skipped`
- `language_detected`
- `file_started`, `file_progress`, `file_result`
- `quality_summary`, `run_completed`
- `run_failed`
- terminal `complete` (`type=complete`) exactly once on normal completion

`counts` is an optional object with integer values such as `exact`, `recovered`, `partial`,
`reviewRequired`, `unresolvedOrFailed`, `astFiles`, and `lines`.

