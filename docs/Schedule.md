# 日期：2026.04.02(四)
## 大方向：修改東西皆用 Restful API，改成功後，後端會用 web socket 傳遞訊息給前端，說明要動哪些東西。
### 前端要做的事情：
- [x] 每五秒版面不固定的問題 (D)
- [x] 自定義滑鼠右鍵(或改成其他按鍵移動)  (J)
- [x] Frame (D)
- [ ] domain model  (J)

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
- [ ] Domain Model (J)
- [x] 旋轉問題 => 刪掉這個功能 (J)
- [ ] Frame、Frame 互相重疊的問題 (D)
- [ ] STOMP 完整推播功能串接與更新 (D)
- [ ] 滑鼠右鍵 (J)
- [ ] 功能列表：複製、貼上、剪下、上一步、下一步、置頂、置底、上一層、下一層

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