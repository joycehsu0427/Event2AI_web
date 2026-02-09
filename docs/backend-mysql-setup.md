# 後端連線 MySQL 資料庫 - 教學文件

---

## Step 1：在 MySQL 建立資料庫

在 MySQL Workbench，執行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS event2ai
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

> 創建 MySQL 的 Database => event2ai

---

## Step 2：在 `build.gradle` 加入依賴 (Ian 已修改完成，這步驟可以忽略)

**檔案位置：** `backend/build.gradle`

在 `dependencies` 區塊中加入以下兩行：

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'  // 新增：JPA + Hibernate
    runtimeOnly 'com.mysql:mysql-connector-j'                               // 新增：MySQL 驅動
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

| 依賴 | 用途 |
|------|------|
| `spring-boot-starter-data-jpa` | Spring Data JPA + Hibernate ORM |
| `mysql-connector-j` | MySQL JDBC 驅動程式 |

---

## Step 3：設定資料庫連線

### 3-1：設定環境變數(帳號密碼)
> `${DB_USERNAME}` 和 `${DB_PASSWORD}` 會在啟動時從環境變數讀取，不會寫死在程式碼裡。

**檔案位置：** `backend/.env`

修改此檔案的以下內容：

```env
DB_USERNAME=root
DB_PASSWORD=你自己的MySQL密碼
```

> ⚠️ **這個檔案已加入 `.gitignore`，不會被 commit 到 Git，所以每位同學的密碼都是私人的。**

### 3-2：在 IntelliJ 設定環境變數

因為 Spring Boot 不會自動讀取 `.env` 檔案，需要在 IntelliJ 設定：

1. 點擊上方工具列的 **Run**
2. 選擇 **Edit Configurations...**
3. 找到或新增 `Event2AiBackendApplication`
4. 點開右側`Modify Options`，點選 **Environment variables** 欄位
5. 新增兩筆：
   - `DB_USERNAME` = `root`
   - `DB_PASSWORD` = `你的密碼`
6. 點 **OK** 儲存

### 各設定說明

| 設定 | 說明 |
|------|------|
| `spring.datasource.url` | MySQL 連線網址，包含資料庫名稱和參數 |
| `useSSL=false` | 本機開發不需要 SSL |
| `allowPublicKeyRetrieval=true` | 允許 MySQL 8+ 的公鑰驗證 |
| `serverTimezone=Asia/Taipei` | 設定為台灣時區 |
| `spring.jpa.hibernate.ddl-auto=update` | 開發階段自動同步 Entity 到資料表 |
| `spring.jpa.show-sql=true` | 在 console 印出 SQL 語句，方便除錯 |
| `spring.jpa.properties.hibernate.format_sql=true` | 格式化印出的 SQL，更易讀 |


---

## Step 4：同步 Gradle 並啟動

1. 回到 IntelliJ，按 **Ctrl + Shift + A** → 輸入 **「Reload All Gradle Projects」** → Enter
2. 等待右下角進度條跑完（Gradle 會下載新的依賴）
3. 開啟 `Event2AiBackendApplication.java`，點擊 `main` 方法旁的 **▶ 綠色按鈕** 啟動

---

## Step 5：驗證連線成功

啟動後，在 IntelliJ 下方的 **Console** 視窗中，如果看到類似以下訊息，代表連線成功：

```
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@xxxxxxxx
HikariPool-1 - Start completed.
```

### 常見錯誤排查

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `Communications link failure` | MySQL 沒有啟動 | 啟動 MySQL 服務 |
| `Access denied for user 'root'` | 密碼錯誤 | 檢查 `application.properties` 中的密碼 |
| `Unknown database 'event2ai'` | 資料庫不存在 | 執行 Step 1 的 SQL 建立資料庫 |
| `Public Key Retrieval is not allowed` | 缺少連線參數 | 確認 URL 中有 `allowPublicKeyRetrieval=true` |
