public class Item { // Itemというクラス（プログラムのまとまり）を作ります。
    public static void main(String[] args) { // 実行すると、ここから処理が始まります。
        String title = "javaの学習"; // String型（文字列を扱う種類）の変数（値を入れる箱）titleに文字を入れます。
        String html = "<li>" + title + "</li>; // +で文字列をつなぎ、HTML（Webページの書き方）のリスト項目を作ります。
        boolean done = false;
        int count = 3;

        System.out.println(html); // 作った文字列をターミナル（文字で操作する画面）に1行表示します。
        System.out.println(done);
        System.out.println("いま" + count + "件");
    } // 処理の終わりです。
} // Itemの終わりです。