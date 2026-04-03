# Analysis 有關之 API

## 執行 Event Storming 分析

```
POST /api/analysis
```

- 對指定的 Board 執行 Event Storming 分析
- 會將同一個 Frame 內的 StickyNote 視為一個 Group，並依照顏色分類出 UseCase、Input、Aggregate 等角色
- 分析完成後，結果會自動存成 JSON 檔案至 Server 端的 `ToAIJsonFile/{boardTitle}/` 目錄下
- 每個 Group（UseCase）各自存成一個獨立的 JSON 檔案，以 UseCase 名稱命名

- 需要的 json request：
```json
{
    "boardId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

- 回傳：
```
HTTP/1.1 200 OK
```

- 錯誤情況：
```
HTTP/1.1 400 Bad Request

"User is not a member of this board"
```

---

## 分析結果說明

### 存檔位置
```
ToAIJsonFile/
└── {boardTitle}/
    ├── {useCaseName1}.json
    ├── {useCaseName2}.json
    └── ...
```

### 單一 JSON 檔案格式
```json
{
    "groupId": "stickyNoteId-of-usecase",
    "useCaseName": "CreateOrder",
    "aggregateName": "Order",
    "method": "Order createOrder",
    "actor": ["User", "Admin"],
    "comment": ["這裡有一些備註"],
    "input": [
        { "name": "orderId", "type": "String" },
        { "name": "amount", "type": "int" }
    ],
    "domainEvents": [
        {
            "eventName": "OrderCreated",
            "reactor": "OrderService",
            "policy": "NotifyUser",
            "attributes": [
                { "name": "orderId", "type": "String", "constraint": "NotNull" }
            ]
        }
    ],
    "aggregateWithAttributes": [
        {
            "aggregateName": "Order",
            "attributes": [
                { "name": "id", "type": "String", "constraint": "NotNull" },
                { "name": "amount", "type": "int", "constraint": "Positive" }
            ]
        }
    ]
}
```

---

## StickyNote 顏色對應規則

| 顏色 | 對應角色 |
|------|----------|
| `blue` | UseCase（每組必須有一張） |
| `green` | Input |
| `light_yellow` | Aggregate Name |
| `yellow` | Actor |
| `gray` | Comment |
| `orange` | Domain Event 名稱 |
| `light_blue` | Domain Event Reactor |
| `violet` | Domain Event Policy |
| `light_green` | Domain Event Attributes |
| `dark_green` | Aggregate with Attributes |
| `pink` | Method |
