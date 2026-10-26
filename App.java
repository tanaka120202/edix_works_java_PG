import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。
import java.net.URLDecoder; // URLで変換された文字を元に戻します。
import java.nio.charset.StandardCharsets; // UTF-8の文字コードを指定します。
import java.util.ArrayList; // 項目を順番に入れるリストを使います。
import java.util.List; // 項目のリストを使います。

public class App { // Appクラスを定義します。
    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。[1]
        List<String> todos = new ArrayList<>(); // Todoを保存する空のリストを用意します。
        server.createContext("/", exchange -> { // "/"へのアクセスを処理します。[毎]☆
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String method = exchange.getRequestMethod(); // GETやPOSTなどの方法を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答形式と文字コードを指定します。
            if (path.equals("/add") && method.equals("POST")) { // POSTで/addに来たTodoを追加します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容をUTF-8で読み取ります。
                String value = body.substring(5); // 「todo=」の5文字を取り除きます。
                String todo = URLDecoder.decode(value, StandardCharsets.UTF_8); // URL表記をUTF-8の文字に戻します。
                if (!todo.isEmpty()) { // 入力が空でないときだけ追加します。
                    todos.add(todo); // Todoをリストに追加します。
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // トップページへの移動を伝えます。
                exchange.close(); // 応答を閉じます。
                return; // このリクエストの処理を終えます。
            } else if (path.equals("/")) { // トップページにフォームとTodo一覧を表示します。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTMLの返答形式と文字コードを指定します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // Todo入力フォームと一覧を始めます。
                for (String todo : todos) { // Todo項目を1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // Todo項目をHTMLに追加します。
                }
                html += "</ul>"; // HTMLの一覧を閉じます。
                message = html; // HTMLを返す内容にします。
            } else if (path.equals("/hello")) { // パスが「/hello」か比べます。
                String query = exchange.getRequestURI().getRawQuery(); // URLのクエリ部分を取り出します。
                String name = query.substring(5); // 「name=」の後ろを切り出します。
                name = URLDecoder.decode(name, "UTF-8"); // URL表記をUTF-8の文字に戻します。
                message = "こんにちは、" + name + "さん！"; // 名前を挨拶に混ぜます。
            } else if (path.equals("/bye")) { // パスが「/bye」か比べます。
                message = "さようなら！";
            } else if (path.equals("/mane")) { // パスが「/bye」か比べます。
                message = "今日の晩御飯はカレー！";
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータに変換します。[毎]
            exchange.sendResponseHeaders(200, body.length); // 正常応答とデータの長さを伝えます。[毎]
            exchange.getResponseBody().write(body); // 文字のデータをブラウザに送ります。[毎]
            exchange.getResponseBody().close(); // 送信を終えて応答を閉じます。[毎]
        }); // "/"へのアクセス処理を登録します。[毎]
        server.start(); // サーバーの待ち受けを開始します。[毎]
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します。[1]
    } // mainメソッドを終えます。
} // Appクラスを終えます。
