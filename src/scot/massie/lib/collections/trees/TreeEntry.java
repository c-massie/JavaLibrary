package scot.massie.lib.collections.trees;

import scot.massie.lib.utils.tuples.Pair;

/**
 * An item from a tree, paired with information about its association with the tree it came from.
 * @param <TNode> The type of the nodes in the entry's path.
 * @param <TLeaf> The type of the item.
 */
public class TreeEntry<TNode, TLeaf>
{
    /**
     * The tree this entry represents an item and path in.
     */
    protected final Tree<TNode, TLeaf> sourceTree;

    /**
     * The path of the item in the tree.
     */
    protected final TreePath<TNode> path;

    /**
     * The item in the source tree at the path.
     */
    protected final TLeaf item;

    /**
     * Creates a new tree entry.
     * @param sourceTree The tree this represents an item in.
     * @param path The path of the item in its source tree.
     * @param item The item in the source tree at the path.
     */
    public TreeEntry(Tree<TNode, TLeaf> sourceTree, TreePath<TNode> path, TLeaf item)
    {
        this.sourceTree = sourceTree;
        this.path = path;
        this.item = item;
    }

    /**
     * Gets the tree this entry came from.
     * @return The tree this entry came from.
     */
    public Tree<TNode, TLeaf> getTree()
    { return sourceTree; }

    /**
     * Gets the path this entry had in its tree.
     * @return The path this entry represented in the tree it came from.
     */
    public TreePath<TNode> getPath()
    { return path; }

    /**
     * Gets the item that was stored in the tree.
     * @return The item that was stored in the tree.
     */
    public TLeaf getItem()
    { return item; }

    /**
     * Gets a new pair of this entry's path and item.
     * @return A new pair object, where the first element is the tree path of this entry and the second element is the
     *         item.
     */
    public Pair<TreePath<TNode>, TLeaf> toPair()
    { return new Pair<>(path, item); }

    @Override
    public String toString()
    { return "[" + path.toString() + "] = " + (item != null ? item : "(null)"); }
}
