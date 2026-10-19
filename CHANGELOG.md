# Changelog

## 0.1.0 — 2026-10-19

First release.

- `emails().send`, `batch().send`, `emails().get`, `emails().list`, `emails().all`, `emails().stats`, with Reply-To and attachments.
- Templates (with tags and image assets), domains, sandboxes (including the JSON door and `waitFor`), webhooks and their dispatches, contacts, audiences, suppressions, mailboxes, automations, workspace and API keys.
- Typed params (builders) and responses; every failure is a `SendClipperException` whose `getName()` is the API's code.
- Automatic `Idempotency-Key` on every JSON write; retries on network errors, 429 and 5xx, honouring `Retry-After`.
- `Webhook.verify()` for `X-SendClipper-Signature-V2`, rotation-aware.
- Lists that span pages have `all()`, a lazy `PageIterable` you can loop over or `stream()`.
- Java 11 or newer; depends on Jackson only.
