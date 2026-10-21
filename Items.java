// Itemsという名前のクラス（プログラムのまとまり）を作ります。
public class Items {
    // プログラムの実行を始める場所です。
    public static void main(String[] args) {
        // Todoの文字列を3件、todosという配列（複数の値を入れる箱）に入れます。
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" };
        // iを0から始め、配列の件数より小さい間、1件ずつ繰り返します。
        for (int i = 0; i < todos.length; i++) {
            // i番目のTodoを<li>と</li>で囲み、1行で表示します。
            System.out.println("<li>" + todos[i] + "</li>");
        }
    }
}
