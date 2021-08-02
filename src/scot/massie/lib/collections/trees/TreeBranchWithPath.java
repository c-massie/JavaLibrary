package scot.massie.lib.collections.trees;

import java.util.Objects;

public class TreeBranchWithPath<T extends Tree<TNode, TLeaf>, TNode, TLeaf>
{
    protected final T branch;
    protected final TreePath<TNode> path;

    public TreeBranchWithPath(T branch, TreePath<TNode> path)
    {
        this.branch = branch;
        this.path = path;
    }

    public T getBranch()
    { return branch; }

    public TreePath<TNode> getPath()
    { return path; }

    @Override
    public String toString()
    { return "Tree branch at path: " + path.toString(); }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;

        if(o == null || getClass() != o.getClass())
            return false;

        TreeBranchWithPath<?, ?, ?> other = (TreeBranchWithPath<?, ?, ?>)o;
        return Objects.equals(branch, other.branch) && Objects.equals(path, other.path);
    }

    @Override
    public int hashCode()
    { return Objects.hash(branch, path); }
}
