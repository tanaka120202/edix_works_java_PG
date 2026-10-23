import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。
import java.net.URLDecoder; // URLで変換された文字を元に戻します。
import java.util.ArrayList; // 項目を順番に入れるリストを使います。
import java.util.List; // 項目のリストを使います。

public class App { // Appクラスを定義します。
    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。[1]
        server.createContext("/", exchange -> { // "/"へのアクセスを処理します。[毎]☆
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答形式と文字コードを指定します。
            if (path.equals("/hello")) { // パスが「/hello」か比べます。
                String query = exchange.getRequestURI().getRawQuery(); // URLのクエリ部分を取り出します。
                String name = query.substring(5); // 「name=」の後ろを切り出します。
                name = URLDecoder.decode(name, "UTF-8"); // URL表記をUTF-8の文字に戻します。
                message = "こんにちは、" + name + "さん！"; // 名前を挨拶に混ぜます。
            } else if (path.equals("/todos")) { // パスが「/todos」か比べます。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // /todosの返答形式と文字コードを指定します。
                List<String> todos = new ArrayList<>(); // Todo項目を入れるリストを用意します。
                todos.add("牛乳を買う"); // Todo項目を追加します。
                todos.add("卵を買う"); // Todo項目を追加します。
                todos.add("パンを買う"); // Todo項目を追加します。
                todos.add("自分の今日のTodo");
                String html = "<ul>"; // HTMLのリストを始めます。
                for (String todo : todos) { // Todo項目を1件ずつ取り出します。
                    html += "<li>" + todo + "</li>"; // Todo項目をHTMLに追加します。
                }
                html += "</ul>"; // HTMLのリストを閉じます。
                message = html; // HTMLを返す内容にします。

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
