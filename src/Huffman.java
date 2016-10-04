import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.nio.charset.Charset;

public class Huffman {

	// Huffman编码算法主要用到的数据结构是完全二叉树(full binary tree)和优先级队列。
	// 后者用的是java.util.PriorityQueue，前者自己实现(都为内部类)
	static class Tree {
		private Node root;

		public Node getRoot() {
			return root;
		}

		public void setRoot(Node root) {
			this.root = root;
		}
	}

	static class Node implements Comparable<Node> {
		private String chars = "";
		private int frequence = 0;
		private Node parent;
		private Node leftNode;
		private Node rightNode;

		@Override
		public int compareTo(Node n) {
			return frequence - n.frequence;
		}

		public boolean isLeaf() {
			return chars.length() == 1;
		}

		public boolean isRoot() {
			return parent == null;
		}

		public boolean isLeftChild() {
			return parent != null && this == parent.leftNode;
		}

		public int getFrequence() {
			return frequence;
		}

		public void setFrequence(int frequence) {
			this.frequence = frequence;
		}

		public String getChars() {
			return chars;
		}

		public void setChars(String chars) {
			this.chars = chars;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		public Node getLeftNode() {
			return leftNode;
		}

		public void setLeftNode(Node leftNode) {
			this.leftNode = leftNode;
		}

		public Node getRightNode() {
			return rightNode;
		}

		public void setRightNode(Node rightNode) {
			this.rightNode = rightNode;
		}
	}

	//统计数据的频率信息
	public static Map<Character, Integer> statistics(char[] charArray) {
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		for (char c : charArray) {
			Character character = new Character(c);
			if (map.containsKey(character)) {
				map.put(character, map.get(character) + 1);
			} else {
				map.put(character, 1);
			}
		}
		return map;
	}

	// 构建树：把统计信息转为Node存放到一个优先级队列里面，每一次从队列里面弹出两个最小频率的节点，
	// 构建一个新的父Node(非叶子节点), 字符内容是刚弹出来的两个节点字符内容之和，频率也是它们的和，
	// 最开始的弹出来的作为左子节点，后面一个作为右子节点，并且把刚构建的父节点放到队列里面。
	// 重复以上的动作N-1次，N为不同字符的个数(每一次队列里面个数减1)。
	private static Tree buildTree(Map<Character, Integer> statistics,
			List<Node> leafs) {
		Character[] keys = statistics.keySet().toArray(new Character[0]);
		PriorityQueue<Node> priorityQueue = new PriorityQueue<Node>();
		
		for (Character character : keys) {
			Node node = new Node();
			node.chars = character.toString();
			node.frequence = statistics.get(character);
			priorityQueue.add(node);
			leafs.add(node);
		}

		int size = priorityQueue.size();
		for (int i = 1; i <= size - 1; i++) {
			Node node1 = priorityQueue.poll();
			Node node2 = priorityQueue.poll();

			Node sumNode = new Node();
			sumNode.chars = node1.chars + node2.chars;
			sumNode.frequence = node1.frequence + node2.frequence;

			sumNode.leftNode = node1;
			sumNode.rightNode = node2;

			node1.parent = sumNode;
			node2.parent = sumNode;

			priorityQueue.add(sumNode);
		}

		Tree tree = new Tree();
		tree.root = priorityQueue.poll();
		return tree;
	}

	//编码：
	//某个字符对应的编码为，从该字符所在的叶子节点向上搜索，如果该字符节点是父节点的左节点，编码字符之前加0，
	//反之如果是右节点，加1，直到根节点。只要获取了字符和二进制码之间的mapping关系，编码就非常简单。
	public static String encode(String originalStr,Map<Character, Integer> statistics) {
		if (originalStr == null || originalStr.equals("")) {
			return "";
		}

		char[] charArray = originalStr.toCharArray();
		List<Node> leafNodes = new ArrayList<Node>();
		buildTree(statistics, leafNodes);
		Map<Character, String> encodInfo = buildEncodingInfo(leafNodes);

		StringBuffer buffer = new StringBuffer();
		for (char c : charArray) {
			Character character = new Character(c);
			buffer.append(encodInfo.get(character));
		}

		return buffer.toString();
	}

	private static Map<Character, String> buildEncodingInfo(List<Node> leafNodes) {
		Map<Character, String> codewords = new HashMap<Character, String>();
		for (Node leafNode : leafNodes) {
			Character character = new Character(leafNode.getChars().charAt(0));
			String codeword = "";
			Node currentNode = leafNode;

			do {
				if (currentNode.isLeftChild()) {
					codeword = "0" + codeword;
				} else {
					codeword = "1" + codeword;
				}

				currentNode = currentNode.parent;
			} while (currentNode.parent != null);

			codewords.put(character, codeword);
		}

		return codewords;
	}

	// 解码:因为Huffman编码算法能够保证任何的二进制码都不会是另外一个码的前缀，
	// 解码非常简单，依次取出二进制的每一位，从树根向下搜索，1向右，0向左，到了叶子节点(命中)，退回根节点继续重复以上动作
	public static String decode(String binaryStr,
			Map<Character, Integer> statistics) {
		if (binaryStr == null || binaryStr.equals("")) {
			return "";
		}

		char[] binaryCharArray = binaryStr.toCharArray();
		LinkedList<Character> binaryList = new LinkedList<Character>();
		int size = binaryCharArray.length;
		for (int i = 0; i < size; i++) {
			binaryList.addLast(new Character(binaryCharArray[i]));
		}

		List<Node> leafNodes = new ArrayList<Node>();
		Tree tree = buildTree(statistics, leafNodes);

		StringBuffer buffer = new StringBuffer();

		while (binaryList.size() > 0) {
			Node node = tree.root;

			do {
				Character c = binaryList.removeFirst();
				if (c.charValue() == '0') {
					node = node.leftNode;
				} else {
					node = node.rightNode;
				}
			} while (!node.isLeaf());

			buffer.append(node.chars);
		}

		return buffer.toString();
	}

	public static void main(String[] args) {
		String oriStr = "Huffman codes compress data very effectively: savings of 20% to 90% are typical, "
				+ "depending on the characteristics of the data being compressed. 中华崛起";
		Map<Character, Integer> statistics = statistics(oriStr.toCharArray());
		String encodedBinariStr = encode(oriStr, statistics);
		String decodedStr = decode(encodedBinariStr, statistics);

		System.out.println("Original sstring: " + oriStr);
		System.out.println("Huffman encoed binary string: " + encodedBinariStr);
		System.out.println("decoded string from binariy string: " + decodedStr);

		System.out.println("binary string of UTF-8: "
				+ getStringOfByte(oriStr, Charset.forName("UTF-8")));
		System.out.println("binary string of UTF-16: "
				+ getStringOfByte(oriStr, Charset.forName("UTF-16")));
		System.out.println("binary string of US-ASCII: "
				+ getStringOfByte(oriStr, Charset.forName("US-ASCII")));
		System.out.println("binary string of GB2312: "
				+ getStringOfByte(oriStr, Charset.forName("GB2312")));
	}

	public static String getStringOfByte(String str, Charset charset) {
		if (str == null || str.equals("")) {
			return "";
		}

		byte[] byteArray = str.getBytes(charset);
		int size = byteArray.length;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < size; i++) {
			byte temp = byteArray[i];
			buffer.append(getStringOfByte(temp));
		}

		return buffer.toString();
	}

	public static String getStringOfByte(byte b) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 7; i >= 0; i--) {
			byte temp = (byte) ((b >> i) & 0x1);
			buffer.append(String.valueOf(temp));
		}

		return buffer.toString();
	}

}
