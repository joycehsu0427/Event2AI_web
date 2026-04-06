# 前端 STOMP 訂閱說明

這份文件只說明前端如何在進入 `board` 後建立 WebSocket/STOMP 連線並訂閱對應的 topic。

目前已確定的是：
- 後端 WebSocket endpoint：`/ws`
- STOMP broker prefix：`/topic`
- board topic：`/topic/boards/{boardId}` 或 `/topic/boards/{boardId}/events`
- `CONNECT` 時必須帶 `Authorization` header
- 只有該 `board` 的成員可以訂閱

目前還沒定案的是：
- 後端推播的 event payload 格式
- 前端收到訊息後如何更新 `store`

所以這份文件先聚焦在「怎麼連上去」。

## 建議流程

進入 `board` 後，前端建議流程如下：

1. 先用既有 REST API 抓初始資料
2. 建立 WebSocket/STOMP 連線
3. 訂閱該 `boardId` 對應的 topic
4. 離開 `board` 頁面時取消訂閱並斷線

建議順序：

```text
GET /api/boards/{boardId}/components
-> connect /ws
-> subscribe /topic/boards/{boardId}
```

這樣做可以避免一進頁面就只靠即時訊息，卻拿不到初始畫面。

## 前端套件

目前 `frontend/package.json` 還沒有 STOMP client 套件。

建議安裝：

```bash
npm install @stomp/stompjs
```

目前後端 `WebSocketConfig` 只註冊了原生 WebSocket endpoint，沒有開 SockJS，所以前端先用原生 WebSocket 版本即可，不需要 `sockjs-client`。

## 後端連線規格

後端設定位置：
- `backend/src/main/java/event/to/ai/backend/websocket/WebSocketConfig.java`
- `backend/src/main/java/event/to/ai/backend/websocket/interceptor/StompJwtConnectChannelInterceptor.java`
- `backend/src/main/java/event/to/ai/backend/websocket/interceptor/BoardSubscriptionAuthorizationChannelInterceptor.java`

前端要配合的重點：

- 連線 URL：`ws://localhost:8080/ws`
- `CONNECT` 時帶 `Authorization: Bearer <token>`
- 訂閱路徑：
  - `"/topic/boards/" + boardId`
  - 或 `"/topic/boards/" + boardId + "/events"`

目前後端兩種都接受，但前端團隊最好固定只選一種，避免後續混亂。

建議先統一使用：

```text
/topic/boards/{boardId}/events
```

## Vue 接線位置

目前最適合的接線點是：

- `frontend/src/views/BoardView.vue`

因為這個頁面已經有：
- `boardId`
- 初始 `fetchBoardData(boardId)`
- `onMounted`
- `onUnmounted`

也就是說，WebSocket/STOMP 的生命週期應該跟 `BoardView` 綁在一起。

## 範例程式

以下是最小可行的 Vue 寫法，先展示連線與訂閱，不先處理 payload 細節。

```ts
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import { onMounted, onUnmounted, ref } from 'vue'

const stompClient = ref<Client | null>(null)
let boardSubscription: StompSubscription | null = null

function connectBoardTopic(boardId: string) {
  const token = localStorage.getItem('token')
  if (!token) {
    console.error('Missing JWT token')
    return
  }

  const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    onConnect: () => {
      boardSubscription = client.subscribe(
        `/topic/boards/${boardId}/events`,
        (message: IMessage) => {
          console.log('board event:', message.body)
          // TODO: 等 payload 格式定案後再 parse / 更新 store
        },
      )
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body)
    },
    onWebSocketError: (event) => {
      console.error('WebSocket error:', event)
    },
  })

  client.activate()
  stompClient.value = client
}

function disconnectBoardTopic() {
  boardSubscription?.unsubscribe()
  boardSubscription = null
  stompClient.value?.deactivate()
  stompClient.value = null
}

onMounted(() => {
  connectBoardTopic(boardId)
})

onUnmounted(() => {
  disconnectBoardTopic()
})
```

## 與目前 `BoardView` 的整合建議

可以先保留既有流程：

```ts
onMounted(() => {
  fetchBoardData(boardId)
  connectBoardTopic(boardId)
})

onUnmounted(() => {
  disconnectBoardTopic()
})
```

目前 `BoardView.vue` 還有 polling。

建議分兩階段：

1. 先保留 polling，確認 STOMP 連線穩定
2. 等後端 event publisher 與 payload 格式定案後，再評估拿掉 polling

這樣比較安全，不會因為即時事件還沒完整接上就讓畫面失去同步能力。

## 前端收到訊息後先做什麼

在 event payload 尚未定案前，建議先做兩件事：

1. `console.log(message.body)` 確認後端實際送出的內容
2. 保留一個集中處理函式，例如 `handleBoardEvent(message.body)`

例如：

```ts
function handleBoardEvent(rawBody: string) {
  console.log('incoming board event:', rawBody)
}
```

等你們把 payload 格式談定，再把：
- `JSON.parse`
- `switch(type)`
- `boardStore` 更新邏輯

接進去即可。

## 常見失敗原因

### 1. `CONNECT` 沒帶 `Authorization`

後端會拒絕 STOMP `CONNECT`。

請確認：

```ts
connectHeaders: {
  Authorization: `Bearer ${token}`,
}
```

### 2. `boardId` 格式不對

topic 路徑裡的 `boardId` 必須是 UUID。

例如：

```text
/topic/boards/123
```

這種會被拒絕。

### 3. 使用者不是 board member

即使 token 合法，只要不是該 `board` 成員，訂閱也會被拒絕。

### 4. topic 路徑不一致

後端目前接受：
- `/topic/boards/{boardId}`
- `/topic/boards/{boardId}/events`

但前端若不同地方混用，後續會很難維護。建議整個前端固定只用 `/topic/boards/{boardId}/events`。

## 現階段建議

前端目前先完成這三件事就夠了：

1. 進入 `BoardView` 時建立 STOMP 連線
2. 訂閱 `/topic/boards/{boardId}/events`
3. 離開頁面時取消訂閱並斷線

等後端 event publisher 與 payload 格式定案後，再補：
- `message.body` 解析
- `boardStore` 更新
- 移除 polling
