package scot.massie.lib.collections.trees;

import java.util.Objects;

/**
 * A pairing of a branch of a tree (as another tree) with the path that that branch's root path has in the tree it came
 * from.
 * @param <T> The type of the branch.
 * @param <TNode> The type of the individual nodes in the tree paths.
 * @param <TLeaf> The type of the items stored in the tree.
 */
public class TreeBranchWithPath<T extends Tree<TNode, TLeaf>, TNode, TLeaf>
{
    /**
     * The tree branch.
     */
    protected final T branch;

    /**
     * The path the branch's root path has in the tree it came from.
     */
    protected final TreePath<TNode> path;

    /**
     * Creates a new TreeBranchWithPath.
     * @param branch The branch.
     * @param path The path of the branch's root path in the source tree.
     */
    public TreeBranchWithPath(T branch, TreePath<TNode> path)
    {
        this.branch = branch;
        this.path = path;
    }

    /**
     * Gets the branch.
     * @return The branch.
     */
    public T getBranch()
    { return branch; }

    /**
     * Gets the path. This path represents the branch's root path's path in the source tree.
     * @return The branch's path.
     */
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
