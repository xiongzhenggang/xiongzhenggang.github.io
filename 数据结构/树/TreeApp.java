/**
 * 
 */
package xzg.com.structure;

import java.util.Stack;

/**
 * @author hasee
 * @TIME 2017年3月17日
 * 注意类的隐藏和实例创建
 */

class Node {
	int iDate;
	double fDate;
	Node leftChild;//左儿子
	Node rightChild;//右儿子
	public void display(){
	//	展示当前节点的值
		System.out.println("idate=："+iDate+"fDate=："+fDate);
	}
	}

class Tree{
	Node root ;
	public Tree(){
		root = null;
	}
	public  int treeLength = 0;
	//查询key值对应的几点
	public Node find(int key){
		Node currentNode = root;
		while(currentNode.iDate!=key){//循环不相等
			if(key<currentNode.iDate){//查询的节点比当前的小，将当前节点的左儿子（左儿子小于右儿子）
				currentNode=currentNode.leftChild;
			}else{
				currentNode=currentNode.rightChild;
			}
			//如果当前节点（没有根节点）不存在，返回空
			if(currentNode == null){
				return null;
			}
		}
		return currentNode;
	}
	//插入方法
	public void insert(int id,double data){
		Node newNode = new Node();//插入节点的初始化
		newNode.iDate=id;
		newNode.fDate=data;
		if(root == null){//先检查根结点
			treeLength++;
			root = newNode;
		}else{
			//如果存在根节点则从根节点开始向下搜寻插入
			Node currentNode = root;
			Node parent;//因为插入是在父节点的前提下插入，所以需要先定义父节点。
			while(true){
				parent = currentNode;
				if(id<parent.iDate){//小于向左搜索
					currentNode = parent.leftChild;//当前节点跳到父节点的左儿子上
					if(currentNode == null){
						treeLength++;
						parent.leftChild = newNode;
						return ;
					}
				}else{//向右儿子搜索
					currentNode = parent.rightChild;
					if(currentNode == null){//搜索到右儿子为空时插入新的节点
						treeLength++;
						parent.rightChild = newNode;
						return ;
					}
				}
			}
		}
	}
	/*删除较为复杂，三种情况考虑
	 * 第一种，该节点是叶节点（没有子节点）
	 * 第二种，该节点有一个子节点
	 * 第三种，该节点有两个子节点，最复杂
	 */	
	public boolean delete(int key){
		//删除也从更节点
		Node currentNode = root;
		Node parent = root;
		boolean isLeftChild =true;//用于表示删除的是左儿子还是右儿子
		 
		while(currentNode.iDate != key){
			parent = currentNode;
			if(currentNode.iDate < key){//向左
				isLeftChild = true;
				currentNode = parent.leftChild;
			}else{
				isLeftChild = false;
				currentNode = parent.rightChild;
			}
			if(currentNode == null){
				return false;//没有找到直接返回null
			}
		}
			//第一种，如果没有子节点就直接删除
			if(currentNode.leftChild == null && currentNode.rightChild == null){
				if(currentNode == root){//只有一个根节点，就直接删除
					root =null;
					}else if(isLeftChild){//有左儿子的情况
					parent.leftChild = null;
					}else{
					parent.rightChild = null;
				}
				//第二种情况只有一个节点，右儿子或者左儿子
			}else if(currentNode.rightChild == null){
					//只有左儿子的情况
				if(currentNode == root){
					root = currentNode.leftChild;
				}else if(isLeftChild){
					parent.leftChild = currentNode.leftChild;
				}else{
					parent.rightChild = currentNode.leftChild;
				}
				//只有右儿子的情况
			}else if(currentNode.leftChild == null){
				if(currentNode == root){
					root = currentNode.rightChild;
				}else if(isLeftChild){
					parent.leftChild = currentNode.rightChild;
				}else{
					parent.rightChild = currentNode.rightChild;
				}
			}else{
			//第三种是左右儿子都有的情况
				Node successor = getSuccessor(currentNode);//获取后继以及后继的子树
				if(currentNode == root){//要删除的为root时
					root = successor;
				}else if(isLeftChild){//是左儿子
					parent.leftChild = successor;
				}else{//是右儿子
					parent.rightChild = successor;
					successor.leftChild = currentNode.leftChild;
				}
			}
			return true;//成功
		}
	//本方法用于获取要删除节点的后继节点（就是比当前要删除节点大且子树中最小的一个数如果没有就是要删除节点的右儿子）
	private Node getSuccessor(Node delNode){
		Node successorParent = delNode;//记录父节点
		Node successor = delNode;//作为要返回的最小节点
		Node currentNode = delNode.rightChild;//用于记录当前节点
		while(currentNode != null){
			successorParent = successor;
			successor = currentNode;//如果当前节点的的左儿子为null，则此节点为后继节点
			currentNode = currentNode.leftChild;//向左边搜索
		}
		//知道当前节点为空的时候，搜索到了最底层
		if(successor != delNode.rightChild){//如果要返回的节点不是，要删除节点的右儿子
			//以上条件满足返回节点是删除节点右子树的最小节点。切断此后继节点
			successorParent.leftChild = successor.rightChild;//搜索知道没有左节点时，将后继的右儿子替换后继原来的位置（即使null）
			successor.rightChild = delNode.rightChild;//将原来的删除节点右子树添加到后继上
		}
		return successor;//返回的后继包括已排序的子树
	}
	//三种遍历树结构，前序、中序、后序。 以根的位置区分
	public  void traverse(int traverseTye){
		switch(traverseTye){
			case 1:System.out.println("\npreOder traverse1");
						preOder(root);
						break;
			case 2:System.out.println("\ninOrder traverse2");
						inOrder(root);
						break;
			case 3:System.out.println("\npostOrder traverse3");
						postOrder(root);
						break;
		}
	}
	//前序，根-》左-》右
	private void preOder(Node localRoot){
		if(localRoot != null){
			System.out.println(localRoot.iDate+"  ");
			preOder(localRoot.leftChild);
			preOder(localRoot.rightChild);
		}
	}
	//中序，左-》根-。右
	private void inOrder(Node localRoot){
		if(localRoot != null){
			preOder(localRoot.leftChild);
			System.out.println(localRoot.iDate+"  ");
			preOder(localRoot.rightChild);
			
		}
	}
	//后序，左-》右-》根
	private void postOrder(Node localRoot){
		if(localRoot != null){
			postOrder(localRoot.leftChild);
			postOrder(localRoot.rightChild);
			System.out.println(localRoot.iDate+"  ");
		}
	}
	//
	public void display(){
		Stack<Node> globalStack = new Stack<Node>();
		globalStack.push(root);
		int nBlank = 32;
		boolean isRowEmpty = false;
		System.out.println("..........................................................");
		while(isRowEmpty == false){
			Stack<Node> localStack =new Stack<Node>();
			isRowEmpty = true;
			for(int j=0;j<nBlank;j++)
				System.out.print(" ");
			while(!globalStack.isEmpty()){
				Node tmp = (Node)globalStack.pop();
				if(tmp != null){
					System.out.println(tmp.iDate);
					localStack.push(tmp.leftChild);
					localStack.push(tmp.rightChild);
					if(tmp.leftChild != null ||tmp.rightChild != null){
						isRowEmpty = false;
					}else{
						System.out.print("--");
						localStack.push(null);
						localStack.push(null);				}
				}
				for(int j=0;j<nBlank*2+2;j++){
					System.out.print("  ");
				}
				nBlank/=2;
				while(localStack.isEmpty() == false){
					globalStack.push(localStack.pop());
				}
			}
			System.out.println(".............................................");
		}
	}
}
public class TreeApp{
	public static void main(String[] args){
		Tree theTree = new Tree();
		theTree.insert(5,43);
		theTree.insert(3, 5.5);
		theTree.insert(7, 5.5);
		theTree.insert(4, 5.5);
		theTree.insert(7, 5.5);
		theTree.insert(8, 5.5);
		theTree.insert(9, 5.5);
		theTree.traverse(1);
	}
	
}
