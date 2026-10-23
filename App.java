import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。

public class App { // Appクラスを定義します。
    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。[1]
        server.createContext("/", exchange -> { // "/"へのアクセスを処理します。[毎]☆
            System.out.println("ハンドラが動いた");
            String message = "こんにちは、サーバー!"; // ブラウザに返す文字を用意します。[毎]
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の種類と文字コードを指定します。[毎]
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータに変換します。[毎]
            exchange.sendResponseHeaders(200, body.length); // 正常応答とデータの長さを伝えます。[毎]
            exchange.getResponseBody().write(body); // 文字のデータをブラウザに送ります。[毎]
            exchange.getResponseBody().close(); // 送信を終えて応答を閉じます。[毎]
        }); // "/"へのアクセス処理を登録します。[毎]
        server.start(); // サーバーの待ち受けを開始します。[毎]
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します。[1]
    } // mainメソッドを終えます。
} // Appクラスを終えます。
