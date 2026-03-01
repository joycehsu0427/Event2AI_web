# User 與 Auth 關係簡述

## 目的
說明 `user` 模組與 `auth` 模組在目前專案中的責任分工，以及兩者如何協作。

## 模組分工
### Auth（`/api/auth`）
- 負責註冊與登入。
- 驗證帳號密碼，簽發 JWT access token。
- 典型端點：
  - `POST /api/auth/register`
  - `POST /api/auth/login`

### User（`/api/users`）
- 負責使用者資料 CRUD。
- 管理使用者資料（查詢、更新、刪除、建立）。
- 典型端點：
  - `GET /api/users`
  - `GET /api/users/{id}`
  - `GET /api/users/username/{username}`
  - `POST /api/users`
  - `PUT /api/users/{id}`
  - `DELETE /api/users/{id}`

## 兩者關係（流程）
1. Client 先呼叫 `auth` 端點完成註冊或登入。
2. 取得 access token（Bearer JWT）。
3. Client 帶著 token 呼叫受保護 API（包含 `user` 相關端點）。

## 目前權限現況（重要）
- 目前安全設定為：`/api/auth/**` 開放匿名，其他 API 需登入。
- 這代表 `/api/users/**` 現況是「已登入即可呼叫」，尚未做 admin/user 的細粒度角色限制。

## 結論
- `auth` 是身份驗證與 token 發放入口。
- `user` 是使用者資料管理模組。
- 在概念上，`user` 常見於後台管理；但在目前程式實作中，尚未限制為「僅管理員」可用。
