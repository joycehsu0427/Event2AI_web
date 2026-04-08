# 前端 STOMP 訂閱說明

這份文件只說明前端如何在進入 `board` 後建立 WebSocket/STOMP 連線並訂閱對應的 topic。

目前已確定的是：
- 後端 WebSocket endpoint：`/ws`
- STOMP broker prefix：`/topic`
- board topic：`/topic/boards/{boardId}/events`
- `CONNECT` 時必須帶 `Authorization` header
- 只有該 `board` 的成員可以訂閱

目前已選定作法一：
- 所有即時訊息都走單一 board topic
- 訊息外層使用統一 event envelope
- `payload` 直接放既有 REST DTO

目前還沒完全定案的是：
- 第一批要先推哪些事件類型
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
-> subscribe /topic/boards/{boardId}/events
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

```text
/topic/boards/{boardId}/events
```

## Event 格式

作法一採用單一 topic + 統一 event envelope。

建議格式：

```json
{
  "type": "sticky-note.updated",
  "boardId": "b1111111-1111-1111-1111-111111111111",
  "payload": {
    "id": "s1111111-1111-1111-1111-111111111111",
    "boardId": "b1111111-1111-1111-1111-111111111111",
    "frameID": null,
    "posX": 100,
    "posY": 200,
    "geoX": 150,
    "geoY": 150,
    "description": "New Sticky Note",
    "color": "#ffeb3b",
    "tag": "sticky-note",
    "fontColor": "#000000",
    "fontSize": "20"
  }
}
```

規則如下：
- `type`：事件類型，例如 `sticky-note.created`
- `boardId`：這筆事件所屬的 board
- `payload`：直接放既有 DTO

也就是說：
- `sticky-note.created` / `sticky-note.updated` 的 `payload` 直接放 `StickyNoteDTO`
- `text-box.created` / `text-box.updated` 的 `payload` 直接放 `TextBoxesDTO`
- `frame.created` / `frame.updated` 的 `payload` 直接放 `FrameDTO`

刪除事件建議先用最小格式：

```json
{
  "type": "sticky-note.deleted",
  "boardId": "b1111111-1111-1111-1111-111111111111",
  "payload": {
    "id": "s1111111-1111-1111-1111-111111111111"
  }
}
```

## 建議第一批事件類型

- `sticky-note.created`
- `sticky-note.updated`
- `sticky-note.deleted`
- `text-box.created`
- `text-box.updated`
- `text-box.deleted`
- `frame.created`
- `frame.updated`
- `frame.deleted`

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
          handleBoardEvent(message.body)
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

## 收到訊息後的基本處理方式

既然作法一已經確定，前端可以直接用 `type` 分流：

```ts
function handleBoardEvent(rawBody: string) {
  const event = JSON.parse(rawBody)

  switch (event.type) {
    case 'sticky-note.created':
    case 'sticky-note.updated':
      console.log('sticky note dto:', event.payload)
      break
    case 'sticky-note.deleted':
      console.log('deleted sticky note id:', event.payload.id)
      break
    case 'text-box.created':
    case 'text-box.updated':
      console.log('text box dto:', event.payload)
      break
    case 'frame.created':
    case 'frame.updated':
      console.log('frame dto:', event.payload)
      break
    default:
      console.warn('unknown board event type:', event.type)
  }
}
```

現階段可以先做到：
- 成功 parse event
- 用 `switch(event.type)` 分流
- 暫時先 `console.log`

等後端 publisher 接好之後，再把 `boardStore` 的 patch 邏輯補進去。

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

前端應固定只用：

```text
/topic/boards/{boardId}/events
```

## 現階段建議

前端目前先完成這三件事就夠了：

1. 進入 `BoardView` 時建立 STOMP 連線
2. 訂閱 `/topic/boards/{boardId}/events`
3. 離開頁面時取消訂閱並斷線

之後再補：
- `message.body` 解析
- `switch(type)` 更新 `boardStore`
- 等即時同步穩定後移除 polling
