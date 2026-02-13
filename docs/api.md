# StickyNote 有關之 API

## 取得所有 StickyNote
```
GET /api/sticky-notes
```


## 建立新的 StickyNote
```
POST /api/sticky-notes
```
- 需要的 json request：
```json
{
    "posX": 100.5,
    "posY": 200.3,
    "geoX": 25.0330,
    "geoY": 121.5654,
    "description": "這是一個便利貼",
    "color": "yellow",
    "tag": "重要"
  }
```
- 回傳：
```json
HTTP/1.1 201 Created
Content-Type: application/json

  {
    "id": "stickyNoteId",
    "posX": 100.5,
    "posY": 200.3,
    "geoX": 25.0330,
    "geoY": 121.5654,
    "description": "這是一個便利貼",
    "color": "yellow",
    "tag": "重要"
  }
```
## 用 id 更新 StickyNote
```
PUT /api/sticky-notes/{id}
```
- 在斜線後方加上 id
- 需要的 json request (跟 create 一樣格式，但可以只放要更新的 attribute )
- 但 [posX、posY]、[geoX、geoY] 兩者皆須同時出現
```json
{
    "posX": 100.5,
    "posY": 200.3
}
```
或者
```json
{
    "color": "yellow"
  }
```
- (第一個範例)回傳：
```json
HTTP/1.1 200
Content-Type: application/json
  {
    "id": "stickyNoteId",
    "posX": 100.5,
    "posY": 200.3
  }
```

## 用 id 刪掉 StickyNote
```
DELETE /api/sticky-notes/{id}
```
- 在斜線後方加上 id
- 不需要的 json request
- 回傳：
```
 HTTP/1.1 204 No Content
```

