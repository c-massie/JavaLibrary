package scot.massie.lib.collections.trees;

public class TreeEntry<TNode, TLeaf>
{
    protected final Tree<TNode, TLeaf> sourceTree;

    protected final TreePath<TNode> path;

    protected final TLeaf item;

    public TreeEntry(Tree<TNode, TLeaf> sourceTree, TreePath<TNode> path, TLeaf item)
    {
        this.sourceTree = sourceTree;
        this.path = path;
        this.item = item;
    }

    public Tree<TNode, TLeaf> getTree()
    { return sourceTree; }

    public TreePath<TNode> getPath()
    { return path; }

    public TLeaf getItem()
    { return item; }

    @Override
    public String toString()
    { return "[" + path.toString() + "] = " + (item != null ? item : "(null)"); }
}
