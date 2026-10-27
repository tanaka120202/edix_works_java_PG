import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.io.IOException; // HTTP処理の入出力例外を扱います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。
import java.net.URLDecoder; // URLで変換された文字を元に戻します。
import java.nio.charset.StandardCharsets; // UTF-8の文字コードを指定します。
import java.sql.Connection; // SQLiteとの接続を扱います。★
import java.sql.DriverManager; // SQLiteへの接続を作ります。★
import java.sql.PreparedStatement; // SQLを安全に実行します。★
import java.sql.ResultSet; // SELECT結果を読み取ります。★
import java.sql.SQLException; // データベース例外を扱います。★
import java.sql.Statement; // テーブル作成SQLを実行します。★

public class App { // Appクラスを定義します。
    private static final String DB_URL = "jdbc:sqlite:todos.db"; // SQLiteデータベースの接続先です。★
    private static Connection connection; // アプリ全体で使うSQLite接続です。★

    private static void initializeDatabase() throws SQLException { // 接続してテーブルを用意します。★
        connection = DriverManager.getConnection(DB_URL); // todos.dbへ接続します。★
        try (Statement statement = connection.createStatement()) { // テーブル作成用のSQL実行器を用意します。★
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // todos表を必要な場合だけ作ります。★
        } // SQL実行器を閉じます。★
    } // DB初期化を終えます。★

    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        initializeDatabase(); // 起動時にSQLiteへ接続してtodos表を用意します。★
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。
        server.createContext("/api/todos", exchange -> { // JSONのTodo一覧入口を追加します。
            if (!exchange.getRequestMethod().equals("GET")) { // GET以外の要求か確認します。
                exchange.sendResponseHeaders(405, -1); // 許可されない方法を返します。
                exchange.close(); // 応答を閉じます。
                return; // このリクエストの処理を終えます。
            } // HTTP方法の確認を終えます。
            try { // 一覧取得時のSQL例外を処理します。
                byte[] body = jsonTodos().getBytes(StandardCharsets.UTF_8); // Todo一覧をUTF-8のJSONにします。
                exchange.getResponseHeaders().set("Content-Type", "application/json"); // JSON形式を指定します。
                exchange.sendResponseHeaders(200, body.length); // 正常応答とデータ長を伝えます。
                exchange.getResponseBody().write(body); // JSONデータを送信します。
                exchange.close(); // 応答を閉じます。
            } catch (SQLException e) { // SQLiteの読み込み失敗を受け取ります。
                throw new IOException("SQLiteの処理に失敗しました", e); // HTTPサーバーへ例外を伝えます。
            } // SQL例外処理を終えます。
        }); // JSON入口を登録します。
        server.createContext("/", exchange -> { // 各HTTPアクセスを処理します。
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String method = exchange.getRequestMethod(); // GETやPOSTなどの方法を取り出します。
            String message; // HTTP応答の内容を入れます。
            try { // SQL例外をHTTP入出力例外に変換します。★
                if (path.equals("/add") && method.equals("POST")) { // Todo追加リクエストを処理します。
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送信内容をUTF-8で読み込みます。
                    String title = URLDecoder.decode(body.substring(5), StandardCharsets.UTF_8); // todo=以降を復号します。
                    if (!title.isEmpty()) { // 内容が空でない場合に追加します。
                        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO todos (title, done) VALUES (?, 0)")) { // INSERT文を準備します。★
                            statement.setString(1, title); // タイトルをSQLに設定します。★
                            statement.executeUpdate(); // Todoをデータベースへ追加します。★
                        } // INSERT文を閉じます。★
                    } // 入力内容の確認を終えます。
                    redirect(exchange); // 一覧ページへ戻します。
                    return; // このリクエストの処理を終えます。
                } else if (path.equals("/done") && method.equals("GET")) { // Todo完了リクエストを処理します。
                    updateDone(exchange.getRequestURI().getQuery()); // PreparedStatementで完了状態を更新します。★
                    redirect(exchange); // 一覧ページへ戻します。
                    return; // このリクエストの処理を終えます。
                } else if (path.equals("/delete") && method.equals("GET")) { // Todo削除リクエストを処理します。
                    deleteTodo(exchange.getRequestURI().getQuery()); // PreparedStatementでTodoを削除します。★
                    redirect(exchange); // 一覧ページへ戻します。
                    return; // このリクエストの処理を終えます。
                } else if (path.equals("/")) { // 一覧ページを表示します。
                    String query = exchange.getRequestURI().getQuery(); // 検索文字をURLから受け取ります。
                    String search = query != null && query.startsWith("search=") ? URLDecoder.decode(query.substring(7), StandardCharsets.UTF_8) : ""; // 検索文字を復号します。
                    message = renderTodos(search); // SELECT結果から一覧HTMLを作ります。★
                    sendHtml(exchange, message); // 一覧HTMLを返します。
                    return; // このリクエストの処理を終えます。
                } else { // その他のパスを処理します。
                    message = "ページが見つかりません"; // 見つからないページの案内を用意します。
                    byte[] body = message.getBytes(StandardCharsets.UTF_8); // 応答をUTF-8にします。
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 404応答の文字コードを明示します。
                    exchange.sendResponseHeaders(404, body.length); // 404応答を返します。
                    exchange.getResponseBody().write(body); // 応答内容を送信します。
                    exchange.close(); // 応答を閉じます。
                    return; // このリクエストの処理を終えます。
                } // パスごとの処理を終えます。
            } catch (SQLException e) { // SQL処理の失敗を受け取ります。★
                throw new IOException("SQLiteの処理に失敗しました", e); // HTTPサーバーへ例外を伝えます。★
            } // SQL例外処理を終えます。★
        }); // アクセス処理を登録します。
        server.start(); // サーバーの待ち受けを開始します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します。
    } // mainメソッドを終えます。

    private static void updateDone(String query) throws SQLException { // Todoを完了状態にします。★
        Integer id = parseId(query); // URLからTodo番号を取得します。
        if (id == null) return; // 番号が不正なら何もしません。
        try (PreparedStatement statement = connection.prepareStatement("UPDATE todos SET done = 1 WHERE id = ?")) { // UPDATE文を準備します。★
            statement.setInt(1, id); // 更新対象の番号を設定します。★
            statement.executeUpdate(); // 完了状態をデータベースへ反映します。★
        } // UPDATE文を閉じます。★
    } // 完了処理を終えます。★

    private static void deleteTodo(String query) throws SQLException { // Todoを削除します。★
        Integer id = parseId(query); // URLからTodo番号を取得します。
        if (id == null) return; // 番号が不正なら何もしません。
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM todos WHERE id = ?")) { // DELETE文を準備します。★
            statement.setInt(1, id); // 削除対象の番号を設定します。★
            statement.executeUpdate(); // Todoをデータベースから削除します。★
        } // DELETE文を閉じます。★
    } // 削除処理を終えます。★

    private static Integer parseId(String query) { // クエリから番号を取り出します。
        if (query == null || !query.startsWith("id=") || query.length() <= 3) return null; // idがなければ不正とします。
        try { // 数字への変換を試します。
            return Integer.parseInt(query.substring(3)); // idの値を返します。
        } catch (NumberFormatException e) { // 数字でない値を処理します。
            return null; // 不正な番号を返します。
        } // 数字への変換を終えます。
    } // 番号の読み取りを終えます。

    private static String jsonTodos() throws SQLException { // 全TodoをJSON配列に変換します。
        StringBuilder json = new StringBuilder("["); // JSON配列を開始します。
        try (PreparedStatement statement = connection.prepareStatement("SELECT title, done FROM todos ORDER BY id")) { // 全件取得SQLを準備します。
            try (ResultSet rows = statement.executeQuery()) { // Todo一覧を読み込みます。
                boolean first = true; // 先頭要素かを記録します。
                while (rows.next()) { // Todoを1件ずつ処理します。
                    if (!first) json.append(','); // 要素間にカンマを追加します。
                    json.append("{\"title\":\"").append(jsonEscape(rows.getString("title"))) // エスケープ済みタイトルを書きます。
                            .append("\",\"done\":").append(rows.getInt("done") == 1).append('}'); // 完了状態を書いて要素を閉じます。
                    first = false; // 次からは先頭要素ではないと記録します。
                } // 一覧の読み取りを終えます。
            } // 結果セットを閉じます。
        } // SQL文を閉じます。
        return json.append(']').toString(); // JSON配列を閉じて返します。
    } // JSON配列の作成を終えます。

    private static String jsonEscape(String value) { // JSON文字列内の特殊文字をエスケープします。
        StringBuilder escaped = new StringBuilder(); // 変換後の文字列を作ります。
        for (int i = 0; i < value.length(); i++) { // タイトルの文字を順に確認します。
            char ch = value.charAt(i); // 現在の文字を取り出します。
            switch (ch) { // 特殊文字ごとに処理します。
                case '"': escaped.append("\\\""); break; // 引用符をエスケープします。
                case '\\': escaped.append("\\\\"); break; // バックスラッシュをエスケープします。
                case '\b': escaped.append("\\b"); break; // バックスペースをエスケープします。
                case '\f': escaped.append("\\f"); break; // フォームフィードをエスケープします。
                case '\n': escaped.append("\\n"); break; // 改行をエスケープします。
                case '\r': escaped.append("\\r"); break; // 復帰をエスケープします。
                case '\t': escaped.append("\\t"); break; // タブをエスケープします。
                default: // その他の文字を処理します。
                    if (ch < 0x20) { // JSONで直接書けない制御文字を確認します。
                        escaped.append(String.format("\\u%04x", (int) ch)); // 制御文字をUnicode形式にします。
                    } else { // 通常の文字を処理します。
                        escaped.append(ch); // 文字をそのまま追加します。
                    } // 制御文字の処理を終えます。
            } // 特殊文字の処理を終えます。
        } // 文字列の確認を終えます。
        return escaped.toString(); // エスケープした文字列を返します。
    } // JSON文字列のエスケープを終えます。

    private static String renderTodos(String search) throws SQLException { // SELECT結果から一覧HTMLを作ります。★
        StringBuilder html = new StringBuilder("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Todoリスト</title><style>body { max-width: 640px; margin: 24px auto; padding: 0 16px; font-size: 16px; }</style></head><body><h1>Todoリスト</h1><form method='post' action='/add'><input name='todo'><button>追加</button></form><form method='get' action='/'><input name='search' value='"); // HTMLの先頭とフォームを作ります。
        html.append(escapeHtml(search)).append("'><button>検索</button></form><ul>"); // 検索欄と一覧を開始します。
        boolean found = false; // 表示対象があるか記録します。
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, title, done FROM todos ORDER BY id")) { // 一覧取得のSELECT文を準備します。★
            try (ResultSet rows = statement.executeQuery()) { // Todo全件を読み込みます。★
                while (rows.next()) { // 結果を1件ずつ処理します。
                    String title = rows.getString("title"); // Todoのタイトルを取得します。★
                    if (!title.contains(search)) continue; // 検索に一致しないTodoを飛ばします。
                    found = true; // 表示対象が見つかったことを記録します。
                    String mark = rows.getInt("done") == 1 ? " ✔" : ""; // 完了状態の印を作ります。★
                    int id = rows.getInt("id"); // Todo番号を取得します。★
                    html.append("<li>").append(escapeHtml(title)).append(mark).append(" <a href='/done?id=").append(id).append("'>完了</a> <a href='/delete?id=").append(id).append("'>削除</a></li>"); // Todoと操作リンクを追加します。★
                } // SELECT結果の処理を終えます。
            } // 結果セットを閉じます。★
        } // SELECT文を閉じます。★
        if (!found) html.append("<p>やることはないです</p>"); // 対象がない場合の案内を表示します。
        return html.append("</ul></body></html>").toString(); // HTMLを完成して返します。
    } // 一覧HTMLの作成を終えます。★

    private static String escapeHtml(String value) { // HTMLで特別な文字を置き換えます。★
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); // HTML表示を安全にします。★
    } // HTML文字の置換を終えます。★

    private static void sendHtml(com.sun.net.httpserver.HttpExchange exchange, String html) throws IOException { // HTML応答を送ります。
        byte[] body = html.getBytes(StandardCharsets.UTF_8); // HTMLをUTF-8のデータに変換します。
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTML形式と文字コードを指定します。
        exchange.sendResponseHeaders(200, body.length); // 正常応答とデータ長を伝えます。
        exchange.getResponseBody().write(body); // ブラウザへHTMLを送ります。
        exchange.close(); // 応答を閉じます。
    } // HTML応答を終えます。

    private static void redirect(com.sun.net.httpserver.HttpExchange exchange) throws IOException { // 一覧ページへリダイレクトします。
        exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします。
        exchange.sendResponseHeaders(303, -1); // トップページへの移動を伝えます。
        exchange.close(); // 応答を閉じます。
    } // リダイレクトを終えます。
} // Appクラスを終えます。
