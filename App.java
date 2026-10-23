import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。

public class App { // Appクラスを定義します。
    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。
        server.createContext("/", exchange -> { // "/"へのアクセスを処理します。
            String message = "こんにちは、サーバー!"; // ブラウザに返す文字を用意します。
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 返す文字の種類と文字コードを指定します。
            byte[] body = message.getBytes("UTF-8"); // 返す文字をUTF-8のデータに変換します。
            exchange.sendResponseHeaders(200, body.length); // 正常応答とデータの長さを伝えます。
            exchange.getResponseBody().write(body); // 文字のデータをブラウザに送ります。
            exchange.getResponseBody().close(); // 送信を終えて応答を閉じます。
        }); // "/"へのアクセス処理を登録します。
        server.start(); // サーバーの待ち受けを開始します。
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示します。
    } // mainメソッドを終えます。
} // Appクラスを終えます。
