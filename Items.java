// Itemsという名前のクラス（プログラムのまとまり）を作ります。
public class Items {
    // プログラムの実行を始める場所です。
    public static void main(String[] args) {
        // Todoの文字列を3件、todosという配列（複数の値を入れる箱）に入れます。
        String[] todos = { "牛乳を買う", "", "パンを買う" };
        // iを0から始め、配列の件数より小さい間、1件ずつ繰り返します。
        for (int i = 0; i < todos.length; i++) {
            // 項目が空でない場合だけ、<li>で囲んで表示します。 // 変更
            if (!todos[i].isEmpty()) { // 変更
                System.out.println("<li>" + todos[i] + "</li>"); // 変更
            } // 変更
        }
    }
}