// Itemsという名前のクラス（プログラムのまとまり）を作ります。
public class Items {
    // プログラムの実行を始める場所です。
    public static void main(String[] args) {
        // Todoの文字列を配列（複数の値を入れる箱）に入れます。
        String[] todos = { "牛乳を買う", "卵を買う", "パンを買う", "掃除をする" };
        boolean[] done = { true, false, false, false }; // 変更
        // iを0から始め、配列の件数より小さい間、1件ずつ繰り返します。
        for (int i = 0; i < todos.length; i++) {
            // 項目が空でない場合だけ表示します。
            if (!todos[i].isEmpty()) {
                // 済んでいれば[済]を付け、<li>で囲んで表示します。 // 変更
                System.out.println("<li>" + (done[i] ? "[済] " : "") + todos[i] + "</li>"); // 変更
            }
        }
    }
}