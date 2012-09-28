public class build_tagger {

	public static void main(String[] args) {
		Tagger t = new Tagger(null);
		t.add("hello", "world");
		t.add("hello", "world");
		t.add("hello", "world");
		t.add("hello", "world");
		t.add("hello", "shit");
		t.add("hello", "shit");
		System.out.println(t.posTransitions);

	}
}
