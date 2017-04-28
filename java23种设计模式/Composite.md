# 第14种 组合模式
### 组合模式有时又叫部分-整体模式在处理类似树形结构的问题时比较方便，看看关系图：
![组合模式](/java23种设计模式/img/composite.png)
### 代码:
```java 
/**
 * @author xzg
 *	定义基础节点、组件
 */
class TreeNode{
	public TreeNode(String name){
		this.name = name;
	}
	private String name;
	private TreeNode parent;
	private Vector<TreeNode> child = new Vector<TreeNode>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public Vector<TreeNode> getChild() {
		return child;
	}
	public void setChild(Vector<TreeNode> child) {
		this.child = child;
	}
	/**
	 * @param treeNode
	 * 增加孩子节点
	 */
	public void add(TreeNode treeNode){
		child.addElement(treeNode);
	}
	/**
	 * @param treeNode
	 *	删除孩子节点
	 */
	public void remove(TreeNode treeNode){
		child.remove(treeNode);
	}
	/**
	 * @return
	 * 取得所有的孩子节点
	 */
	public Enumeration<TreeNode> getChildren(){
		return child.elements();
	}
}
```
```java
/**
 * @author xzg
 *	组合这些基础组件
 */
class Tree{
	TreeNode root = null;
	public Tree(String name){
		this.root = new TreeNode(name);
	}
	/**
	 * 测试
	 * 初始化一棵树
	 */
	public void composite(){
		//创建节点
		TreeNode node01 = new TreeNode("第一个分支");
		TreeNode node02 = new TreeNode("第一片叶子");
		TreeNode node03 = new TreeNode("第二片叶子");
		//组合这棵树
		this.root.add(node01);
		node01.add(node02);
		node01.add(node03);
	}
	public void display(){
		
		System.out.println(this.root.getName());
		for(TreeNode treeNode:this.root.getChild()){
			System.out.println(treeNode.getName());
			for(TreeNode treeNode2: treeNode.getChild()){
				System.out.print(treeNode2.getName()+"  ");
			}
		}
	}
}

```

### 测试类
```java
public class CompositeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Tree tree = new Tree("我是一棵树");
		tree.composite();
		tree.display();
	}
}
```
### 使用场景：将多个对象组合在一起进行操作，常用于表示树形结构中，例如二叉树，数等。
