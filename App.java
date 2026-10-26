import com.sun.net.httpserver.HttpServer; // Webサーバーを使います。
import java.net.InetSocketAddress; // 待ち受けるポート番号を指定します。
import java.net.URLDecoder; // URLで変換された文字を元に戻します。
import java.nio.charset.StandardCharsets; // UTF-8の文字コードを指定します。
import java.util.ArrayList; // 項目を順番に入れるリストを使います。
import java.util.List; // 項目のリストを使います。

class Todo { // Todoの情報をまとめるクラスです。★変更
    private final int id; // Todoの番号です。★変更
    private final String title; // Todoの内容です。★変更
    private boolean done; // 完了したかを表します。★変更

    Todo(int id, String title) { // Todoを作るときに番号と内容を受け取ります。★変更
        this.id = id; // 番号を保存します。★変更
        this.title = title; // 内容を保存します。★変更
        this.done = false; // 最初は未完了にします。★変更
    } // コンストラクタを終えます。★変更

    int getId() { // 番号を読み出します。★変更
        return id; // 番号を返します。★変更
    } // メソッドを終えます。★変更

    String getTitle() { // 内容を読み出します。★変更
        return title; // 内容を返します。★変更
    } // メソッドを終えます。★変更

    boolean isDone() { // 完了したかを読み出します。★変更
        return done; // 完了状態を返します。★変更
    } // メソッドを終えます。★変更

    void setDone(boolean done) { // 完了状態を書き換えます。★変更
        this.done = done; // 新しい完了状態を保存します。★変更
    } // メソッドを終えます。★変更
} // Todoクラスを終えます。★変更

public class App { // Appクラスを定義します。
    static List<Todo> todos = new ArrayList<>(); // Todoを保存するリストです。★変更
    static int nextId = 1; // 次に使う番号です。★変更

    public static void main(String[] args) throws Exception { // Javaが最初に実行する場所です。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番ポートでサーバーを用意します。[1]
        todos.add(new Todo(nextId++, "牛乳を買う")); // 起動時のサンプルTodoを追加します。★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // 起動時の2件目を用意します。★変更
        egg.setDone(true); // 2件目を完了状態にします。★変更
        todos.add(egg); // 2件目をリストに追加します。★変更
        server.createContext("/", exchange -> { // "/"へのアクセスを処理します。[毎]☆
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出します。
            String method = exchange.getRequestMethod(); // GETやPOSTなどの方法を取り出します。
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常の返答形式と文字コードを指定します。
            if (path.equals("/add") && method.equals("POST")) { // POSTで/addに来たTodoを追加します。
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // 送られた内容をUTF-8で読み取ります。
                String value = body.substring(5); // 「todo=」の5文字を取り除きます。
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // 入力内容をUTF-8の文字に戻します。★変更
                if (!title.isEmpty()) { // 入力が空でないときだけ追加します。
                    todos.add(new Todo(nextId, title)); // 新しいTodoを番号付きで追加します。★変更
                    nextId++; // 次に使う番号を1つ進めます。★変更
                }
                exchange.getResponseHeaders().set("Location", "/"); // 戻り先をトップページにします。
                exchange.sendResponseHeaders(303, -1); // トップページへの移動を伝えます。
                exchange.close(); // 応答を閉じます。
                return; // このリクエストの処理を終えます。
            } else if (path.equals("/")) { // トップページにフォームとTodo一覧を表示します。
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // HTMLの返答形式と文字コードを指定します。
                String html = "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>"; // Todo入力フォームと一覧を始めます。
                for (Todo todo : todos) { // Todoを1件ずつ取り出します。★変更
                    String mark = ""; // 完了印を空で用意します。★変更
                    if (todo.isDone()) { // 完了済みかを確認します。★変更
                        mark = " ✔"; // 完了済みのTodoに印を付けます。★変更
                    } // 完了確認を終えます。★変更
                    html += "<li>" + todo.getTitle() + mark + "</li>"; // 内容と完了印を一覧に追加します。★変更
                }
                html += "</ul>"; // HTMLの一覧を閉じます。
                message = html; // HTMLを返す内容にします。
            } else { // その他のパスを受け付けます。
                message = "ページが見つかりません"; // 見つからないページの案内です。
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
