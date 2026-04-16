# 日期：2026.04.02(四)
## 大方向：修改東西皆用 Restful API，改成功後，後端會用 web socket 傳遞訊息給前端，說明要動哪些東西。
### 前端要做的事情：
- [x] 每五秒版面不固定的問題 (D)
- [x] 自定義滑鼠右鍵(或改成其他按鍵移動)  (J)
- [x] Frame (D)
- [x] domain model  (J)

### 後端要做的事情：
- [x] STOMP (B)
- [x] 套用 Event2AI 的演算法 (決定採用複製貼上) (I)
- [x] Frame (I、B)
- [x] domain model (entity、line) 的 API (I、B)

## deadline: 0409(四)

---

# 日期：2026.04.09(四)
## Frontend：
- [x] 串接 Event2AI 的按鈕、Board 畫面中，回首頁的按鈕 (D)
- [x] Create StickyNote 時，視窗看的到地方 (要將 StickyNote 先黏在滑鼠上) (D)
- [x] 顏色加上文字敘述 (J)
- [x] Domain Model (J)
- [x] 旋轉問題 => 刪掉這個功能 (J)
- [x] Frame、Frame 互相重疊的問題 (D)
- [x] 滑鼠右鍵 (J)
- [x] 功能列表：複製、貼上、剪下、上一步、下一步、置頂、置底、上一層、下一層

## Backend：
- [x] STOMP 完整推播功能 (B)
- [x] 測試 Event2AI & 怎麼方便使用者拿到檔案 (I)
- [x] Create 一組 EventStorming 的 Api (I)
- [x] 帳號密碼API錯誤訊息回傳 (I)
- [x] 將 DomainEntity 改成 DomainModelItem，修改可以符合下圖的 attribute、API (I、B)


## Future ：
- EventStorming 內部可以直接用點擊的方式編輯參數，而非打字上去(像舊版那樣在 description 上打 attribute 要長怎樣)
- 跟教授約：0416(四) => 改至 0417(五) 下午15:10以後
## DeadLine : 0414(二)8PM、0415(三)午餐結束各開一次會

---

# 日期：2026.04.14(二)
## Frontend：
- [x] 修正 `Frame` (D) 
- [v] 修正 `DomainModelItem` (J)
- [x] Create 一組 EventStorming Template 的按鈕 (D)
- [v] STOMP 完整推播功能串接與更新 (D)
- [x] 功能列表：複製、貼上、剪下、上一步、下一步 (J)
- [v] `Analsis` 的按鈕 + 壓縮 JsonFile (D)
- [ ] `Connector` 的箭頭折角 (J)
- [ ] 功能列表：置頂、置底、上一層、下一層 (J)
- [ ] EventStorming 內部可以直接用點擊的方式編輯參數 (J)
- [x] 修正 Create `Rectangle` 創立時的大小問題 (D)
- [x] 修正 `Rectangle` 的縮放問題 (D)
- [ ] 更改關於 `Frame`、`Connector` Z軸的相對關係 
- [x] 更改關於 `Frame`、`DomainModelItem` 的 API，ex：`DomainModelItem` 移進 `Frame` 中後，需要發API改變 `FrameId`

## Backend：
- [x] 加上 `WebSocket` 的 `Message` 中缺少的 `UserId` (B)
- [v] `Board` DELETE 會報錯 (B)
- [x] `DomainModel` 的 `ENUM` 在 Create 時會有問題 (I)
- [x] 整理 test-api (I)
- [ ] `Connector` 的箭頭折角 (B、I)
- [x] 將 `DomainModelItem` 新增 `FrameId` (B)
- [x] 將 `DomainModelItem` 的 API 修改成可以更新 `FrameId` (I)
- [x] 將所有 component 新增紀錄 Z 軸的參數 (B)
- [x] 新增 Z軸 的 API (I)

## DeadLine : 0416(四) AM 11:00


---

# 日期：0416(四)
## Frontend：
- [ ] `Connector` 的 Z-index 要在最上面 ()

## Backend：
- [x] GetAllComponents 的 `Connector` 沒有回傳 (I)
- [ ] CreateStickyNote 時，多了一個 `Tag` (I)
- [ ] `Connector` 的 offset 意義 (B)
- [ ] `Connector` 的 WS 推播 (B)

## 報告重點：
- 前端展示
    - 分享 board 的測試
    - Analysis 的按鈕
    - Template 的按鈕
    - domain model 繪製測試