package scot.massie.lib.collections.trees.exceptions;

import scot.massie.lib.collections.trees.Tree;
import scot.massie.lib.collections.trees.TreePath;

public class NoItemAtPathException extends RuntimeException
{
    protected final Tree<?, ?> tree;
    protected final TreePath<?> path;

    public NoItemAtPathException(Tree<?, ?> tree, TreePath<?> path)
    {
        super("There was no item in the tree at the path: " + path.toString());
        this.tree = tree;
        this.path = path;
    }

    public NoItemAtPathException(Tree<?, ?> tree, TreePath<?> path, String message)
    {
        super(message);
        this.tree = tree;
        this.path = path;
    }

    public Tree<?, ?> getTree()
    { return tree; }

    public TreePath<?> getPath()
    { return path; }
}
