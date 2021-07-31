package scot.massie.lib.collections.trees;

public class RecursiveTreeView<TNode, TLeaf>
        extends AbstractRecursiveTreeView<RecursiveTree<TNode, TLeaf>, TNode, TLeaf>
{

    public RecursiveTreeView(RecursiveTree<TNode, TLeaf> source, TreePath<TNode> viewPath)
    { super(source, viewPath); }

    @Override
    public RecursiveTree<TNode, TLeaf> getInternalBranch(RecursiveTree<TNode, TLeaf> sourceTree,
                                                         TreePath<TNode> branchPath)
    { return sourceTree.getInternalBranch(branchPath); }

    @Override
    public RecursiveTree<TNode, TLeaf> getOrCreateInternalBranch(RecursiveTree<TNode, TLeaf> sourceTree,
                                                                 TreePath<TNode> branchPath)
    { return sourceTree.getOrCreateInternalBranch(branchPath); }

    @Override
    public void trim(RecursiveTree<TNode, TLeaf> sourceTree, TreePath<TNode> branchPath)
    { sourceTree.trim(branchPath); }

    @Override
    public RecursiveTree<TNode, TLeaf> getNewEmptyTree()
    { return new RecursiveTree<>(); }
}
